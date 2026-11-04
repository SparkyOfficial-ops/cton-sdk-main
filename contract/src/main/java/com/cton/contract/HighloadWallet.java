// HighloadWallet.java - реалізація високонавантаженого кошелька
// Author: Андрій Будильников (Sparky)
// Highload wallet implementation
// Реализация высоконагруженного кошелька

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Boc;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;
import com.cton.sdk.Crypto;

/**
 * Реалізація високонавантаженого кошелька (Highload Wallet v2)
 * 
 * Highload wallet implementation (Highload Wallet v2)
 * Реализация высоконагруженного кошелька (Highload Wallet v2)
 */
public class HighloadWallet extends BaseWallet {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final int subwalletId;
    private final int queryTtlSeconds;
    
    /**
     * @param queryTtlSeconds how long the external message stays valid.
     *        The high 32 bits of {@code query_id} become {@code now + queryTtlSeconds}.
     */
    public HighloadWallet(Address address, TonApiClient apiClient, int subwalletId, int queryTtlSeconds) {
        super(address, apiClient);
        if (queryTtlSeconds < 1) {
            throw new IllegalArgumentException("highload query ttl must be positive");
        }
        this.subwalletId = subwalletId;
        this.queryTtlSeconds = queryTtlSeconds;
    }

    /**
     * Конструктор
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     * @param subwalletId ідентифікатор підкошелька
     */
    public HighloadWallet(Address address, TonApiClient apiClient, int subwalletId) {
        this(address, apiClient, subwalletId, 3600);
    }
    
    /**
     * Конструктор зі значенням subwalletId за замовчуванням
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public HighloadWallet(Address address, TonApiClient apiClient) {
        this(address, apiClient, 698983191);
    }
    
    @Override
    public Cell createTransfer(Address destination, BigInteger amount, String comment) throws IOException {
        // Для високонавантаженого кошелька створюємо один елемент масиву
        BulkTransfer[] transfers = new BulkTransfer[] {
            new BulkTransfer(destination, amount, comment)
        };
        return createBulkTransfer(transfers);
    }
    
    @Override
    public Cell createBulkTransfer(BulkTransfer[] transfers) throws IOException {
        return signingCell(transfers, nextQueryId());
    }

    /**
     * Signs the external message and returns its cell hash and BOC. Nothing is broadcast.
     */
    public PreparedBulk prepareBulkTransfer(BulkTransfer[] transfers, Crypto.PrivateKey privateKey) throws IOException {
        if (privateKey == null) {
            throw new IllegalArgumentException("wallet key is required");
        }
        long queryId = nextQueryId();
        Cell signing = signingCell(transfers, queryId);
        byte[] signature = Crypto.sign(privateKey, cellRepresentationHash(signing));
        if (signature == null || signature.length != 64) {
            throw new IOException("highload signature must be 64 bytes");
        }
        Cell body = new CellBuilder()
                .storeBytes(signature)
                .storeUInt(32, this.subwalletId & 0xFFFFFFFFL)
                .storeUInt(64, queryId)
                .storeUInt(1, 1)
                .storeRef(signing.getRef(0))
                .build();
        Cell external = createExternalInMessage(body);
        byte[] hash = cellRepresentationHash(external);
        try (Boc boc = new Boc(external)) {
            byte[] bytes = boc.serialize(true, true);
            if (bytes == null || bytes.length == 0) {
                throw new IOException("highload message boc is empty");
            }
            return new PreparedBulk(toHex(hash), bytes);
        }
    }

    /**
     * Signed external message. {@code hashHex} is the cell hash Toncenter returns from {@code sendBoc}.
     */
    public static final class PreparedBulk {
        private final String hashHex;
        private final byte[] boc;

        PreparedBulk(String hashHex, byte[] boc) {
            this.hashHex = hashHex;
            this.boc = boc.clone();
        }

        public String hashHex() {
            return hashHex;
        }

        public byte[] boc() {
            return boc.clone();
        }
    }
    
    @Override
    public int getVersion() throws IOException {
        return 2; // Highload Wallet v2
    }
    
    @Override
    public int getSubwalletId() throws IOException {
        return this.subwalletId;
    }
    
    /**
     * Надіслати багато транзакцій
     * @param transfers масив транзакцій
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void sendBulkTransfer(BulkTransfer[] transfers, Crypto.PrivateKey privateKey) throws IOException {
        PreparedBulk prepared = prepareBulkTransfer(transfers, privateKey);
        apiClient.sendBoc(prepared.boc());
    }

    /**
     * High 32 bits are the expiration unix time. Low 32 bits keep two messages sent in the same
     * second from sharing a {@code query_id}. The contract rejects {@code query_id < now() << 32}.
     */
    private long nextQueryId() {
        long expireAt = Instant.now().getEpochSecond() + queryTtlSeconds;
        if (expireAt <= 0 || expireAt > 0x7FFF_FFFFL) {
            throw new IllegalArgumentException("highload query expiration is out of range");
        }
        long nonce = RANDOM.nextInt() & 0xFFFFFFFFL;
        return (expireAt << 32) | nonce;
    }

    private Cell signingCell(BulkTransfer[] transfers, long queryId) {
        if (transfers == null || transfers.length < 1 || transfers.length > 254) {
            throw new IllegalArgumentException("a highload batch must contain 1..254 transfers");
        }
        List<HighloadDict.Order> orders = new ArrayList<>(transfers.length);
        for (int i = 0; i < transfers.length; i++) {
            BulkTransfer transfer = transfers[i];
            int mode = transfer.getMode();
            if (mode < 0 || mode > 255) {
                throw new IllegalArgumentException("send mode must fit in 8 bits");
            }
            Cell internal = transfer.getBody() != null
                    ? createInternalMessage(
                            transfer.getDestination(),
                            transfer.getAmount(),
                            transfer.getBody(),
                            transfer.isBounce())
                    : createInternalMessage(
                            transfer.getDestination(),
                            transfer.getAmount(),
                            transfer.getComment(),
                            transfer.isBounce());
            orders.add(new HighloadDict.Order(i, mode, internal));
        }
        Cell dict = HighloadDict.root(orders);
        return new CellBuilder()
                .storeUInt(32, this.subwalletId & 0xFFFFFFFFL)
                .storeUInt(64, queryId)
                .storeUInt(1, 1)
                .storeRef(dict)
                .build();
    }

    private static String toHex(byte[] bytes) {
        char[] digits = new char[bytes.length * 2];
        char[] alphabet = "0123456789abcdef".toCharArray();
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            digits[i * 2] = alphabet[value >>> 4];
            digits[i * 2 + 1] = alphabet[value & 0x0F];
        }
        return new String(digits);
    }
}