// Jetton.java - інтерфейс для роботи з Jetton токенами
// Author: Андрій Будильников (Sparky)
// Interface for working with Jetton tokens
// Интерфейс для работы с Jetton токенами

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;

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
 * Інтерфейс для роботи з Jetton токенами відповідно до TEP-74
 * 
 * Interface for working with Jetton tokens according to TEP-74
 * Интерфейс для работы с Jetton токенами согласно TEP-74
 */
public class Jetton extends Contract {
    
    /**
     * Конструктор
     * @param address адреса контракту токена
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public Jetton(Address address, TonApiClient apiClient) {
        super(address, apiClient);
    }
    
    /**
     * Отримати загальну емісію токенів
     * @return загальна емісія
     * @throws IOException якщо сталася помилка мережі
     */
    public BigInteger getTotalSupply() throws IOException {
        JsonObject stack = new JsonObject();
        JsonObject result = runGetMethod("get_jetton_data", stack);
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!result.has("result") || result.get("result").isJsonNull()) {
            throw new IOException("Failed to get jetton data");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = result.getAsJsonArray("result");
        if (stackArray.size() < 4) {
            throw new IOException("Invalid jetton data format");
        }
        
        // Загальна емісія - перший елемент стеку
        // Total supply - first element of stack
        // Общая эмиссия - первый элемент стека
        JsonElement supplyElement = stackArray.get(0);
        if (!supplyElement.isJsonArray()) {
            throw new IOException("Invalid supply data format");
        }
        
        JsonArray supplyArray = supplyElement.getAsJsonArray();
        if (supplyArray.size() < 2) {
            throw new IOException("Invalid supply array format");
        }
        
        // Другий елемент містить значення
        // Second element contains the value
        // Второй элемент содержит значение
        String supplyStr = supplyArray.get(1).getAsString();
        return new BigInteger(supplyStr);
    }
    
    /**
     * Отримати адресу minter контракту
     * @return адреса minter контракту
     * @throws IOException якщо сталася помилка мережі
     */
    public Address getJettonMinter() throws IOException {
        JsonObject stack = new JsonObject();
        JsonObject result = runGetMethod("get_jetton_data", stack);
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!result.has("result") || result.get("result").isJsonNull()) {
            throw new IOException("Failed to get jetton data");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = result.getAsJsonArray("result");
        if (stackArray.size() < 4) {
            throw new IOException("Invalid jetton data format");
        }
        
        // Адреса minter - третій елемент стеку
        // Minter address - third element of stack
        // Адрес minter - третий элемент стека
        JsonElement minterElement = stackArray.get(2);
        if (!minterElement.isJsonArray()) {
            throw new IOException("Invalid minter data format");
        }
        
        JsonArray minterArray = minterElement.getAsJsonArray();
        if (minterArray.size() < 2) {
            throw new IOException("Invalid minter array format");
        }
        
        // Другий елемент містить адресу
        // Second element contains the address
        // Второй элемент содержит адрес
        String addressStr = minterArray.get(1).getAsString();
        return new Address(addressStr);
    }
    
    /**
     * Отримати баланс користувача
     * @param owner адреса власника
     * @return баланс користувача
     * @throws IOException якщо сталася помилка мережі
     */
    public BigInteger getBalance(Address owner) throws IOException {
        JsonObject stack = new JsonObject();
        // Додаємо адресу власника до стеку
        // Добавляем адрес владельца в стек
        // Add owner address to stack
        JsonArray ownerArray = new JsonArray();
        ownerArray.add("tvm.Slice");
        ownerArray.add(owner.toRaw());
        stack.add("address", ownerArray);
        
        JsonObject result = runGetMethod("get_wallet_data", stack);
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!result.has("result") || result.get("result").isJsonNull()) {
            throw new IOException("Failed to get wallet data");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = result.getAsJsonArray("result");
        if (stackArray.size() < 1) {
            throw new IOException("Invalid wallet data format");
        }
        
        // Баланс - перший елемент стеку
        // Balance - first element of stack
        // Баланс - первый элемент стека
        JsonElement balanceElement = stackArray.get(0);
        if (!balanceElement.isJsonArray()) {
            throw new IOException("Invalid balance data format");
        }
        
        JsonArray balanceArray = balanceElement.getAsJsonArray();
        if (balanceArray.size() < 2) {
            throw new IOException("Invalid balance array format");
        }
        
        // Другий елемент містить значення
        // Second element contains the value
        // Второй элемент содержит значение
        String balanceStr = balanceArray.get(1).getAsString();
        return new BigInteger(balanceStr);
    }
    
