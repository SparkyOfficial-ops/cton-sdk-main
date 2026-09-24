// BaseWallet.java - базова реалізація кошелька
// Author: Андрій Будильников (Sparky)
// Base wallet implementation
// Базовая реализация кошелька

package com.cton.contract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        return createInternalMessage(destination, amount, comment, true);
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
        Cell body = createMessageBody(comment);
        
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
    
    @Override
    public long getSeqno() throws IOException {
        // Виконуємо get-метод seqno
        // Выполняем get-метод seqno
        // Execute seqno get-method
        JsonObject stack = new JsonObject();
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
        JsonObject stack = new JsonObject();
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
    protected JsonObject runGetMethod(String method, JsonObject stack) throws IOException {
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
        // 1. Отримуємо seqno та параметри підкошелька
        long seqno = getSeqno();
        int subwalletId = getSubwalletId();
        long validUntil = Instant.now().getEpochSecond() + 60; // валідність 1 хвилина
        int sendMode = 3; // PAYABLE_FWD_FEE | DESTROY_ON_ZERO_BALANCE (стандартний простий переказ)
        
        // 2. Внутрішнє повідомлення-платеж (int_msg_info)
        Cell internalMessage = createInternalMessage(destination, amount, comment, true);
        
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
        return reprHashAndDepth(cell).hash;
    }
    
    private static final class HashDepth {
        final byte[] hash;
        final int depth;
        HashDepth(byte[] hash, int depth) {
            this.hash = hash;
            this.depth = depth;
        }
    }
    
    private static HashDepth reprHashAndDepth(Cell cell) {
        int bitSize = cell.getBitSize();
        byte[] data = cell.getData();          // ceil(bitSize/8) байтів (хвости обнулені)
        int refsCount = cell.getRefsCount();
        
        // Дескриптори: d1 = refs (звичайна комірка), d2 = floor(bits/8) + ceil(bits/8)
        int d1 = refsCount & 0x7F;
        int d2 = (bitSize / 8) + ((bitSize + 7) / 8);
        
        // Копія даних із completion tag (біт 1 + нулі до байтової межі), якщо bits % 8 != 0
        byte[] reprData = data.clone();
        if (bitSize % 8 != 0 && reprData.length > 0) {
            int last = reprData.length - 1;
            int mask = 0x80 >> (bitSize % 8);
            reprData[last] = (byte) (reprData[last] | mask);
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(d1);
        out.write(d2);
        out.write(reprData, 0, reprData.length);
        
        int maxRefDepth = 0;
        for (int i = 0; i < refsCount; i++) {
            Cell ref = cell.getRef(i);
            HashDepth rh = reprHashAndDepth(ref);
            out.write((rh.depth >> 8) & 0xFF);
            out.write(rh.depth & 0xFF);
            out.write(rh.hash, 0, rh.hash.length);
            if (rh.depth > maxRefDepth) {
                maxRefDepth = rh.depth;
            }
        }
        
        byte[] hash = sha256(out.toByteArray());
        int depth = (refsCount == 0) ? 0 : (maxRefDepth + 1);
        return new HashDepth(hash, depth);
    }
    
    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}