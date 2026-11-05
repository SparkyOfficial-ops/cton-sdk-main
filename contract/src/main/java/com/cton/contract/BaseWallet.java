// BaseWallet.java - базова реалізація кошелька
// Author: Андрій Будильников (Sparky)
// Base wallet implementation
// Базовая реализация кошелька

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Boc;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;
import com.cton.sdk.Crypto;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Базова реалізація кошелька для TON блокчейну
 * 
 * Цей клас надає основну функціональність для роботи з TON кошельками:
 * - Отримання балансу адреси
 * - Створення транзакцій
 * - Відправка повідомлень
 * - Робота з get-методами смарт-контрактів
 * 
 * Base wallet implementation for TON blockchain
 * 
 * This class provides core functionality for working with TON wallets:
 * - Getting address balance
 * - Creating transactions
 * - Sending messages
 * - Working with smart contract get-methods
 * 
 * Базовая реализация кошелька для TON блокчейна
 * 
 * Этот класс предоставляет основной функционал для работы с TON кошельками:
 * - Получение баланса адреса
 * - Создание транзакций
 * - Отправка сообщений
 * - Работа с get-методами смарт-контрактов
 */
public abstract class BaseWallet implements Wallet {
    /** Pay transfer fees from the wallet balance, not from the message value. */
    public static final int SEND_MODE_PAY_GAS_SEPARATELY = 1;
    public static final int SEND_MODE_IGNORE_ERRORS = 2;
    public static final int SEND_MODE_CARRY_ALL_REMAINING_BALANCE = 128;

    protected final Address address;
    protected final TonApiClient apiClient;
    
