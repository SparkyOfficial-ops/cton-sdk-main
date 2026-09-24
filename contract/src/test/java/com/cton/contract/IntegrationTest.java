// IntegrationTest.java - інтеграційний тест для всіх нових функцій
// Author: Андрій Будильников (Sparky)
// Integration test for all new features
// Интеграционный тест для всех новых функций

package com.cton.contract;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;

public class IntegrationTest {
    
    @Test
    @DisplayName("Test all wallet versions integration")
    public void testAllWalletVersionsIntegration() {
        assertDoesNotThrow(() -> {
            // Створюємо мок API клієнта
            TonApiClient apiClient = Mockito.mock(TonApiClient.class);
            
            // Створюємо адресу кошелька
            Address walletAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
            
            // Тестуємо всі версії кошельків
            WalletV1 walletV1 = new WalletV1(walletAddress, apiClient);
            WalletV2 walletV2 = new WalletV2(walletAddress, apiClient);
            WalletV3 walletV3 = new WalletV3(walletAddress, apiClient);
            WalletV4 walletV4 = new WalletV4(walletAddress, apiClient);
            
            // Перевіряємо, що всі кошельки мають правильні версії
            assertEquals(1, walletV1.getVersion());
            assertEquals(2, walletV2.getVersion());
            assertEquals(3, walletV3.getVersion());
            assertEquals(4, walletV4.getVersion());
            
            // Перевіряємо, що кошельки V2, V3, V4 мають subwallet ID
            assertEquals(698983191, walletV2.getSubwalletId());
            assertEquals(698983191, walletV3.getSubwalletId());
            assertEquals(698983191, walletV4.getSubwalletId());
        });
    }
    
    @Test
    @DisplayName("Test highload wallet integration")
    public void testHighloadWalletIntegration() {
        assertDoesNotThrow(() -> {
            // Створюємо мок API клієнта
            TonApiClient apiClient = Mockito.mock(TonApiClient.class);
            
            // Створюємо адресу кошелька
            Address walletAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
            
            // Створюємо високонавантажений кошик
            HighloadWallet highloadWallet = new HighloadWallet(walletAddress, apiClient);
            
            // Перевіряємо версію та subwallet ID
            assertEquals(2, highloadWallet.getVersion()); // Highload Wallet v2
            assertEquals(698983191, highloadWallet.getSubwalletId());
            
            // Створюємо масив транзакцій
            Address recipient1 = new Address("0:abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890");
            Address recipient2 = new Address("0:fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210");
            
            BulkTransfer[] transfers = new BulkTransfer[] {
                new BulkTransfer(recipient1, BigInteger.valueOf(1000000000L), "Payment 1"),
                new BulkTransfer(recipient2, BigInteger.valueOf(2000000000L), "Payment 2")
            };
            
            // Створюємо багато транзакцій
            Cell bulkTransferMessage = highloadWallet.createBulkTransfer(transfers);
            assertNotNull(bulkTransferMessage);
        });
    }
    
    @Test
    @DisplayName("Test subscription contract integration")
    public void testSubscriptionContractIntegration() {
        assertDoesNotThrow(() -> {
            // Створюємо мок API клієнта
            TonApiClient apiClient = Mockito.mock(TonApiClient.class);
            
            // Створюємо адресу контракту
            Address contractAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
            
            // Створюємо контракт підписки
            Subscription subscription = new Subscription(contractAddress, apiClient);
            
            // Перевіряємо адресу
            assertEquals(contractAddress, subscription.getAddress());
            
            // Створюємо повідомлення для підписки
            Address beneficiary = new Address("0:abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890");
            Cell subscriptionMessage = subscription.createSubscriptionMessage(
                beneficiary,
                BigInteger.valueOf(1000000000L), // 1 TON
                86400, // 1 день
                3600,  // 1 година таймаут
                "Monthly subscription"
            );
            assertNotNull(subscriptionMessage);
            
            // Створюємо повідомлення для скасування підписки
            Cell cancelMessage = subscription.createCancelSubscriptionMessage(12345L);
            assertNotNull(cancelMessage);
        });
    }
    
    @Test
    @DisplayName("Test bulk transfer functionality")
    public void testBulkTransferFunctionality() {
        // Створюємо адреси
        assertDoesNotThrow(() -> {
            Address addr1 = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
            Address addr2 = new Address("0:abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890");
            
            // Створюємо масив транзакцій
            BulkTransfer[] transfers = new BulkTransfer[] {
                new BulkTransfer(addr1, BigInteger.valueOf(1000000000L), "Payment 1"),
                new BulkTransfer(addr2, BigInteger.valueOf(2000000000L), "Payment 2"),
                new BulkTransfer(addr1, BigInteger.valueOf(1500000000L), "Payment 3", 1)
            };
            
            // Перевіряємо створення транзакцій
            assertEquals(3, transfers.length);
            assertEquals(addr1, transfers[0].getDestination());
            assertEquals(addr2, transfers[1].getDestination());
            assertEquals(addr1, transfers[2].getDestination());
            assertEquals("Payment 1", transfers[0].getComment());
            assertEquals("Payment 2", transfers[1].getComment());
            assertEquals("Payment 3", transfers[2].getComment());
            assertEquals(3, transfers[0].getMode()); // Default mode
            assertEquals(1, transfers[2].getMode()); // Custom mode
        });
    }
}