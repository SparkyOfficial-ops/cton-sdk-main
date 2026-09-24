// TonApiClientComprehensiveTest.java - комплексні тести для всіх методів TonApiClient
// Author: Андрій Будильников (Sparky)
// Comprehensive tests for all TonApiClient methods
// Комплексные тесты для всех методов TonApiClient

package com.cton.api;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Комплексні тести для всіх методів TonApiClient
 * Comprehensive tests for all TonApiClient methods
 * Комплексные тесты для всех методов TonApiClient
 */
public class TonApiClientComprehensiveTest {
    
    private TonApiClient client;
    
    @BeforeEach
    public void setUp() {
        // Використовуємо тестову адресу TON Center API
        // Используем тестовый адрес TON Center API
        // Use test TON Center API address
        client = new TonApiClient("https://toncenter.com/api/v2/");
    }
    
    // Тести для синхронних методів
    // Tests for synchronous methods
    // Тесты для синхронных методов
    
    @Test
    @Disabled("Requires network connection and valid API key")
    public void testGetAddressInformation() throws IOException {
        // Тест отримання інформації про адресу
        // Тест получения информации об адресе
        // Test getting address information
        String address = "0:779dcc85521e5fc8d5f3cbf718303297f066a787deb92152043b70231d365e1a";
        JsonObject result = client.getAddressInformation(address);
        assertNotNull(result);
        assertTrue(result.has("ok"));
        assertEquals(true, result.get("ok").getAsBoolean());
    }
    
    @Test
    @Disabled("Requires network connection and valid API key")
    public void testRunGetMethod() throws IOException {
        // Тест виконання get-методу
        // Тест выполнения get-метода
        // Test running get-method
        String address = "0:779dcc85521e5fc8d5f3cbf718303297f066a787deb92152043b70231d365e1a";
        String method = "seqno";
        JsonObject stack = new JsonObject();
        // Порожній стек для seqno методу
        // Пустой стек для seqno метода
        // Empty stack for seqno method
        
        JsonObject result = client.runGetMethod(address, method, stack);
        assertNotNull(result);
        assertTrue(result.has("ok"));
        assertEquals(true, result.get("ok").getAsBoolean());
    }
    
    @Test
    @Disabled("Requires network connection and valid API key")
    public void testGetBlockHeader() throws IOException {
        // Тест отримання інформації про блок
        // Тест получения информации о блоке
        // Test getting block information
        int workchain = -1; // masterchain
        long shard = -9223372036854775808L; // основний шард / основной шард / main shard
        long seqno = 1234567; // приклад номера / пример номера / example number
        
        JsonObject result = client.getBlockHeader(workchain, shard, seqno);
        assertNotNull(result);
        // Результат може бути різним залежно від доступності блоку
        // Результат может быть разным в зависимости от доступности блока
        // Result may vary depending on block availability
    }
    
    @Test
    @Disabled("Requires network connection and valid API key")
    public void testGetBlockTransactions() throws IOException {
        // Тест отримання транзакцій з блоку
        // Тест получения транзакций из блока
        // Test getting transactions from block
        int workchain = -1; // masterchain
        long shard = -9223372036854775808L; // основний шард / основной шард / main shard
        long seqno = 1234567; // приклад номера / пример номера / example number
        
        JsonObject result = client.getBlockTransactions(workchain, shard, seqno);
        assertNotNull(result);
        // Результат може бути різним залежно від доступності блоку
        // Результат может быть разным в зависимости от доступности блока
        // Result may vary depending on block availability
    }
    
    @Test
    public void testSendBocByteArray() {
        // Тест надсилання BOC (байти)
        // Тест отправки BOC (байты)
        // Test sending BOC (bytes)
        byte[] dummyBoc = new byte[]{0x01, 0x02, 0x03, 0x04};
        assertThrows(IOException.class, () -> {
            client.sendBoc(dummyBoc);
        });
    }
    
    @Test
    public void testSendBocString() {
        // Тест надсилання BOC (строка)
        // Тест отправки BOC (строка)
        // Test sending BOC (string)
        String dummyBoc = "AQIDBA=="; // base64 encoded 01020304
        assertThrows(IOException.class, () -> {
            client.sendBoc(dummyBoc);
        });
    }
    
