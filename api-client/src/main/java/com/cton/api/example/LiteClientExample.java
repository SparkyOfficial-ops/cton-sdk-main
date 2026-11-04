// LiteClientExample.java - приклад використання проксі-клієнта TON
// Author: Андрій Будильников (Sparky)
// Example of using TON proxy client
// Пример использования прокси-клиента TON

package com.cton.api.example;

import java.io.IOException;

import com.cton.api.liteclient.LiteClient;
import com.cton.api.liteclient.TonProxyApiClient;
import com.google.gson.JsonObject;

/**
 * Приклад використання проксі-клієнта TON
 */
public class LiteClientExample {
    
    public static void main(String[] args) {
        // Створюємо клієнт для проксі-сервера
        // Создаем клиент для прокси-сервера
        // Create client for proxy server
        LiteClient client = new TonProxyApiClient();
        
        try {
            System.out.println("CTON-SDK Proxy Client Example");
            System.out.println("==========================");
            
            // Підключаємось до проксі-сервера
            // Подключаемся к прокси-серверу
            // Connect to proxy server
            System.out.println("Connecting to proxy server...");
            client.connect("127.0.0.1", 8080, "server-public-key");
            
            if (client.isConnected()) {
                System.out.println("Connected to proxy server successfully!");
                
                // Виконуємо get-метод смарт-контракту
                // Выполняем get-метод смарт-контракта
                // Execute smart contract get-method
                System.out.println("Running get-method...");
                JsonObject stack = new JsonObject();
                JsonObject result = client.runGetMethod(
                    "EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N", 
                    "get_balance", 
                    stack
                );
                
                System.out.println("Method result: " + result.toString());
                
                // Отримуємо інформацію про блок
                // Получаем информацию о блоке
                // Get block information
                System.out.println("Getting block header...");
                JsonObject blockHeader = client.getBlockHeader(0, 123456789L, 1000L);
                System.out.println("Block header: " + blockHeader.toString());
                
            } else {
                System.out.println("Failed to connect to proxy server");
            }
            
        } catch (IOException e) {
            System.err.println("Error communicating with proxy server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Відключаємось від проксі-сервера
            // Отключаемся от прокси-сервера
            // Disconnect from proxy server
            if (client.isConnected()) {
                client.disconnect();
                System.out.println("Disconnected from proxy server");
            }
        }
    }
}