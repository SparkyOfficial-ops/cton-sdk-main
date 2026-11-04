// LiteClient.java - інтерфейс для роботи з проксі-сервером TON
// Author: Андрій Будильников (Sparky)
// Interface for working with TON proxy server
// Интерфейс для работы с прокси-сервером TON

package com.cton.api.liteclient;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

/**
 * Інтерфейс для роботи з проксі-сервером TON
 * 
 * Interface for working with TON proxy server
 * Интерфейс для работы с прокси-сервером TON
 */
public interface LiteClient {
    
    /**
     * Підключитися до проксі-сервера
     * @param host хост сервера
     * @param port порт сервера
     * @param publicKeyBase64 публічний ключ сервера в Base64
     * @throws IOException якщо сталася помилка підключення
     */
    void connect(String host, int port, String publicKeyBase64) throws IOException;
    
    /**
     * Відключитися від проксі-сервера
     */
    void disconnect();
    
    /**
     * Перевірити чи підключено до проксі-сервера
     * @return true якщо підключено
     */
    boolean isConnected();
    
    /**
     * Виконати get-метод смарт-контракту
     * @param address адреса контракту
     * @param method назва методу
     * @param stack параметри методу
     * @return результат виконання методу
     * @throws IOException якщо сталася помилка мережі
     */
    JsonObject runGetMethod(String address, String method, JsonObject stack) throws IOException;
    
    /**
     * Асинхронно виконати get-метод смарт-контракту
     * @param address адреса контракту
     * @param method назва методу
     * @param stack параметри методу
     * @return CompletableFuture з результатом виконання методу
     */
    CompletableFuture<JsonObject> runGetMethodAsync(String address, String method, JsonObject stack);
    
    /**
     * Отримати інформацію про блок
     * @param workchain робочий ланцюг
     * @param shard шард
     * @param seqno номер послідовності
     * @return інформація про блок
     * @throws IOException якщо сталася помилка мережі
     */
    JsonObject getBlockHeader(int workchain, long shard, long seqno) throws IOException;
    
    /**
     * Отримати транзакції з блоку
     * @param workchain робочий ланцюг
     * @param shard шард
     * @param seqno номер послідовності
     * @return транзакції з блоку
     * @throws IOException якщо сталася помилка мережі
     */
    JsonObject getBlockTransactions(int workchain, long shard, long seqno) throws IOException;
    
    /**
     * Надіслати зовнішнє повідомлення
     * @param messageBoc BOC повідомлення у форматі Base64
     * @return результат операції
     * @throws IOException якщо сталася помилка мережі
     */
    JsonObject sendExternalMessage(String messageBoc) throws IOException;
}