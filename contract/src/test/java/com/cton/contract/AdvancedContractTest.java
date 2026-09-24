// AdvancedContractTest.java - тестування розширених функцій контрактів
// Author: Андрій Будильников (Sparky)
// Testing advanced contract features
// Тестирование расширенных функций контрактов

package com.cton.contract;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;

public class AdvancedContractTest {
    
    @Test
    @DisplayName("Test Wallet V1 functionality")
    public void testWalletV1() {
        assertDoesNotThrow(() -> {
            // Створюємо мок API клієнта
            TonApiClient apiClient = Mockito.mock(TonApiClient.class);
            
            // Створюємо адресу кошелька
            Address walletAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
            
            // Створюємо Wallet V1
            WalletV1 wallet = new WalletV1(walletAddress, apiClient);
            
            // Перевіряємо базову функціональність
            assertEquals(walletAddress, wallet.getAddress());
            assertEquals(1, wallet.getVersion());
        });
    }
    
    @Test
    @DisplayName("Test Wallet V2 functionality")
    public void testWalletV2() {
        assertDoesNotThrow(() -> {
            // Створюємо мок API клієнта
            TonApiClient apiClient = Mockito.mock(TonApiClient.class);
            
            // Створюємо адресу кошелька
            Address walletAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
            
            // Створюємо Wallet V2
            WalletV2 wallet = new WalletV2(walletAddress, apiClient);
            
            // Перевіряємо базову функціональність
            assertEquals(walletAddress, wallet.getAddress());
            assertEquals(2, wallet.getVersion());
            assertEquals(698983191, wallet.getSubwalletId());
        });
    }
    
    @Test
    @DisplayName("Test Wallet V3 functionality")
    public void testWalletV3() {
        assertDoesNotThrow(() -> {
            // Створюємо мок API клієнта
            TonApiClient apiClient = Mockito.mock(TonApiClient.class);
            
            // Створюємо адресу кошелька
            Address walletAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
            
            // Створюємо Wallet V3
            WalletV3 wallet = new WalletV3(walletAddress, apiClient);
            
            // Перевіряємо базову функціональність
            assertEquals(walletAddress, wallet.getAddress());
            assertEquals(3, wallet.getVersion());
            assertEquals(698983191, wallet.getSubwalletId());
        });
    }
    
    @Test
    @DisplayName("Test Wallet V4 functionality")
    public void testWalletV4() {
        assertDoesNotThrow(() -> {
            // Створюємо мок API клієнта
            TonApiClient apiClient = Mockito.mock(TonApiClient.class);
            
            // Створюємо адресу кошелька
            Address walletAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
            
            // Створюємо Wallet V4
            WalletV4 wallet = new WalletV4(walletAddress, apiClient);
            
            // Перевіряємо базову функціональність
            assertEquals(walletAddress, wallet.getAddress());
            assertEquals(4, wallet.getVersion());
            assertEquals(698983191, wallet.getSubwalletId());
        });
    }
    
    @Test
    @DisplayName("Test Highload Wallet functionality")
    public void testHighloadWallet() {
        assertDoesNotThrow(() -> {
            // Створюємо мок API клієнта
            TonApiClient apiClient = Mockito.mock(TonApiClient.class);
            
            // Створюємо адресу кошелька
            Address walletAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
            
            // Створюємо Highload Wallet
            HighloadWallet wallet = new HighloadWallet(walletAddress, apiClient);
            
            // Перевіряємо базову функціональність
            assertEquals(walletAddress, wallet.getAddress());
            assertEquals(2, wallet.getVersion()); // Highload Wallet v2
            assertEquals(698983191, wallet.getSubwalletId());
        });
    }
    
    @Test
    @DisplayName("Test Bulk Transfer functionality")
    public void testBulkTransfer() {
        assertDoesNotThrow(() -> {
            // Створюємо адреси
            Address addr1 = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
            Address addr2 = new Address("0:abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890");
            
            // Створюємо масив транзакцій
            BulkTransfer[] transfers = new BulkTransfer[] {
                new BulkTransfer(addr1, BigInteger.valueOf(1000000000L), "Payment 1"),
                new BulkTransfer(addr2, BigInteger.valueOf(2000000000L), "Payment 2")
            };
            
            // Перевіряємо створення транзакцій
            assertEquals(2, transfers.length);
            assertEquals(addr1, transfers[0].getDestination());
            assertEquals(addr2, transfers[1].getDestination());
            assertEquals("Payment 1", transfers[0].getComment());
            assertEquals("Payment 2", transfers[1].getComment());
        });
    }
    
    @Test
    @DisplayName("Test Subscription contract functionality")
    public void testSubscriptionContract() {
        assertDoesNotThrow(() -> {
            // Створюємо мок API клієнта
            TonApiClient apiClient = Mockito.mock(TonApiClient.class);
            
            // Створюємо адресу контракту
            Address contractAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
            
            // Створюємо контракт підписки
            Subscription subscription = new Subscription(contractAddress, apiClient);
            
            // Перевіряємо базову функціональність
            assertEquals(contractAddress, subscription.getAddress());
        });
    }
}