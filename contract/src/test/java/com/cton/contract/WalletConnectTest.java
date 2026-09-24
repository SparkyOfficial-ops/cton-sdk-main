// WalletConnectTest.java - тести для WalletConnect
// Author: Андрій Будильников (Sparky)
// Tests for WalletConnect
// Тесты для WalletConnect

package com.cton.contract;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cton.sdk.Address;

/**
 * Тести для WalletConnect
 */
public class WalletConnectTest {
    private Wallet mockWallet;
    private WalletConnect walletConnect;
    
    @BeforeEach
    public void setUp() {
        mockWallet = mock(Wallet.class);
        walletConnect = new WalletConnect(mockWallet);
    }
    
    @Test
    public void testWalletConnectConstructor() {
        // Тест конструктора WalletConnect
        // Test WalletConnect constructor
        // Тест конструктора WalletConnect
        
        assertNotNull(walletConnect);
        assertNotNull(walletConnect.getSessionId());
        assertFalse(walletConnect.getSessionId().isEmpty());
    }
    
    @Test
    public void testCreateSession() {
        // Тест створення сесії
        // Test session creation
        // Тест создания сессии
        
        Map<String, String> dAppMetadata = new HashMap<>();
        dAppMetadata.put("name", "Test dApp");
        dAppMetadata.put("url", "https://test.com");
        
        String sessionUri = walletConnect.createSession(dAppMetadata);
        
        assertNotNull(sessionUri);
        assertTrue(sessionUri.startsWith("wc:"));
        assertTrue(sessionUri.contains("@1?bridge="));
    }
    
    @Test
    public void testHandleGetBalanceRequest() {
        // Тест обробки запиту на отримання балансу
        // Test handling get balance request
        // Тест обработки запроса на получение баланса
        
        try {
            when(mockWallet.getBalance()).thenReturn(java.math.BigInteger.valueOf(1000000000L));
            
            Map<String, Object> params = new HashMap<>();
            WalletConnect.WalletConnectResponse response = walletConnect.handleRequest(
                "test_req_1", 
                "ton_getBalance", 
                params
            );
            
            assertTrue(response.isSuccess());
            assertNotNull(response.getResult());
            assertTrue(response.getResult().containsKey("balance"));
            assertEquals("1000000000", response.getResult().get("balance"));
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testHandleGetBalanceRequestWithIOException() throws Exception {
        // Тест обробки запиту на отримання балансу з IOException
        // Test handling get balance request with IOException
        // Тест обработки запроса на получение баланса с IOException
        
        when(mockWallet.getBalance()).thenThrow(new java.io.IOException("Network error"));
        
        Map<String, Object> params = new HashMap<>();
        WalletConnect.WalletConnectResponse response = walletConnect.handleRequest(
            "test_req_1", 
            "ton_getBalance", 
            params
        );
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        assertTrue(response.getError().contains("Failed to get balance"));
    }
    
    @Test
    public void testHandleSendTransactionRequest() {
        // Тест обробки запиту на відправку транзакції
        // Test handling send transaction request
        // Тест обработки запроса на отправку транзакции
        
        try {
            // Налаштовуємо mock для createTransfer
            com.cton.sdk.Cell mockCell = mock(com.cton.sdk.Cell.class);
            when(mockCell.toString()).thenReturn("mock_cell");
            when(mockWallet.createTransfer(any(Address.class), any(java.math.BigInteger.class), anyString()))
                .thenReturn(mockCell);
            
            Map<String, Object> params = new HashMap<>();
            params.put("to", "EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V");
            params.put("amount", "1000000000");
            params.put("comment", "Test payment");
            
            WalletConnect.WalletConnectResponse response = walletConnect.handleRequest(
                "test_req_2", 
                "ton_sendTransaction", 
                params
            );
            
            assertTrue(response.isSuccess());
            assertNotNull(response.getResult());
            assertTrue(response.getResult().containsKey("status"));
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testHandleUnsupportedRequest() {
        // Тест обробки непідтримуваного запиту
        // Test handling unsupported request
        // Тест обработки неподдерживаемого запроса
        
        Map<String, Object> params = new HashMap<>();
        WalletConnect.WalletConnectResponse response = walletConnect.handleRequest(
            "test_req_3", 
            "unsupported_request", 
            params
        );
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        assertTrue(response.getError().contains("Unsupported request type"));
    }
    
    @Test
    public void testApproveRequest() throws Exception {
        // Тест підтвердження запиту
        // Test approving request
        // Тест подтверждения запроса
        
        // Налаштовуємо mock для getBalance
        when(mockWallet.getBalance()).thenReturn(java.math.BigInteger.valueOf(1000000000L));
        
        // Спочатку створюємо запит
        Map<String, Object> params = new HashMap<>();
        walletConnect.handleRequest("test_req_4", "ton_getBalance", params);
        
        // Потім підтверджуємо його
        WalletConnect.WalletConnectResponse response = walletConnect.approveRequest("test_req_4", null);
        
        assertTrue(response.isSuccess());
        assertNotNull(response.getResult());
        assertEquals("approved", response.getResult().get("status"));
    }
    
    @Test
    public void testRejectRequest() throws Exception {
        // Тест відхилення запиту
        // Test rejecting request
        // Тест отклонения запроса
        
        // Налаштовуємо mock для getBalance
        when(mockWallet.getBalance()).thenReturn(java.math.BigInteger.valueOf(1000000000L));
        
        // Спочатку створюємо запит
        Map<String, Object> params = new HashMap<>();
        walletConnect.handleRequest("test_req_5", "ton_getBalance", params);
        
        // Потім відхиляємо його
        WalletConnect.WalletConnectResponse response = walletConnect.rejectRequest("test_req_5");
        
        assertTrue(response.isSuccess());
        assertNotNull(response.getResult());
        assertEquals("rejected", response.getResult().get("status"));
    }
    
    @Test
    public void testApproveNonExistentRequest() {
        // Тест підтвердження неіснуючого запиту
        // Test approving non-existent request
        // Тест подтверждения несуществующего запроса
        
        WalletConnect.WalletConnectResponse response = walletConnect.approveRequest("non_existent_req", null);
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        assertTrue(response.getError().contains("Request not found"));
    }
    
    @Test
    public void testDisconnect() throws Exception {
        // Тест закриття сесії
        // Test disconnecting session
        // Тест закрытия сессии
        
        // Налаштовуємо mock для getBalance
        when(mockWallet.getBalance()).thenReturn(java.math.BigInteger.valueOf(1000000000L));
        
        // Створюємо кілька запитів
        Map<String, Object> params = new HashMap<>();
        walletConnect.handleRequest("req_1", "ton_getBalance", params);
        walletConnect.handleRequest("req_2", "ton_sendTransaction", params);
        
        // Закриваємо сесію
        walletConnect.disconnect();
        
        // Спроба підтвердити запит після закриття сесії
        WalletConnect.WalletConnectResponse response = walletConnect.approveRequest("req_1", null);
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        assertTrue(response.getError().contains("Request not found"));
    }
}