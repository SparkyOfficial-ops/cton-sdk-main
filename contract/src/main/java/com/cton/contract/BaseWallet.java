// BaseWallet.java - базова реалізація кошелька
// Author: Андрій Будильников (Sparky)
// Base wallet implementation
// Базовая реализация кошелька

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
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
    /**
     * send_mode = PAY_GAS_SEPARATELY (1) | IGNORE_ERRORS (2).
     * Газ списується з балансу гаманця, а value внутрішнього повідомлення доходить до контракту повністю.
     */
    public static final int SEND_MODE_PAY_GAS_SEPARATELY = 3;

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
        // Створюємо тіло внутрішнього повідомлення (op=0x0 + коментар)
        // Build the internal message body (op code + comment)
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Код операції: 0x00000000 = простий переказ з текстовим коментарем
        bodyBuilder.storeUInt(32, 0);
        
        if (comment != null && !comment.isEmpty()) {
            bodyBuilder.storeBytes(comment.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        
        Cell body = bodyBuilder.build();
        
        // Створюємо внутрішнє повідомлення (int_msg_info) згідно з TL-B TON:
        //   int_msg_info$0 ihr_disabled bounce discarded src value
        //     ihr_fee fwd_fee created_lt created_at init body
        return buildInternalMessage(destination, amount, body, true);
    }
    
    /**
     * Побудувати стандартне внутрішнє повідомлення (int_msg_info) за TL-B TON.
     *
     * @param destination адрес отримувача
     * @param amount сума в нанотонах (Value)
     * @param body тіло повідомлення (буде додане як ref)
     * @param bounce чи повідомлення bounceable
     * @return комірка з внутрішнім повідомленням
     */
    protected Cell buildInternalMessage(Address destination, BigInteger amount, Cell body, boolean bounce) {
        CellBuilder b = new CellBuilder();
        b.storeBit(false);          // int_msg_info$0
        b.storeBit(true);           // ihr_disabled = 1
        b.storeBit(bounce);         // bounce
        b.storeBit(false);          // discarded = 0
        b.storeUInt(2, 0);          // src = addr_none
        b.storeAddress(destination);// dst = addr_std
        b.storeCoins(amount);       // value: Grams
        b.storeBit(false);          // extra currencies (ExtraCurrencies$0) = nothing
        b.storeCoins(BigInteger.ZERO); // ihr_fee
        b.storeCoins(BigInteger.ZERO); // fwd_fee
        b.storeUInt(64, 0);         // created_lt
        b.storeUInt(32, 0);         // created_at
        b.storeBit(false);          // init = Nothing
        if (body == null) {
            b.storeBit(false);      // body = Nothing
        } else {
            b.storeBit(true);       // body = ^Cell
            b.storeRef(body);
        }
        return b.build();
    }

    /**
     * Внутрішнє повідомлення з необов'язковим StateInit за посиланням.
     */
    protected Cell buildInternalMessage(Address destination, BigInteger amount, Cell body,
                                        boolean bounce, Cell stateInit) {
        CellBuilder b = new CellBuilder();
        b.storeBit(false);
        b.storeBit(true);
        b.storeBit(bounce);
        b.storeBit(false);
        b.storeUInt(2, 0);
        b.storeAddress(destination);
        b.storeCoins(amount);
        b.storeBit(false);
        b.storeCoins(BigInteger.ZERO);
        b.storeCoins(BigInteger.ZERO);
        b.storeUInt(64, 0);
        b.storeUInt(32, 0);
        if (stateInit == null) {
            b.storeBit(false);
        } else {
            b.storeBit(true);
            b.storeBit(true);
            b.storeRef(stateInit);
        }
        if (body == null) {
            b.storeBit(false);
        } else {
            b.storeBit(true);
            b.storeRef(body);
        }
        return b.build();
    }
    
    /**
     * Час дії транзакції (valid_until). 0 = без обмеження.
     * @return unix timestamp до якого транзакція дійсна
     */
    protected long getValidUntil() {
        return System.currentTimeMillis() / 1000 + 60;
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
    
    @Override
    public long getSeqno() throws IOException {
        // Виконуємо get-метод seqno
        // Выполняем get-метод seqno
        // Execute seqno get-method
        JsonArray stack = new JsonArray();
        JsonObject result = runGetMethod("seqno", stack);
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!result.has("result") || result.get("result").isJsonNull()) {
            throw new IOException("Failed to get seqno");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = result.getAsJsonArray("result");
        if (stackArray.size() < 1) {
            throw new IOException("Invalid seqno format");
        }
        
        // Seqno - перший елемент стеку
        // Seqno - first element of stack
        // Seqno - первый элемент стека
        JsonElement seqnoElement = stackArray.get(0);
        if (!seqnoElement.isJsonArray()) {
            throw new IOException("Invalid seqno data format");
        }
        
        JsonArray seqnoArray = seqnoElement.getAsJsonArray();
        if (seqnoArray.size() < 2) {
            throw new IOException("Invalid seqno array format");
        }
        
        // Другий елемент містить значення
        // Second element contains the value
        // Второй элемент содержит значение
        String seqnoStr = seqnoArray.get(1).getAsString();
        return Long.parseLong(seqnoStr);
    }
    
    @Override
    public int getVersion() throws IOException {
        // Виконуємо get-метод get_version
        // Выполняем get-метод get_version
        // Execute get_version get-method
        JsonArray stack = new JsonArray();
        JsonObject result = runGetMethod("get_version", stack);
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!result.has("result") || result.get("result").isJsonNull()) {
            throw new IOException("Failed to get version");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = result.getAsJsonArray("result");
        if (stackArray.size() < 1) {
            throw new IOException("Invalid version format");
        }
        
        // Версія - перший елемент стеку
        // Version - first element of stack
        // Версия - первый элемент стека
        JsonElement versionElement = stackArray.get(0);
        if (!versionElement.isJsonArray()) {
            throw new IOException("Invalid version data format");
        }
        
        JsonArray versionArray = versionElement.getAsJsonArray();
        if (versionArray.size() < 2) {
            throw new IOException("Invalid version array format");
        }
        
        // Другий елемент містить значення
        // Second element contains the value
        // Второй элемент содержит значение
        String versionStr = versionArray.get(1).getAsString();
        return Integer.parseInt(versionStr);
    }
    
    /**
     * Виконати get-метод смарт-контракту
     * @param method назва методу
     * @param stack параметри методу
     * @return результат виконання методу
     * @throws IOException якщо сталася помилка мережі
     */
    protected JsonObject runGetMethod(String method, JsonArray stack) throws IOException {
        return apiClient.runGetMethod(address.toRaw(), method, stack);
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
        // Внутрішнє повідомлення-ордер. Підпис і зовнішнє повідомлення збирає гаманець.
        Cell orderMessage = createTransfer(destination, amount, comment);
        sendSignedOrder(orderMessage, getSendMode(), privateKey);
    }

    /**
     * Надіслати внутрішнє повідомлення в контракт через цей гаманець (V3 або V4).
     * У мережу йде лише external-in на адресу гаманця. Контракт отримує internal message
     * з {@code body} як пейлоадом; газ у TON списує гаманець.
     */
    @Override
    public void sendInternalMessage(Address destination, BigInteger tonAmount, Cell body,
                                    Crypto.PrivateKey privateKey) throws IOException {
        int version = getVersion();
        if (version != 3 && version != 4) {
            throw new IOException(
                "Jetton, NFT and DeFi operations must be internal messages from Wallet V3 or V4. Wallet version: "
                    + version);
        }
        if (destination == null) {
            throw new IOException("Internal message destination is required");
        }
        if (body == null) {
            throw new IOException("Internal message body is required");
        }
        if (tonAmount == null || tonAmount.signum() <= 0) {
            throw new IOException("Attached TON amount must be positive: the wallet pays gas");
        }
        Cell orderMessage = buildInternalMessage(destination, tonAmount, body, true);
        sendSignedOrder(orderMessage, SEND_MODE_PAY_GAS_SEPARATELY, privateKey);
    }

    /**
     * Підписати кілька внутрішніх повідомлень одним external-in і надіслати BOC у мережу.
     * @return BOC зовнішнього повідомлення у Base64
     */
    public String sendTransfers(List<OutgoingTransfer> transfers, Crypto.PrivateKey privateKey,
                                Long validUntil) throws IOException {
        if (transfers == null || transfers.isEmpty() || transfers.size() > 4) {
            throw new IOException("Wallet external message accepts from 1 to 4 internal messages");
        }
        List<Cell> orders = new ArrayList<>(transfers.size());
        int[] modes = new int[transfers.size()];
        for (int i = 0; i < transfers.size(); i++) {
            OutgoingTransfer transfer = transfers.get(i);
            orders.add(buildInternalMessage(
                transfer.destination, transfer.amount, transfer.body, transfer.bounce, transfer.stateInit));
            modes[i] = transfer.sendMode;
        }
        return signAndSend(orders, modes, privateKey, validUntil == null ? getValidUntil() : validUntil);
    }

    /**
     * Підписати ордер ключем гаманця і надіслати external-in на адресу цього гаманця.
     * Підпис — Ed25519 над representation hash тіла, а не над BOC пейлоада контракту.
     */
    private void sendSignedOrder(Cell orderMessage, int sendMode, Crypto.PrivateKey privateKey) throws IOException {
        signAndSend(java.util.Collections.singletonList(orderMessage), new int[] { sendMode },
            privateKey, getValidUntil());
    }

    private String signAndSend(List<Cell> orders, int[] sendModes, Crypto.PrivateKey privateKey,
                               long validUntil) throws IOException {
        if (privateKey == null) {
            throw new IOException("Private key is required to sign the wallet message");
        }
        long seqno = seqnoForSigning();
        if (seqno == 0 && orders.size() > 2) {
            throw new IOException("The first wallet transaction carries StateInit and at most 2 messages");
        }
        long subwalletId = getSubwalletIdAsLong();
        Cell signingBody = buildWalletBody(subwalletId, validUntil, seqno, sendModes, orders);
        byte[] signature = Crypto.sign(privateKey, signingBody.representationHash());
        Cell externalMessage = buildExternalMessage(
            signature, subwalletId, validUntil, seqno, sendModes, orders, privateKey);
        Boc boc = new Boc(externalMessage);
        byte[] bocBytes = boc.serialize(true, true);
        boc.close();
        apiClient.sendBoc(bocBytes);
        return Base64.getEncoder().encodeToString(bocBytes);
    }
    
    /**
     * Тіло гаманцевої транзакції:
     * subwallet_id(32) + valid_until(32) + seqno(32) + дія + ref(order).
     * Для V3 дія — це send_mode. Для V4 перед ним стоїть opcode 0 (simple send).
     */
    protected Cell buildWalletBody(long subwalletId, long validUntil, long seqno, Cell orderMessage) {
        return buildWalletBody(subwalletId, validUntil, seqno, getSendMode(), orderMessage);
    }

    protected Cell buildWalletBody(long subwalletId, long validUntil, long seqno, int sendMode, Cell orderMessage) {
        CellBuilder b = new CellBuilder();
        b.storeUInt(32, subwalletId);   // subwallet_id
        b.storeUInt(32, validUntil);    // valid_until
        b.storeUInt(32, seqno);         // seqno
        storeOutgoingAction(b, sendMode, orderMessage);
        return b.build();
    }

    protected Cell buildWalletBody(long subwalletId, long validUntil, long seqno,
                                   int[] sendModes, List<Cell> orderMessages) {
        CellBuilder b = new CellBuilder();
        b.storeUInt(32, subwalletId);
        b.storeUInt(32, validUntil);
        b.storeUInt(32, seqno);
        storeOutgoingActions(b, sendModes, orderMessages);
        return b.build();
    }

    /**
     * Дія гаманця після seqno. V3 пише send_mode і референс на внутрішнє повідомлення.
     * Wallet V4 перевизначає цей метод і додає opcode 0.
     */
    protected void storeOutgoingAction(CellBuilder builder, int sendMode, Cell orderMessage) {
        storeOutgoingActions(builder, new int[] { sendMode }, java.util.Collections.singletonList(orderMessage));
    }

    /**
     * Кілька внутрішніх повідомлень. V4 перед списком пише opcode 0.
     * У комірці не більше чотирьох посилань.
     */
    protected void storeOutgoingActions(CellBuilder builder, int[] sendModes, List<Cell> orderMessages) {
        for (int i = 0; i < orderMessages.size(); i++) {
            builder.storeUInt(8, sendModes[i]);
            builder.storeRef(orderMessages.get(i));
        }
    }
    
    /**
     * Режим надсилання внутрішнього повідомлення (send_mode).
     * 6 = PAY_GAS_SEPARATELY | DESTROY_IF_ZERO.
     * @return 8-бітне значення send_mode
     */
    protected int getSendMode() {
        return 6;
    }
    
    /**
     * Зовнішнє in-повідомлення (ext_in_msg_info$10).
     * Якщо seqno == 0, до init:(Maybe (Either StateInit ^StateInit)) пишеться inline StateInit
     * гаманця V3R2 або V4R2, інакше Maybe = Nothing.
     */
    private Cell buildExternalMessage(byte[] signature, long subwalletId, long validUntil, long seqno,
                                      int[] sendModes, List<Cell> orderMessages, Crypto.PrivateKey privateKey) throws IOException {
        CellBuilder b = new CellBuilder();
        b.storeUInt(2, 2);                 // ext_in_msg_info$10
        b.storeUInt(2, 0);                 // src = addr_none
        b.storeAddress(this.address);      // dst = адреса гаманця, не контракту жетона
        b.storeCoins(BigInteger.ZERO);     // import_fee = 0
        if (seqno == 0) {
            int version = getVersion();
            if (version != 3 && version != 4) {
                throw new IOException("First transaction StateInit is implemented for Wallet V3R2 and V4R2");
            }
            Crypto.PublicKey publicKey = privateKey.getPublicKey();
            try {
                StateInit init = StateInit.forWallet(version, subwalletId, publicKey.getData());
                b.storeBit(true);          // Maybe = Just
                b.storeBit(false);         // Either = Left, StateInit inline
                init.store(b);
            } finally {
                publicKey.close();
            }
        } else {
            b.storeBit(false);             // Maybe = Nothing
        }
        b.storeBit(false);                 // body = Either left (inline)
        b.storeBytes(signature);           // signature (512 біт)
        b.storeUInt(32, subwalletId);      // subwallet_id
        b.storeUInt(32, validUntil);       // valid_until
        b.storeUInt(32, seqno);            // seqno
        storeOutgoingActions(b, sendModes, orderMessages);
        return b.build();
    }

    /**
     * Нерозгорнутий рахунок не має методу seqno. Для першої транзакції це seqno 0.
     */
    private long seqnoForSigning() throws IOException {
        if (!isAccountActive()) {
            return 0;
        }
        return getSeqno();
    }

    private boolean isAccountActive() throws IOException {
        JsonObject info = apiClient.getAddressInformation(address.toRaw());
        if (!info.has("result") || !info.get("result").isJsonObject()) {
            return false;
        }
        JsonObject result = info.getAsJsonObject("result");
        if (!result.has("state") || result.get("state").isJsonNull()) {
            return false;
        }
        return "active".equals(result.get("state").getAsString());
    }
    
    private long getSubwalletIdAsLong() {
        try {
            return getSubwalletId() & 0xFFFFFFFFL;
        } catch (Exception e) {
            return 698983191L & 0xFFFFFFFFL;
        }
    }
}