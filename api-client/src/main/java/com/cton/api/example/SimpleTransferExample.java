// SimpleTransferExample.java - приклад простого переказу через API
// Author: Андрій Будильников (Sparky)
// Example of simple transfer through API
// Пример простого перевода через API

package com.cton.api.example;

import com.cton.api.TonApiClient;
import com.google.gson.JsonObject;
import java.io.IOException;

/**
 * Приклад простого переказу через API
 */
public class SimpleTransferExample {
    
    public static void main(String[] args) {
        // Створюємо клієнт для TON Center API
        // Создаем клиент для TON Center API
        // Create client for TON Center API
        TonApiClient client = new TonApiClient("https://toncenter.com/api/v2/");
        
        try {
            System.out.println("CTON-SDK API Client Example");
            System.out.println("==========================");
            
            // Отримуємо інформацію про адресу
            // Получаем информацию об адресе
            // Get information about address
            System.out.println("Getting address information...");
            
            // Для реального використання вам потрібно буде:
            // 1. Створити Address об'єкт з адресою
            // 2. Викликати client.getAddressInformation(address)
            
            System.out.println("To use this client:");
            System.out.println("1. Create an Address object with a valid TON address");
            System.out.println("2. Call client.getAddressInformation(address)");
            System.out.println("3. Process the returned JsonObject");
            
            // Приклад надсилання BOC (потрібен дійсний BOC)
            // Пример отправки BOC (нужен действительный BOC)
            // Example sending BOC (valid BOC needed)
            System.out.println("\nTo send a transaction:");
            System.out.println("1. Create a transaction using the core SDK");
            System.out.println("2. Serialize it to BOC format");
            System.out.println("3. Call client.sendBoc(bocBytes)");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Закриваємо клієнт
            // Закрываем клиент
            // Close client
            client.close();
        }
    }
}