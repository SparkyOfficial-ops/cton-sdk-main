// WalletConnectExample.java - приклад використання Wallet Connect
// Author: Андрій Будильников (Sparky)
// Example of using Wallet Connect
// Пример использования Wallet Connect

package com.cton.contract.example;

import java.util.HashMap;
import java.util.Map;

import com.cton.api.TonApiClient;
import com.cton.contract.WalletConnect;
import com.cton.contract.WalletV3;
import com.cton.sdk.Address;

/**
 * Приклад використання Wallet Connect
 */
public class WalletConnectExample {
    
    public static void main(String[] args) {
        System.out.println("CTON-SDK Wallet Connect Example");
        System.out.println("===============================");
        
        // Створюємо API клієнт
        TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/");
        
        // Створюємо адресу кошелька
        Address walletAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
        
        // Створюємо кошик
        WalletV3 wallet = new WalletV3(walletAddress, apiClient);
        
        // Створюємо Wallet Connect
        WalletConnect walletConnect = new WalletConnect(wallet);
        
        // Отримуємо ідентифікатор сесії
        String sessionId = walletConnect.getSessionId();
        System.out.println("Wallet Connect session ID: " + sessionId);
        
        // Створюємо метадані dApp
        Map<String, String> dAppMetadata = new HashMap<>();
        dAppMetadata.put("name", "Example dApp");
        dAppMetadata.put("url", "https://example.com");
        dAppMetadata.put("description", "An example decentralized application");
        dAppMetadata.put("icons", "https://example.com/icon.png");
        
        // Створюємо сесію Wallet Connect
        String wcUri = walletConnect.createSession(dAppMetadata);
        System.out.println("Wallet Connect URI: " + wcUri);
        
        // Симуляція запиту від dApp на отримання балансу
        System.out.println("\n--- Simulating dApp request: getBalance ---");
        Map<String, Object> balanceParams = new HashMap<>();
        WalletConnect.WalletConnectResponse balanceResponse = walletConnect.handleRequest(
            "req_1", 
            "ton_getBalance", 
            balanceParams
        );
        
        if (balanceResponse.isSuccess()) {
            System.out.println("Balance request successful");
            Map<String, Object> result = balanceResponse.getResult();
            System.out.println("Balance: " + result.get("balance"));
        } else {
            System.out.println("Balance request failed: " + balanceResponse.getError());
        }
        
        // Симуляція запиту від dApp на відправку транзакції
        System.out.println("\n--- Simulating dApp request: sendTransaction ---");
        Map<String, Object> transactionParams = new HashMap<>();
        transactionParams.put("to", "EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V");
        transactionParams.put("amount", "1000000000"); // 1 TON
        transactionParams.put("comment", "Payment for service");
        
        WalletConnect.WalletConnectResponse transactionResponse = walletConnect.handleRequest(
            "req_2", 
            "ton_sendTransaction", 
            transactionParams
        );
        
        if (transactionResponse.isSuccess()) {
            System.out.println("Transaction request processed");
            Map<String, Object> result = transactionResponse.getResult();
            System.out.println("Status: " + result.get("status"));
        } else {
            System.out.println("Transaction request failed: " + transactionResponse.getError());
        }
        
        // Симуляція підтвердження запиту користувачем
        System.out.println("\n--- Simulating user approval ---");
        WalletConnect.WalletConnectResponse approvalResponse = walletConnect.approveRequest("req_2", null);
        
        if (approvalResponse.isSuccess()) {
            System.out.println("Transaction approved by user");
            Map<String, Object> result = approvalResponse.getResult();
            System.out.println("Status: " + result.get("status"));
        } else {
            System.out.println("Approval failed: " + approvalResponse.getError());
        }
        
        // Закриваємо сесію
        walletConnect.disconnect();
        System.out.println("\nWallet Connect session disconnected");
    }
}