    // Тести для асинхронних методів
    // Tests for asynchronous methods
    // Тесты для асинхронных методов
    
    @Test
    @Disabled("Requires network connection and valid API key")
    public void testGetAddressInformationAsync() {
        // Тест асинхронного отримання інформації про адресу
        // Тест асинхронного получения информации об адресе
        // Test asynchronous getting address information
        String address = "0:779dcc85521e5fc8d5f3cbf718303297f066a787deb92152043b70231d365e1a";
        CompletableFuture<JsonObject> future = client.getAddressInformationAsync(address);
        
        assertDoesNotThrow(() -> {
            JsonObject result = future.join();
            assertNotNull(result);
            assertTrue(result.has("ok"));
            assertEquals(true, result.get("ok").getAsBoolean());
        });
    }
    
    @Test
    @Disabled("Requires network connection and valid API key")
    public void testRunGetMethodAsync() {
        // Тест асинхронного виконання get-методу
        // Тест асинхронного выполнения get-метода
        // Test asynchronous running get-method
        String address = "0:779dcc85521e5fc8d5f3cbf718303297f066a787deb92152043b70231d365e1a";
        String method = "seqno";
        JsonObject stack = new JsonObject();
        
        CompletableFuture<JsonObject> future = client.runGetMethodAsync(address, method, stack);
        
        assertDoesNotThrow(() -> {
            JsonObject result = future.join();
            assertNotNull(result);
            assertTrue(result.has("ok"));
            assertEquals(true, result.get("ok").getAsBoolean());
        });
    }
    
    @Test
    @Disabled("Requires network connection and valid API key")
    public void testGetBlockHeaderAsync() {
        // Тест асинхронного отримання інформації про блок
        // Тест асинхронного получения информации о блоке
        // Test asynchronous getting block information
        int workchain = -1; // masterchain
        long shard = -9223372036854775808L; // основний шард / основной шард / main shard
        long seqno = 1234567; // приклад номера / пример номера / example number
        
        CompletableFuture<JsonObject> future = client.getBlockHeaderAsync(workchain, shard, seqno);
        
        assertDoesNotThrow(() -> {
            JsonObject result = future.join();
            assertNotNull(result);
        });
    }
    
    @Test
    @Disabled("Requires network connection and valid API key")
    public void testGetBlockTransactionsAsync() {
        // Тест асинхронного отримання транзакцій з блоку
        // Тест асинхронного получения транзакций из блока
        // Test asynchronous getting transactions from block
        int workchain = -1; // masterchain
        long shard = -9223372036854775808L; // основний шард / основной шард / main shard
        long seqno = 1234567; // приклад номера / пример номера / example number
        
        CompletableFuture<JsonObject> future = client.getBlockTransactionsAsync(workchain, shard, seqno);
        
        assertDoesNotThrow(() -> {
            JsonObject result = future.join();
            assertNotNull(result);
        });
    }
    
    @Test
    public void testSendBocAsyncByteArray() {
        // Тест асинхронного надсилання BOC (байти)
        // Тест асинхронной отправки BOC (байты)
        // Test asynchronous sending BOC (bytes)
        byte[] dummyBoc = new byte[]{0x01, 0x02, 0x03, 0x04};
        CompletableFuture<JsonObject> future = client.sendBocAsync(dummyBoc);
        
        // For async methods, we expect RuntimeException which wraps the IOException
        assertThrows(RuntimeException.class, () -> {
            future.join();
        });
    }
    
    @Test
    public void testSendBocAsyncString() {
        // Тест асинхронного надсилання BOC (строка)
        // Тест асинхронной отправки BOC (строка)
        // Test asynchronous sending BOC (string)
        String dummyBoc = "AQIDBA=="; // base64 encoded 01020304
        CompletableFuture<JsonObject> future = client.sendBocAsync(dummyBoc);
        
        // For async methods, we expect RuntimeException which wraps the IOException
        assertThrows(RuntimeException.class, () -> {
            future.join();
        });
    }
}