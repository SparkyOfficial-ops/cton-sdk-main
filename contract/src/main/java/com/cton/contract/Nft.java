// Nft.java - інтерфейс для роботи з NFT токенами
// Author: Андрій Будильников (Sparky)
// Interface for working with NFT tokens
// Интерфейс для работы с NFT токенами

package com.cton.contract;

import java.io.IOException;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Інтерфейс для роботи з NFT токенами відповідно до TEP-62
 * 
 * Interface for working with NFT tokens according to TEP-62
 * Интерфейс для работы с NFT токенами согласно TEP-62
 */
public class Nft extends Contract {
    
    /**
     * Конструктор
     * @param address адреса NFT контракту
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public Nft(Address address, TonApiClient apiClient) {
        super(address, apiClient);
    }
    
    /**
     * Отримати інформацію про NFT
     * @return інформація про NFT
     * @throws IOException якщо сталася помилка мережі
     */
    public JsonObject getNftData() throws IOException {
        JsonObject stack = new JsonObject();
        return runGetMethod("get_nft_data", stack);
    }
    
    /**
     * Отримати індекс NFT
     * @return індекс NFT
     * @throws IOException якщо сталася помилка мережі
     */
    public long getIndex() throws IOException {
        JsonObject data = getNftData();
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!data.has("result") || data.get("result").isJsonNull()) {
            throw new IOException("Failed to get NFT data");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = data.getAsJsonArray("result");
        if (stackArray.size() < 1) {
            throw new IOException("Invalid NFT data format");
        }
        
        // Індекс - перший елемент стеку
        // Index - first element of stack
        // Индекс - первый элемент стека
        JsonElement indexElement = stackArray.get(0);
        if (!indexElement.isJsonArray()) {
            throw new IOException("Invalid index data format");
        }
        
        JsonArray indexArray = indexElement.getAsJsonArray();
        if (indexArray.size() < 2) {
            throw new IOException("Invalid index array format");
        }
        
        // Другий елемент містить значення
        // Second element contains the value
        // Второй элемент содержит значение
        String indexStr = indexArray.get(1).getAsString();
        return Long.parseLong(indexStr);
    }
    
    /**
     * Отримати адресу колекції
     * @return адреса колекції
     * @throws IOException якщо сталася помилка мережі
     */
    public Address getCollection() throws IOException {
        JsonObject data = getNftData();
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!data.has("result") || data.get("result").isJsonNull()) {
            throw new IOException("Failed to get NFT data");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = data.getAsJsonArray("result");
        if (stackArray.size() < 3) {
            throw new IOException("Invalid NFT data format");
        }
        
        // Адреса колекції - третій елемент стеку
        // Collection address - third element of stack
        // Адрес коллекции - третий элемент стека
        JsonElement collectionElement = stackArray.get(2);
        if (!collectionElement.isJsonArray()) {
            throw new IOException("Invalid collection data format");
        }
        
        JsonArray collectionArray = collectionElement.getAsJsonArray();
        if (collectionArray.size() < 2) {
            throw new IOException("Invalid collection array format");
        }
        
        // Другий елемент містить адресу
        // Second element contains the address
        // Второй элемент содержит адрес
        String addressStr = collectionArray.get(1).getAsString();
        return new Address(addressStr);
    }
    
    /**
     * Отримати власника NFT
     * @return адреса власника
     * @throws IOException якщо сталася помилка мережі
     */
    public Address getOwner() throws IOException {
        JsonObject data = getNftData();
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!data.has("result") || data.get("result").isJsonNull()) {
            throw new IOException("Failed to get NFT data");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = data.getAsJsonArray("result");
        if (stackArray.size() < 2) {
            throw new IOException("Invalid NFT data format");
        }
        
        // Адреса власника - другий елемент стеку
        // Owner address - second element of stack
        // Адрес владельца - второй элемент стека
        JsonElement ownerElement = stackArray.get(1);
        if (!ownerElement.isJsonArray()) {
            throw new IOException("Invalid owner data format");
        }
        
        JsonArray ownerArray = ownerElement.getAsJsonArray();
        if (ownerArray.size() < 2) {
            throw new IOException("Invalid owner array format");
        }
        
        // Другий елемент містить адресу
        // Second element contains the address
        // Второй элемент содержит адрес
        String addressStr = ownerArray.get(1).getAsString();
        return new Address(addressStr);
    }
    
    /**
     * Отримати контент NFT
     * @return контент NFT у вигляді комірки
     * @throws IOException якщо сталася помилка мережі
     */
    public Cell getContent() throws IOException {
        JsonObject data = getNftData();
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!data.has("result") || data.get("result").isJsonNull()) {
            throw new IOException("Failed to get NFT data");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = data.getAsJsonArray("result");
        if (stackArray.size() < 4) {
            throw new IOException("Invalid NFT data format");
        }
        
        // Контент - четвертий елемент стеку
        // Content - fourth element of stack
        // Контент - четвертый элемент стека
        JsonElement contentElement = stackArray.get(3);
        if (!contentElement.isJsonArray()) {
            throw new IOException("Invalid content data format");
        }
        
        JsonArray contentArray = contentElement.getAsJsonArray();
        if (contentArray.size() < 2) {
            throw new IOException("Invalid content array format");
        }
        
        // Другий елемент містить дані контенту
        // Second element contains content data
        // Второй элемент содержит данные контента
        String contentStr = contentArray.get(1).getAsString();
        
        // Для простоти, повертаємо порожню комірку
        // Для простоты, возвращаем пустую ячейку
        // For simplicity, return empty cell
        return new com.cton.sdk.CellBuilder().build();
    }
}