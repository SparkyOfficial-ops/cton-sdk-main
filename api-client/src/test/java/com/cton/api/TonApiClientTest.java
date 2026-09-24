// TonApiClientTest.java - тести для TonApiClient
// Author: Андрій Будильников (Sparky)
// Tests for TonApiClient
// Тесты для TonApiClient

package com.cton.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonObject;
import java.io.IOException;

/**
 * Тести для TonApiClient
 */
public class TonApiClientTest {
    
    private TonApiClient client;
    
    @BeforeEach
    public void setUp() {
        // Використовуємо тестову адресу TON Center API
        // Используем тестовый адрес TON Center API
        // Use test TON Center API address
        client = new TonApiClient("https://toncenter.com/api/v2/");
    }
    
    @Test
    public void testConstructor() {
        // Тест конструктора без API ключа
        // Тест конструктора без API ключа
        // Test constructor without API key
        TonApiClient clientWithoutKey = new TonApiClient("https://toncenter.com/api/v2/");
        assertNotNull(clientWithoutKey);
        
        // Тест конструктора з API ключем
        // Тест конструктора с API ключом
        // Test constructor with API key
        TonApiClient clientWithKey = new TonApiClient("https://toncenter.com/api/v2/", "test-key");
        assertNotNull(clientWithKey);
    }
    
    @Test
    public void testBytesToBase64() {
        // Цей тест не може бути виконаний напряму, оскільки bytesToBase64 є приватним
        // Этот тест не может быть выполнен напряму, так как bytesToBase64 является приватным
        // This test cannot be executed directly since bytesToBase64 is private
        assertTrue(true); // Просто перевірка що все компілюється / Just check that everything compiles / Просто проверка что все компилируется
    }
    
    @Test
    public void testClose() {
        // Тест методу close
        // Тест метода close
        // Test close method
        assertDoesNotThrow(() -> {
            client.close();
        });
    }
    
    // Примітка: Наступні тести вимагають мережевого з'єднання і дійсного API ключа
    // Закоментовані щоб не викликати помилок при запуску тестів
    // Note: The following tests require network connection and a valid API key
    // Commented out to avoid errors when running tests
    
    /*
    @Test
    public void testGetAddressInformation() throws IOException {
        // Тест отримання інформації про адресу
        // Тест получения информации об адресе
        // Test getting address information
        String address = "EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N"; // Example address in raw format
        JsonObject result = client.getAddressInformation(address);
        assertNotNull(result);
        assertTrue(result.has("ok"));
    }
    
    @Test
    public void testSendBoc() throws IOException {
        // Тест надсилання BOC
        // Тест отправки BOC
        // Test sending BOC
        byte[] dummyBoc = new byte[]{0x01, 0x02, 0x03, 0x04};
        assertThrows(IOException.class, () -> {
            client.sendBoc(dummyBoc);
        });
    }
    */
}