    /**
     * Отримати адресу кошелька токенів для користувача
     * @param owner адреса власника
     * @return адреса кошелька токенів
     * @throws IOException якщо сталася помилка мережі
     */
    public Address getWalletAddress(Address owner) throws IOException {
        JsonObject stack = new JsonObject();
        // Додаємо адресу власника до стеку
        // Добавляем адрес владельца в стек
        // Add owner address to stack
        JsonArray ownerArray = new JsonArray();
        ownerArray.add("tvm.Slice");
        ownerArray.add(owner.toRaw());
        stack.add("address", ownerArray);
        
        JsonObject result = runGetMethod("get_wallet_address", stack);
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!result.has("result") || result.get("result").isJsonNull()) {
            throw new IOException("Failed to get wallet address");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = result.getAsJsonArray("result");
        if (stackArray.size() < 1) {
            throw new IOException("Invalid wallet address format");
        }
        
        // Адреса кошелька - перший елемент стеку
        // Wallet address - first element of stack
        // Адрес кошелька - первый элемент стека
        JsonElement walletElement = stackArray.get(0);
        if (!walletElement.isJsonArray()) {
            throw new IOException("Invalid wallet address data format");
        }
        
        JsonArray walletArray = walletElement.getAsJsonArray();
        if (walletArray.size() < 2) {
            throw new IOException("Invalid wallet address array format");
        }
        
        // Другий елемент містить адресу
        // Second element contains the address
        // Второй элемент содержит адрес
        String addressStr = walletArray.get(1).getAsString();
        return new Address(addressStr);
    }
    
    /**
     * Transfer jettons to another address
     * @param fromWalletAddress адреса кошелька відправника
     * @param toAddress адреса одержувача
     * @param amount кількість токенів
     * @param forwardAmount сума для пересилання разом з повідомленням
     * @param comment коментар до переказу
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void transfer(Address fromWalletAddress, Address toAddress, BigInteger amount, 
                        BigInteger forwardAmount, String comment, Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо комірку з тілом повідомлення для переказу jetton
        // Создаем ячейку с телом сообщения для перевода jetton
        // Create cell with message body for jetton transfer
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо opcode для transfer (0xf8a7ea5)
        // Добавляем opcode для transfer (0xf8a7ea5)
        // Add opcode for transfer (0xf8a7ea5)
        bodyBuilder.storeUInt(32, 0xf8a7ea5);
        
        // Додаємо query_id (0 для простоти)
        // Добавляем query_id (0 для простоты)
        // Add query_id (0 for simplicity)
        bodyBuilder.storeUInt(64, 0);
        
        // Додаємо кількість токенів
        // Добавляем количество токенов
        // Add token amount
        bodyBuilder.storeCoins(amount);
        
        // Додаємо адресу одержувача
        // Добавляем адрес получателя
        // Add recipient address
        bodyBuilder.storeAddress(toAddress);
        
        // Додаємо адресу відправника (response destination)
        // Добавляем адрес отправителя (response destination)
        // Add sender address (response destination)
        bodyBuilder.storeAddress(fromWalletAddress);
        
        // Додаємо прапор custom_payload (0 - немає)
        // Добавляем флаг custom_payload (0 - нет)
        // Add custom_payload flag (0 - none)
        bodyBuilder.storeUInt(1, 0); // Використовуємо storeUInt(1, 0) замість storeBit(false)
        
        // Додаємо forward_amount
        // Добавляем forward_amount
        // Add forward_amount
        bodyBuilder.storeCoins(forwardAmount);
        
        // Додаємо прапор forward_payload (0 - немає)
        // Добавляем флаг forward_payload (0 - нет)
        // Add forward_payload flag (0 - none)
        bodyBuilder.storeUInt(1, 0); // Використовуємо storeUInt(1, 0) замість storeBit(false)
        
        // Якщо є коментар, додаємо його
        // Если есть комментарий, добавляем его
        // If there's a comment, add it
        if (comment != null && !comment.isEmpty()) {
            CellBuilder commentBuilder = new CellBuilder();
            commentBuilder.storeUInt(32, 0); // opcode для коментаря
            commentBuilder.storeBytes(comment.getBytes());
            bodyBuilder.storeRef(commentBuilder.build());
        }
        
        Cell body = bodyBuilder.build();
        
        // Створюємо зовнішнє повідомлення
        // Создаем внешнее сообщение
        // Create external message
        CellBuilder messageBuilder = new CellBuilder();
        
        // Створюємо BOC для повідомлення
        // Создаем BOC для сообщения
        // Create BOC for message
        Boc boc = new Boc(body);
        byte[] messageBytes = boc.serialize(true, true);
        
        // Підписуємо повідомлення приватним ключем
        // Подписываем сообщение приватным ключом
        // Sign message with private key
        byte[] signature = Crypto.sign(privateKey, messageBytes);
        
        // Створюємо зовнішнє повідомлення з підписом
        // Создаем внешнее сообщение с подписью
        // Create external message with signature
        messageBuilder.storeBytes(signature);
        messageBuilder.storeRef(body);
        
        Cell message = messageBuilder.build();
        
        // Надсилаємо повідомлення через API
        // Отправляем сообщение через API
        // Send message through API
        if (apiClient != null) {
            apiClient.sendBoc(messageBytes);
        } else {
            throw new IOException("API client not set");
        }
    }
}