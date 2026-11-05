// TonApiClientComprehensiveTest.java - комплексні тести для всіх методів TonApiClient
// Author: Андрій Будильников (Sparky)
// Comprehensive tests for all TonApiClient methods
// Комплексные тесты для всех методов TonApiClient

package com.cton.api;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
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
        String apiKey = System.getenv("CTON_TESTNET_API_KEY");
        if (apiKey != null && !apiKey.isBlank()) {
            client = new TonApiClient("https://testnet.toncenter.com/api/v2/", apiKey);
        } else {
            client = new TonApiClient("https://toncenter.com/api/v2/");
        }
    }
    
    // Тести для синхронних методів
    // Tests for synchronous methods
    // Тесты для синхронных методов
    
    @Test
    @EnabledIfEnvironmentVariable(named = "CTON_TESTNET_API_KEY", matches = ".+")
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
    @EnabledIfEnvironmentVariable(named = "CTON_TESTNET_API_KEY", matches = ".+")
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
    @EnabledIfEnvironmentVariable(named = "CTON_TESTNET_API_KEY", matches = ".+")
    public void testGetBlockHeader() throws IOException {
        // Тест отримання інформації про блок
        // Тест получения информации о блоке
        // Test getting block information
        JsonObject result = client.getBlockHeader(workchain(), shard(), seqno());
        assertNotNull(result);
        // Результат може бути різним залежно від доступності блоку
        // Результат может быть разным в зависимости от доступности блока
        // Result may vary depending on block availability
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "CTON_TESTNET_API_KEY", matches = ".+")
    public void testGetBlockTransactions() throws IOException {
        // Тест отримання транзакцій з блоку
        // Тест получения транзакций из блока
        // Test getting transactions from block
        JsonObject result = client.getBlockTransactions(workchain(), shard(), seqno());
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
    @EnabledIfEnvironmentVariable(named = "CTON_TESTNET_API_KEY", matches = ".+")
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
    @EnabledIfEnvironmentVariable(named = "CTON_TESTNET_API_KEY", matches = ".+")
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
    @EnabledIfEnvironmentVariable(named = "CTON_TESTNET_API_KEY", matches = ".+")
    public void testGetBlockHeaderAsync() throws IOException {
        // Тест асинхронного отримання інформації про блок
        // Тест асинхронного получения информации о блоке
        // Test asynchronous getting block information
        CompletableFuture<JsonObject> future = client.getBlockHeaderAsync(workchain(), shard(), seqno());
        
        assertDoesNotThrow(() -> {
            JsonObject result = future.join();
            assertNotNull(result);
        });
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "CTON_TESTNET_API_KEY", matches = ".+")
    public void testGetBlockTransactionsAsync() throws IOException {
        // Тест асинхронного отримання транзакцій з блоку
        // Тест асинхронного получения транзакций из блока
        // Test asynchronous getting transactions from block
        CompletableFuture<JsonObject> future = client.getBlockTransactionsAsync(workchain(), shard(), seqno());
        
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

    private static long[] cachedMasterchain;

    private int workchain() throws IOException {
        return (int) masterchain()[0];
    }

    private long shard() throws IOException {
        return masterchain()[1];
    }

    private long seqno() throws IOException {
        return masterchain()[2];
    }

    private long[] masterchain() throws IOException {
        if (cachedMasterchain != null) {
            return cachedMasterchain;
        }
        JsonObject last = client.getMasterchainInfo().getAsJsonObject("result").getAsJsonObject("last");
        com.google.gson.JsonElement shard = last.get("shard");
        long shardValue = shard.getAsJsonPrimitive().isString()
            ? Long.parseLong(shard.getAsString())
            : shard.getAsLong();
        cachedMasterchain = new long[] { last.get("workchain").getAsLong(), shardValue, last.get("seqno").getAsLong() };
        return cachedMasterchain;
    }
}