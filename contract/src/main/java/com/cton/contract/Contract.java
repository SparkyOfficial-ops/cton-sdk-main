// Contract.java - базовий клас для контрактів
// Author: Андрій Будильников (Sparky)
// Base contract class
// Базовый класс контрактов

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;
import com.google.gson.JsonObject;

/**
 * Базовий клас для роботи з TON контрактами
 * 
 * Base class for working with TON contracts
 * Базовый класс для работы с TON контрактами
 */
public abstract class Contract {
    protected final Address address;
    protected final TonApiClient apiClient;
    
    /**
     * Конструктор
     * @param address адреса контракту
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public Contract(Address address, TonApiClient apiClient) {
        this.address = address;
        this.apiClient = apiClient;
    }
    
    /**
     * Отримати адресу контракту
     * @return адреса контракту
     */
    public Address getAddress() {
        return this.address;
    }
    
    /**
     * Отримати баланс контракту
     * @return баланс у нанотоні
     * @throws IOException якщо сталася помилка мережі
     */
    public BigInteger getBalance() throws IOException {
        JsonObject addressInfo = apiClient.getAddressInformation(address.toRaw());
        
        if (!addressInfo.has("result") || addressInfo.get("result").isJsonNull()) {
            throw new IOException("Failed to get address information");
        }
        
        JsonObject result = addressInfo.getAsJsonObject("result");
        
        if (result.has("balance")) {
            return new BigInteger(result.get("balance").getAsString());
        } else {
            throw new IOException("Balance not found in response");
        }
    }
    
    /**
     * Виконати get-метод контракту
     * @param method назва методу
     * @param stack параметри методу
     * @return результат виконання методу
     * @throws IOException якщо сталася помилка мережі
     */
    protected JsonObject runGetMethod(String method, JsonObject stack) throws IOException {
        return apiClient.runGetMethod(address.toRaw(), method, stack);
    }
    
    /**
     * Надіслати повідомлення до контракту
     * @param message комірка з повідомленням
     * @throws IOException якщо сталася помилка мережі
     */
    public void sendMessage(Cell message) throws IOException {
        // Створюємо BOC з повідомлення
        com.cton.sdk.Boc boc = new com.cton.sdk.Boc(message);
        byte[] bocBytes = boc.serialize(true, true);
        
        // Надсилаємо через API
        apiClient.sendBoc(bocBytes);
    }
    
    /**
     * Створити повідомлення для виклику методу контракту
     * @param methodId ідентифікатор методу
     * @param params параметри методу
     * @return комірка з повідомленням
     */
    public Cell createMethodCallMessage(int methodId, Object... params) {
        CellBuilder builder = new CellBuilder();
        
        // Додаємо ідентифікатор методу
        builder.storeUInt(32, methodId);
        
        // Додаємо параметри (спрощена реалізація)
        for (Object param : params) {
            if (param instanceof BigInteger) {
                builder.storeUInt(256, ((BigInteger) param).longValue());
            } else if (param instanceof String) {
                builder.storeBytes(((String) param).getBytes());
            } else if (param instanceof Address) {
                builder.storeAddress((Address) param);
            }
        }
        
        return builder.build();
    }
}