    /**
     * Конструктор
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public BaseWallet(Address address, TonApiClient apiClient) {
        this.address = address;
        this.apiClient = apiClient;
    }
    
    @Override
    public Address getAddress() {
        return this.address;
    }
    
    @Override
    public BigInteger getBalance() throws IOException {
        // Отримуємо інформацію про адресу через API
        // Получаем информацию об адресе через API
        // Get address information through API
        JsonObject addressInfo = apiClient.getAddressInformation(address.toRaw());
        
        // Перевіряємо чи запит був успішним
        // Проверяем, был ли запрос успешным
        // Check if request was successful
        if (!addressInfo.has("result") || addressInfo.get("result").isJsonNull()) {
            throw new IOException("Failed to get address information");
        }
        
        JsonObject result = addressInfo.getAsJsonObject("result");
        
        // Отримуємо баланс
        // Получаем баланс
        // Get balance
        if (result.has("balance")) {
            return new BigInteger(result.get("balance").getAsString());
        } else {
            throw new IOException("Balance not found in response");
        }
    }
    
    @Override
    public Cell createTransfer(Address destination, BigInteger amount, String comment) throws IOException {
        // Повертаємо стандартне внутрішнє повідомлення (TL-B int_msg_info$0),
        // яке кошелек перешле одержувачу.
        return createInternalMessage(destination, amount, comment, destination.isBounceable());
    }
    
    /**
     * Тіло повідомлення: код операції (0x00000000 = текстовий коментар) + байти коментаря.
     */
    protected Cell createMessageBody(String comment) {
        CellBuilder bodyBuilder = new CellBuilder();
        bodyBuilder.storeUInt(32, 0); // op = 0x00000000 (text comment)
        if (comment != null && !comment.isEmpty()) {
            bodyBuilder.storeBytes(comment.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return bodyBuilder.build();
    }
    
    /**
     * Створює валідне внутрішнє повідомлення (TL-B int_msg_info$0) від адреси
     *шелька до одержувача.
     */
    protected Cell createInternalMessage(Address destination, BigInteger amount, String comment, boolean bounce) {
        return createInternalMessage(destination, amount, createMessageBody(comment), bounce);
    }

    protected Cell createInternalMessage(Address destination, BigInteger amount, Cell body, boolean bounce) {
        CellBuilder msg = new CellBuilder();
        msg.storeUInt(1, 0);                       // int_msg_info$0
        msg.storeUInt(1, 1);                       // ihr_disabled = true
        msg.storeUInt(1, bounce ? 1 : 0);          // bounce
        msg.storeUInt(1, 0);                       // bounced = false
        msg.storeAddress(this.address);            // src (адресашелька)
        msg.storeAddress(destination);             // dst
        msg.storeCoins(amount);                    // value (Grams)
        msg.storeUInt(1, 0);                       // extra currencies: порожній dict
        msg.storeCoins(BigInteger.ZERO);           // ihr_fee
        msg.storeCoins(BigInteger.ZERO);           // fwd_fee
        msg.storeUInt(64, 0);                      // created_lt
        msg.storeUInt(32, Instant.now().getEpochSecond()); // created_at
        msg.storeUInt(1, 0);                       // init: відсутній StateInit
        msg.storeUInt(1, 1);                       // body: у референсі
        msg.storeRef(body);
        return msg.build();
    }
    
    @Override
    public void sendTransaction(Cell transaction) throws IOException {
        // Серіалізуємо транзакцію в BOC
        // Сериализуем транзакцию в BOC
        // Serialize transaction to BOC
        Boc boc = new Boc(transaction);
        byte[] bocBytes = boc.serialize(true, true);
        
        // Конвертуємо в Base64
        // Конвертируем в Base64
        // Convert to Base64
        String bocBase64 = Base64.getEncoder().encodeToString(bocBytes);
        
        // Надсилаємо через API
        // Отправляем через API
        // Send through API
        apiClient.sendBoc(bocBytes);
    }

    /**
     * Hash returned by Toncenter {@code sendBoc}. It is base64 of the external message cell hash.
     */
    public static String messageHash(JsonObject response) throws IOException {
        if (response == null) {
            throw new IOException("sendBoc returned an empty response");
        }
        if (response.has("ok") && !response.get("ok").isJsonNull() && !response.get("ok").getAsBoolean()) {
            throw new IOException("sendBoc rejected the message");
        }
        JsonObject result = response;
        if (response.has("result") && response.get("result").isJsonObject()) {
            result = response.getAsJsonObject("result");
        }
        if (!result.has("hash") || result.get("hash").isJsonNull()) {
            throw new IOException("sendBoc response has no message hash");
        }
        String hash = result.get("hash").getAsString();
        if (hash == null || hash.isBlank()) {
            throw new IOException("sendBoc response has no message hash");
        }
        return hash;
    }
    
    @Override
    public long getSeqno() throws IOException {
        return readStackInteger(runGetMethod("seqno", new JsonArray()));
    }
    
    @Override
    public int getVersion() throws IOException {
        return (int) readStackInteger(runGetMethod("get_version", new JsonArray()));
    }
    
    /**
     * Виконати get-метод смарт-контракту
     * @param method назва методу
     * @param stack параметри методу
     * @return результат виконання методу
     * @throws IOException якщо сталася помилка мережі
     */
    protected JsonObject runGetMethod(String method, JsonObject stack) throws IOException {
        return apiClient.runGetMethod(address.toRaw(), method, stack);
    }

    protected JsonObject runGetMethod(String method, JsonArray stack) throws IOException {
        return apiClient.runGetMethod(address.toRaw(), method, stack);
    }

    private static long readStackInteger(JsonObject response) throws IOException {
        if (response == null || !response.has("ok") || response.get("ok").isJsonNull()
                || !response.get("ok").getAsBoolean() || !response.has("result") || response.get("result").isJsonNull()) {
            throw new IOException("get method failed: " + response);
        }
        JsonElement resultElement = response.get("result");
        JsonArray stack;
        if (resultElement.isJsonObject()) {
            JsonObject result = resultElement.getAsJsonObject();
            if (result.has("exit_code") && result.get("exit_code").getAsInt() != 0) {
                throw new IOException("get method exit_code " + result.get("exit_code") + ": " + response);
            }
            if (!result.has("stack") || !result.get("stack").isJsonArray()) {
                throw new IOException("get method returned no stack: " + response);
            }
            stack = result.getAsJsonArray("stack");
        } else if (resultElement.isJsonArray()) {
            stack = resultElement.getAsJsonArray();
        } else {
            throw new IOException("get method returned no stack: " + response);
        }
        if (stack.size() < 1 || !stack.get(0).isJsonArray() || stack.get(0).getAsJsonArray().size() < 2) {
            throw new IOException("get method stack is empty: " + response);
        }
        return Long.decode(stack.get(0).getAsJsonArray().get(1).getAsString());
    }
    
    /**
     * Transfer funds to another address
     * @param destination адреса одержувача
     * @param amount сума для переказу
     * @param comment коментар до переказу
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void transfer(Address destination, BigInteger amount, String comment, Crypto.PrivateKey privateKey) throws IOException {
        // 1. Отримуємо seqno та параметри підкошелька
        long seqno = getSeqno();
        int subwalletId = getSubwalletId();
        long validUntil = Instant.now().getEpochSecond() + 60; // валідність 1 хвилина
        int sendMode = 3; // PAYABLE_FWD_FEE | DESTROY_ON_ZERO_BALANCE (стандартний простий переказ)
        
        // 2. Внутрішнє повідомлення-платеж (int_msg_info)
        Cell internalMessage = createInternalMessage(destination, amount, comment, destination.isBounceable());
        
        // 3. Тіло для підпису:
        //    signing_body = subwallet_id(32) + valid_until(32) + seqno(32) + send_mode(8) + [Order Message]
        Cell signingBody = buildSigningBody(subwalletId, validUntil, seqno, sendMode, internalMessage);
        
        // 4. signature = Ed25519( representation_hash(signing_body) )
        byte[] signingHash = cellRepresentationHash(signingBody);
        byte[] signature = Crypto.sign(privateKey, signingHash);
        
        // 5. Тіло зовнішнього повідомлення: signature(512 bits) + signing_body
        Cell externalBody = buildSignedExternalBody(signature, subwalletId, validUntil, seqno, sendMode, internalMessage);
        
        // 6. Гортаємо в ext_in_msg_info$10 (зовнішнє in-повідомлення до контракту кошелька)
        Cell externalMessage = createExternalInMessage(externalBody);
        
        // 7. Надсилаємо через API
        sendTransaction(externalMessage);
    }

    /**
     * Несколько внутренних сообщений одним внешним сообщением кошелька.
     * V4 добавляет opcode 0. Возвращает base64 BoC отправленного сообщения.
     */
    public String sendTransfers(List<OutgoingTransfer> transfers, Crypto.PrivateKey privateKey, Long validUntil)
            throws IOException {
        if (transfers == null || transfers.isEmpty() || transfers.size() > 4) {
            throw new IOException("A wallet message contains from 1 to 4 transfers");
        }
        long seqno = getSeqno();
        int subwalletId = getSubwalletId();
        long until = validUntil != null ? validUntil : Instant.now().getEpochSecond() + 60;
        boolean walletV4 = getVersion() == 4;
        List<Cell> messages = new ArrayList<>();
        for (OutgoingTransfer transfer : transfers) {
            messages.add(internalMessage(transfer));
        }
        Cell signingBody = signingBody(subwalletId, until, seqno, messages, transfers, walletV4);
        byte[] signature = Crypto.sign(privateKey, cellRepresentationHash(signingBody));
        Cell externalBody = signedBody(signature, subwalletId, until, seqno, messages, transfers, walletV4);
        Cell externalMessage = createExternalInMessage(externalBody);
        sendTransaction(externalMessage);
        try (Boc boc = new Boc(externalMessage)) {
            return Base64.getEncoder().encodeToString(boc.serialize(true, true));
        }
    }

    private Cell internalMessage(OutgoingTransfer transfer) {
        CellBuilder msg = new CellBuilder();
        msg.storeUInt(1, 0);
        msg.storeUInt(1, 1);
        msg.storeUInt(1, transfer.bounce ? 1 : 0);
        msg.storeUInt(1, 0);
        msg.storeAddress(this.address);
        msg.storeAddress(transfer.destination);
        msg.storeCoins(transfer.amount);
        msg.storeUInt(1, 0);
        msg.storeCoins(BigInteger.ZERO);
        msg.storeCoins(BigInteger.ZERO);
        msg.storeUInt(64, 0);
        msg.storeUInt(32, Instant.now().getEpochSecond());
        if (transfer.stateInit == null) {
            msg.storeUInt(1, 0);
        } else {
            msg.storeUInt(1, 1);
            msg.storeUInt(1, 1);
            msg.storeRef(transfer.stateInit);
        }
        if (transfer.body == null) {
            msg.storeUInt(1, 0);
        } else {
            msg.storeUInt(1, 1);
            msg.storeRef(transfer.body);
        }
        return msg.build();
    }

    private Cell signingBody(int subwalletId, long validUntil, long seqno, List<Cell> messages,
                             List<OutgoingTransfer> transfers, boolean walletV4) {
        CellBuilder builder = new CellBuilder();
        builder.storeUInt(32, subwalletId & 0xFFFFFFFFL);
        builder.storeUInt(32, validUntil);
        builder.storeUInt(32, seqno);
        if (walletV4) {
            builder.storeUInt(8, 0);
        }
        for (int i = 0; i < messages.size(); i++) {
            builder.storeUInt(8, transfers.get(i).sendMode);
            builder.storeRef(messages.get(i));
        }
        return builder.build();
    }

    private Cell signedBody(byte[] signature, int subwalletId, long validUntil, long seqno, List<Cell> messages,
                            List<OutgoingTransfer> transfers, boolean walletV4) {
        CellBuilder builder = new CellBuilder();
        builder.storeBytes(signature);
        builder.storeUInt(32, subwalletId & 0xFFFFFFFFL);
        builder.storeUInt(32, validUntil);
        builder.storeUInt(32, seqno);
        if (walletV4) {
            builder.storeUInt(8, 0);
        }
        for (int i = 0; i < messages.size(); i++) {
            builder.storeUInt(8, transfers.get(i).sendMode);
            builder.storeRef(messages.get(i));
        }
        return builder.build();
    }
    
    /**
     * Тіло, що підписується (Wallet V3/V4): subwallet_id + valid_until + seqno + send_mode + [Message].
     */
    protected Cell buildSigningBody(int subwalletId, long validUntil, long seqno, int sendMode, Cell internalMessage) {
        CellBuilder b = new CellBuilder();
        b.storeUInt(32, subwalletId & 0xFFFFFFFFL);
        b.storeUInt(32, validUntil);
        b.storeUInt(32, seqno);
        b.storeUInt(8, sendMode);
        b.storeRef(internalMessage);
        return b.build();
    }
    
    /**
     * Тіло зовнішнього повідомлення = підпис (64 байти = 512 біт) + той самий вміст signing_body.
     */
    protected Cell buildSignedExternalBody(byte[] signature, int subwalletId, long validUntil,
                                           long seqno, int sendMode, Cell internalMessage) {
        CellBuilder b = new CellBuilder();
        b.storeBytes(signature);                    // 512 біт підпису
        b.storeUInt(32, subwalletId & 0xFFFFFFFFL);
        b.storeUInt(32, validUntil);
        b.storeUInt(32, seqno);
        b.storeUInt(8, sendMode);
        b.storeRef(internalMessage);
        return b.build();
    }
    
    /**
     * Зовнішнє in-повідомлення (ext_in_msg_info$10) до адреси самого кошелька.
     */
    protected Cell createExternalInMessage(Cell body) {
        CellBuilder msg = new CellBuilder();
        msg.storeUInt(2, 2);              // ext_in_msg_info$10
        msg.storeUInt(2, 0);             // src = addr_none$00
        msg.storeAddress(this.address);  // dst = адреса кошелька
        msg.storeCoins(BigInteger.ZERO); // import_fee = 0
        msg.storeUInt(1, 0);             // init: відсутній StateInit
        msg.storeUInt(1, 1);             // body: у референсі
        msg.storeRef(body);
        return msg.build();
    }
    
    /**
     * Обчислює representation hash комірки (SHA-256 від стандартного представлення комірки:
     * d1 + d2 + дані(з completion tag) + для кожного референсу 2-байтна глибина + 32-байтний хеш).
     */
    protected static byte[] cellRepresentationHash(Cell cell) {
        return cell.representationHash();
    }
}