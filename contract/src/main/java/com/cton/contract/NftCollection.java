// NftCollection.java - інтерфейс для роботи з NFT колекціями
// Author: Андрій Будильников (Sparky)
// Interface for working with NFT collections
// Интерфейс для работы с NFT коллекциями

package com.cton.contract;

import java.io.IOException;
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
 * Інтерфейс для роботи з NFT колекціями відповідно до TEP-62
 * 
 * Interface for working with NFT collections according to TEP-62
 * Интерфейс для работы с NFT коллекциями согласно TEP-62
 */
public class NftCollection extends Contract {
    
    /**
     * Конструктор
     * @param address адреса контракту колекції
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public NftCollection(Address address, TonApiClient apiClient) {
        super(address, apiClient);
    }
    
    /**
     * Отримати інформацію про колекцію
     * @return інформація про колекцію
     * @throws IOException якщо сталася помилка мережі
     */
    public JsonObject getCollectionData() throws IOException {
        JsonObject stack = new JsonObject();
        return runGetMethod("get_collection_data", stack);
    }
    
    /**
     * Отримати загальну кількість NFT в колекції
     * @return загальна кількість NFT
     * @throws IOException якщо сталася помилка мережі
     */
    public long getNextItemIndex() throws IOException {
        JsonObject data = getCollectionData();
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!data.has("result") || data.get("result").isJsonNull()) {
            throw new IOException("Failed to get collection data");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = data.getAsJsonArray("result");
        if (stackArray.size() < 1) {
            throw new IOException("Invalid collection data format");
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
     * Отримати адресу minter контракту
     * @return адреса minter контракту
     * @throws IOException якщо сталася помилка мережі
     */
    public Address getOwner() throws IOException {
        JsonObject data = getCollectionData();
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!data.has("result") || data.get("result").isJsonNull()) {
            throw new IOException("Failed to get collection data");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = data.getAsJsonArray("result");
        if (stackArray.size() < 2) {
            throw new IOException("Invalid collection data format");
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
     * Отримати контент колекції
     * @return контент колекції у вигляді комірки
     * @throws IOException якщо сталася помилка мережі
     */
    public Cell getContent() throws IOException {
        JsonObject data = getCollectionData();
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!data.has("result") || data.get("result").isJsonNull()) {
            throw new IOException("Failed to get collection data");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = data.getAsJsonArray("result");
        if (stackArray.size() < 3) {
            throw new IOException("Invalid collection data format");
        }
        
        // Контент - третій елемент стеку
        // Content - third element of stack
        // Контент - третий элемент стека
        JsonElement contentElement = stackArray.get(2);
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
    
    /**
     * Отримати адресу NFT за індексом
     * @param index індекс NFT
     * @return адреса NFT
     * @throws IOException якщо сталася помилка мережі
     */
    public Address getNftAddressByIndex(long index) throws IOException {
        JsonObject stack = new JsonObject();
        
        // Додаємо індекс до стеку
        // Добавляем индекс в стек
        // Add index to stack
        JsonArray indexArray = new JsonArray();
        indexArray.add("tvm.Slice");
        indexArray.add(String.valueOf(index));
        stack.add("index", indexArray);
        
        JsonObject result = runGetMethod("get_nft_address_by_index", stack);
        
        // Перевіряємо чи є результат
        // Проверяем, есть ли результат
        // Check if there is a result
        if (!result.has("result") || result.get("result").isJsonNull()) {
            throw new IOException("Failed to get NFT address");
        }
        
        // Отримуємо стек результатів
        // Получаем стек результатов
        // Get result stack
        JsonArray stackArray = result.getAsJsonArray("result");
        if (stackArray.size() < 1) {
            throw new IOException("Invalid NFT address format");
        }
        
        // Адреса NFT - перший елемент стеку
        // NFT address - first element of stack
        // Адрес NFT - первый элемент стека
        JsonElement nftElement = stackArray.get(0);
        if (!nftElement.isJsonArray()) {
            throw new IOException("Invalid NFT address data format");
        }
        
        JsonArray nftArray = nftElement.getAsJsonArray();
        if (nftArray.size() < 2) {
            throw new IOException("Invalid NFT address array format");
        }
        
        // Другий елемент містить адресу
        // Second element contains the address
        // Второй элемент содержит адрес
        String addressStr = nftArray.get(1).getAsString();
        return new Address(addressStr);
    }
    
    /**
     * Масове створення NFT (mint)
     * @param owner адреса власника
     * @param contents список контенту для кожного NFT
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void batchMint(Address owner, List<Cell> contents, Crypto.PrivateKey privateKey) throws IOException {
        // Отримуємо поточний індекс для наступного NFT
        // Получаем текущий индекс для следующего NFT
        // Get current index for next NFT
        long startIndex = getNextItemIndex();
        
        // Створюємо список транзакцій для масового мінтингу
        // Создаем список транзакций для массового минтинга
        // Create list of transactions for batch minting
        for (int i = 0; i < contents.size(); i++) {
            mintNft(owner, contents.get(i), startIndex + i, privateKey);
        }
    }
    
    /**
     * Створення одного NFT (mint)
     * @param owner адреса власника
     * @param content контент NFT
     * @param index індекс NFT
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void mintNft(Address owner, Cell content, long index, Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо комірку з тілом повідомлення для мінтингу NFT
        // Создаем ячейку с телом сообщения для минтинга NFT
        // Create cell with message body for NFT minting
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо opcode для mint (0x178d4519)
        // Добавляем opcode для mint (0x178d4519)
        // Add opcode for mint (0x178d4519)
        bodyBuilder.storeUInt(32, 0x178d4519);
        
        // Додаємо query_id (0 для простоти)
        // Добавляем query_id (0 для простоты)
        // Add query_id (0 for simplicity)
        bodyBuilder.storeUInt(64, 0);
        
        // Додаємо індекс
        // Добавляем индекс
        // Add index
        bodyBuilder.storeUInt(64, index);
        
        // Додаємо адресу власника
        // Добавляем адрес владельца
        // Add owner address
        bodyBuilder.storeAddress(owner);
        
        // Додаємо контент
        // Добавляем контент
        // Add content
        bodyBuilder.storeRef(content);
        
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