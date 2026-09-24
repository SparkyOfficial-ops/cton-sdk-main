// TonApiClientExample.java - приклад використання TonApiClient
// Author: Андрій Будильников (Sparky)
// Example of using TonApiClient
// Пример использования TonApiClient

package com.cton.api.example;

import com.cton.api.TonApiClient;
import com.google.gson.JsonObject;
import java.io.IOException;

/**
 * Приклад використання TonApiClient
 */
public class TonApiClientExample {
    
    public static void main(String[] args) {
        // Створюємо клієнт для TON Center API
        // Создаем клиент для TON Center API
        // Create client for TON Center API
        TonApiClient client = new TonApiClient("https://toncenter.com/api/v2/");
        
        try {
            // Отримуємо інформацію про адресу
            // Получаем информацию об адресе
            // Get information about address
            String address = "EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N"; // Example address in raw format
            JsonObject addressInfo = client.getAddressInformation(address);
            
            System.out.println("Address information:");
            System.out.println(addressInfo.toString());
            
            // Якщо у вас є API ключ, ви можете його використовувати
            // Если у вас есть API ключ, вы можете его использовать
            // If you have an API key, you can use it
            // TonApiClient clientWithKey = new TonApiClient("https://toncenter.com/api/v2/", "your-api-key");
            
        } catch (IOException e) {
            System.err.println("Error communicating with TON Center API: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Закриваємо клієнт
            // Закрываем клиент
            // Close client
            client.close();
        }
    }
}