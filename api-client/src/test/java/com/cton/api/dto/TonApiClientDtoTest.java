// TonApiClientDtoTest.java - тести для DTO в TonApiClient
// Author: Андрій Будильников (Sparky)
// Tests for DTOs in TonApiClient
// Тесты для DTO в TonApiClient

package com.cton.api.dto;

import com.cton.api.TonApiClient;
import com.google.gson.JsonObject;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тести для DTO в TonApiClient
 */
public class TonApiClientDtoTest {
    
    private MockWebServer server;
    private TonApiClient client;
    
    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new TonApiClient(server.url("/").toString());
    }
    
    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }
    
    @Test
    public void testGetAddressInformationDto() throws IOException {
        // Підготовлюємо мок відповідь
        // Готовим мок ответ
        // Prepare mock response
        String jsonResponse = "{\n" +
                "  \"balance\": \"1000000000\",\n" +
                "  \"state\": \"active\",\n" +
                "  \"data\": \"data123\",\n" +
                "  \"code\": \"code123\",\n" +
                "  \"lastTransactionId\": \"lt123\"\n" +
                "}";
        
        server.enqueue(new MockResponse().setBody(jsonResponse));
        
        // Виконуємо запит
        // Выполняем запрос
        // Execute request
        AddressInformation result = client.getAddressInformationDto("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
        
        // Перевіряємо результат
        // Проверяем результат
        // Check result
        assertNotNull(result);
        assertEquals("1000000000", result.getBalance());
        assertEquals("active", result.getState());
        assertEquals("data123", result.getData());
        assertEquals("code123", result.getCode());
        assertEquals("lt123", result.getLastTransactionId());
    }
    
    @Test
    public void testGetBlockHeaderDto() throws IOException {
        // Підготовлюємо мок відповідь
        // Готовим мок ответ
        // Prepare mock response
        String jsonResponse = "{\n" +
                "  \"workchain\": 0,\n" +
                "  \"shard\": 123456789,\n" +
                "  \"seqno\": 1000,\n" +
                "  \"rootHash\": \"root123\",\n" +
                "  \"fileHash\": \"file123\",\n" +
                "  \"time\": 1234567890\n" +
                "}";
        
        server.enqueue(new MockResponse().setBody(jsonResponse));
        
        // Виконуємо запит
        // Выполняем запрос
        // Execute request
        BlockHeader result = client.getBlockHeaderDto(0, 123456789L, 1000L);
        
        // Перевіряємо результат
        // Проверяем результат
        // Check result
        assertNotNull(result);
        assertEquals(0, result.getWorkchain());
        assertEquals(123456789L, result.getShard());
        assertEquals(1000L, result.getSeqno());
        assertEquals("root123", result.getRootHash());
        assertEquals("file123", result.getFileHash());
        assertEquals(1234567890L, result.getTime());
    }
    
    @Test
    public void testGetBlockTransactionsDto() throws IOException {
        // Підготовлюємо мок відповідь
        // Готовим мок ответ
        // Prepare mock response
        String jsonResponse = "{\n" +
                "  \"workchain\": 0,\n" +
                "  \"shard\": 123456789,\n" +
                "  \"seqno\": 1000,\n" +
                "  \"rootHash\": \"root123\",\n" +
                "  \"fileHash\": \"file123\",\n" +
                "  \"transactions\": [\n" +
                "    {\n" +
                "      \"account\": \"account1\",\n" +
                "      \"hash\": \"hash1\",\n" +
                "      \"lt\": 100\n" +
                "    },\n" +
                "    {\n" +
                "      \"account\": \"account2\",\n" +
                "      \"hash\": \"hash2\",\n" +
                "      \"lt\": 200\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        
        server.enqueue(new MockResponse().setBody(jsonResponse));
        
        // Виконуємо запит
        // Выполняем запрос
        // Execute request
        BlockTransactions result = client.getBlockTransactionsDto(0, 123456789L, 1000L);
        
        // Перевіряємо результат
        // Проверяем результат
        // Check result
        assertNotNull(result);
        assertEquals(0, result.getWorkchain());
        assertEquals(123456789L, result.getShard());
        assertEquals(1000L, result.getSeqno());
        assertEquals("root123", result.getRootHash());
        assertEquals("file123", result.getFileHash());
        assertNotNull(result.getTransactions());
        assertEquals(2, result.getTransactions().size());
        
        BlockTransactions.Transaction tx1 = result.getTransactions().get(0);
        assertEquals("account1", tx1.getAccount());
        assertEquals("hash1", tx1.getHash());
        assertEquals(100L, tx1.getLt());
        
        BlockTransactions.Transaction tx2 = result.getTransactions().get(1);
        assertEquals("account2", tx2.getAccount());
        assertEquals("hash2", tx2.getHash());
        assertEquals(200L, tx2.getLt());
    }
    
    @Test
    public void testSendBocDto() throws IOException {
        // Підготовлюємо мок відповідь
        // Готовим мок ответ
        // Prepare mock response
        String jsonResponse = "{\n" +
                "  \"hash\": \"boc_hash_123\"\n" +
                "}";
        
        server.enqueue(new MockResponse().setBody(jsonResponse));
        
        // Виконуємо запит
        // Выполняем запрос
        // Execute request
        SendBocResult result = client.sendBocDto("test_boc_data");
        
        // Перевіряємо результат
        // Проверяем результат
        // Check result
        assertNotNull(result);
        assertEquals("boc_hash_123", result.getHash());
    }
    
    @Test
    public void testAsyncDtoMethods() {
        // Тест асинхронних методів DTO
        // Тест асинхронных методов DTO
        // Test async DTO methods
        
        // Підготовлюємо мок відповідь
        // Готовим мок ответ
        // Prepare mock response
        String jsonResponse = "{\n" +
                "  \"balance\": \"1000000000\",\n" +
                "  \"state\": \"active\"\n" +
                "}";
        
        server.enqueue(new MockResponse().setBody(jsonResponse));
        
        // Виконуємо асинхронний запит
        // Выполняем асинхронный запрос
        // Execute async request
        var future = client.getAddressInformationDtoAsync("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
        
        // Очікуємо результат
        // Ожидаем результат
        // Wait for result
        assertDoesNotThrow(() -> {
            AddressInformation result = future.join();
            assertNotNull(result);
            assertEquals("1000000000", result.getBalance());
            assertEquals("active", result.getState());
        });
    }
}