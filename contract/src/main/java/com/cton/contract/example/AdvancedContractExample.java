// AdvancedContractExample.java - приклад використання розширених функцій контрактів
// Author: Андрій Будильников (Sparky)
// Example of using advanced contract features
// Пример использования расширенных функций контрактов

package com.cton.contract.example;

import java.io.IOException;
import java.math.BigInteger;

import com.cton.api.TonApiClient;
import com.cton.contract.BulkTransfer;
import com.cton.contract.HighloadWallet;
import com.cton.contract.Subscription;
import com.cton.contract.WalletV1;
import com.cton.contract.WalletV2;
import com.cton.contract.WalletV3;
import com.cton.contract.WalletV4;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;

/**
 * Приклад використання розширених функцій контрактів
 * 
 * Example of using advanced contract features
 * Пример использования расширенных функций контрактов
 */
public class AdvancedContractExample {
    
    public static void main(String[] args) {
        try {
            // Створюємо API клієнта
            TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/jsonRPC");
            
            // Демонстрація різних версій кошельків
            demonstrateWalletVersions(apiClient);
            
            // Демонстрація високонавантаженого кошелька
            demonstrateHighloadWallet(apiClient);
            
            // Демонстрація контракту підписки
            demonstrateSubscriptionContract(apiClient);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Демонстрація різних версій кошельків
     */
    private static void demonstrateWalletVersions(TonApiClient apiClient) throws IOException {
        System.out.println("=== Демонстрація різних версій кошельків ===");
        
        // Створюємо адресу кошелька
        Address walletAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
        
        // Wallet V1
        WalletV1 walletV1 = new WalletV1(walletAddress, apiClient);
        System.out.println("Wallet V1 version: " + walletV1.getVersion());
        
        // Wallet V2
        WalletV2 walletV2 = new WalletV2(walletAddress, apiClient);
        System.out.println("Wallet V2 version: " + walletV2.getVersion());
        System.out.println("Wallet V2 subwallet ID: " + walletV2.getSubwalletId());
        
        // Wallet V3
        WalletV3 walletV3 = new WalletV3(walletAddress, apiClient);
        System.out.println("Wallet V3 version: " + walletV3.getVersion());
        System.out.println("Wallet V3 subwallet ID: " + walletV3.getSubwalletId());
        
        // Wallet V4
        WalletV4 walletV4 = new WalletV4(walletAddress, apiClient);
        System.out.println("Wallet V4 version: " + walletV4.getVersion());
        System.out.println("Wallet V4 subwallet ID: " + walletV4.getSubwalletId());
        
        System.out.println();
    }
    
    /**
     * Демонстрація високонавантаженого кошелька
     */
    private static void demonstrateHighloadWallet(TonApiClient apiClient) throws IOException {
        System.out.println("=== Демонстрація високонавантаженого кошелька ===");
        
        // Створюємо адресу кошелька
        Address walletAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
        
        // Створюємо високонавантажений кошик
        HighloadWallet highloadWallet = new HighloadWallet(walletAddress, apiClient);
        
        System.out.println("Highload Wallet version: " + highloadWallet.getVersion());
        System.out.println("Highload Wallet subwallet ID: " + highloadWallet.getSubwalletId());
        
        // Створюємо масив транзакцій
        Address recipient1 = new Address("0:abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890");
        Address recipient2 = new Address("0:fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210");
        
        BulkTransfer[] transfers = new BulkTransfer[] {
            new BulkTransfer(recipient1, BigInteger.valueOf(1000000000L), "Payment 1"),
            new BulkTransfer(recipient2, BigInteger.valueOf(2000000000L), "Payment 2")
        };
        
        // Створюємо багато транзакцій
        Cell bulkTransferMessage = highloadWallet.createBulkTransfer(transfers);
        System.out.println("Created bulk transfer message");
        
        System.out.println();
    }
    
    /**
     * Демонстрація контракту підписки
     */
    private static void demonstrateSubscriptionContract(TonApiClient apiClient) throws IOException {
        System.out.println("=== Демонстрація контракту підписки ===");
        
        // Створюємо адресу контракту
        Address contractAddress = new Address("0:1234567890123456789012345678901234567890123456789012345678901234");
        
        // Створюємо контракт підписки
        Subscription subscription = new Subscription(contractAddress, apiClient);
        
        System.out.println("Subscription contract address: " + subscription.getAddress().toRaw());
        
        // Створюємо повідомлення для підписки
        Address beneficiary = new Address("0:abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890");
        Cell subscriptionMessage = subscription.createSubscriptionMessage(
            beneficiary,
            BigInteger.valueOf(1000000000L), // 1 TON
            86400, // 1 день
            3600,  // 1 година таймаут
            "Monthly subscription"
        );
        
        System.out.println("Created subscription message");
        
        // Створюємо повідомлення для скасування підписки
        Cell cancelMessage = subscription.createCancelSubscriptionMessage(12345L);
        System.out.println("Created cancel subscription message");
        
        System.out.println();
    }
}