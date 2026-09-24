// WalletExample.java - приклад використання кошелька
// Author: Андрій Будильников (Sparky)
// Example of using wallet
// Пример использования кошелька

package com.cton.contract.example;

import com.cton.contract.Wallet;
import com.cton.contract.WalletV3;
import com.cton.sdk.Address;
import com.cton.api.TonApiClient;
import java.math.BigInteger;
import java.io.IOException;

/**
 * Приклад використання кошелька
 */
public class WalletExample {
    
    public static void main(String[] args) {
        try {
            System.out.println("CTON-SDK Wallet Example");
            System.out.println("======================");
            
            // Створюємо API клієнт
            // Создаем API клиент
            // Create API client
            TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/");
            
            // Створюємо адресу кошелька
            // Создаем адрес кошелька
            // Create wallet address
            Address walletAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            
            // Створюємо кошик
            // Создаем кошелек
            // Create wallet
            Wallet wallet = new WalletV3(walletAddress, apiClient);
            
            // Отримуємо адресу кошелька
            // Получаем адрес кошелька
            // Get wallet address
            Address address = wallet.getAddress();
            System.out.println("Wallet address: " + address.toRaw());
            
            // Отримуємо баланс
            // Получаем баланс
            // Get balance
            BigInteger balance = wallet.getBalance();
            System.out.println("Wallet balance: " + balance.toString() + " nanotons");
            
            // Створюємо транзакцію
            // Создаем транзакцию
            // Create transaction
            Address recipient = new Address("EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V");
            BigInteger amount = BigInteger.valueOf(1000000000L); // 1 TON
            String comment = "Test transfer";
            
            // В реальній реалізації тут має бути підпис транзакції приватним ключем
            // В реальной реализации здесь должна быть подпись транзакции приватным ключом
            // In real implementation, transaction should be signed with private key here
            
            System.out.println("To send a transaction:");
            System.out.println("1. Create a transaction using wallet.createTransfer()");
            System.out.println("2. Sign it with your private key");
            System.out.println("3. Send it using wallet.sendTransaction()");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}