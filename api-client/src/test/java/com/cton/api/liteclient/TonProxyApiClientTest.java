// TonProxyApiClientTest.java - тести для TonProxyApiClient
// Author: Андрій Будильников (Sparky)
// Tests for TonProxyApiClient
// Тесты для TonProxyApiClient

package com.cton.api.liteclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonObject;

/**
 * Тести для TonProxyApiClient
 */
public class TonProxyApiClientTest {
    
    private TonProxyApiClient client;
    
    @BeforeEach
    public void setUp() {
        client = new TonProxyApiClient();
    }
    
    @Test
    public void testConstructor() {
        // Тест конструктора
        // Тест конструктора
        // Test constructor
        assertNotNull(client);
        assertFalse(client.isConnected());
    }
    
    @Test
    public void testConnectDisconnect() {
        // Тест підключення та відключення
        // Тест подключения и отключения
        // Test connect and disconnect
        assertDoesNotThrow(() -> {
            client.connect("127.0.0.1", 8080, "test-key");
            assertTrue(client.isConnected());
            
            client.disconnect();
            assertFalse(client.isConnected());
        });
    }
    
    @Test
    public void testRunGetMethodWithoutConnection() {
        // Тест виконання методу без підключення
        // Тест выполнения метода без подключения
        // Test method execution without connection
        JsonObject stack = new JsonObject();
        assertThrows(java.io.IOException.class, () -> {
            client.runGetMethod("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N", "get_balance", stack);
        });
    }
    
    @Test
    public void testGetBlockHeaderWithoutConnection() {
        // Тест отримання заголовка блоку без підключення
        // Тест получения заголовка блока без подключения
        // Test block header retrieval without connection
        assertThrows(java.io.IOException.class, () -> {
            client.getBlockHeader(0, 123456789L, 1000L);
        });
    }
    
    @Test
    public void testGetBlockTransactionsWithoutConnection() {
        // Тест отримання транзакцій блоку без підключення
        // Тест получения транзакций блока без подключения
        // Test block transactions retrieval without connection
        assertThrows(java.io.IOException.class, () -> {
            client.getBlockTransactions(0, 123456789L, 1000L);
        });
    }
    
    @Test
    public void testSendExternalMessageWithoutConnection() {
        // Тест надсилання зовнішнього повідомлення без підключення
        // Тест отправки внешнего сообщения без подключения
        // Test external message sending without connection
        assertThrows(java.io.IOException.class, () -> {
            client.sendExternalMessage("test-boc");
        });
    }
}