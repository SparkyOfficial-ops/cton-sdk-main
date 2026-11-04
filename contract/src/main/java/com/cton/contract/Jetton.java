// Jetton.java - інтерфейс для роботи з Jetton токенами
// Author: Андрій Будильников (Sparky)
// Interface for working with Jetton tokens
// Интерфейс для работы с Jetton токенами

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
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
     * Internal message the owner's TON wallet must send to the owner's jetton wallet.
     * The destination is resolved with {@code get_wallet_address} on this minter.
     * {@code tonAmount} is the TON attached to that internal message.
     */
    public Cell transfer(Address owner, Address recipient, BigInteger jettonAmount,
                         BigInteger tonAmount, BigInteger forwardAmount, String comment) throws IOException {
        return JettonWallet.ofOwner(this, owner).createInternalTransfer(
                owner, recipient, jettonAmount, tonAmount, forwardAmount, comment, true);
    }
}