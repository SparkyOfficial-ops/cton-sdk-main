// JettonExample.java - приклад використання Jetton
// Author: Андрій Будильников (Sparky)
// Example of using Jetton
// Пример использования Jetton

package com.cton.contract.example;

import com.cton.contract.Jetton;
import com.cton.sdk.Address;
import com.cton.api.TonApiClient;
import java.math.BigInteger;
import java.io.IOException;

/**
 * Приклад використання Jetton
 */
public class JettonExample {
    
    public static void main(String[] args) {
        try {
            System.out.println("CTON-SDK Jetton Example");
            System.out.println("======================");
            
            // Створюємо API клієнт
            // Создаем API клиент
            // Create API client
            TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/");
            
            // Створюємо адресу контракту токена (приклад)
            // Создаем адрес контракта токена (пример)
            // Create token contract address (example)
            Address jettonAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            
            // Створюємо Jetton об'єкт
            // Создаем Jetton объект
            // Create Jetton object
            Jetton jetton = new Jetton(jettonAddress, apiClient);
            
            // Отримуємо загальну емісію
            // Получаем общую эмиссию
            // Get total supply
            BigInteger totalSupply = jetton.getTotalSupply();
            System.out.println("Total supply: " + totalSupply.toString());
            
            // Отримуємо адресу minter контракту
            // Получаем адрес minter контракта
            // Get minter contract address
            Address minter = jetton.getJettonMinter();
            System.out.println("Minter address: " + minter.toRaw());
            
            // Отримуємо адресу власника (приклад)
            // Получаем адрес владельца (пример)
            // Get owner address (example)
            Address owner = new Address("EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V");
            
            // Отримуємо баланс користувача
            // Получаем баланс пользователя
            // Get user balance
            BigInteger balance = jetton.getBalance(owner);
            System.out.println("User balance: " + balance.toString());
            
            // Отримуємо адресу кошелька токенів для користувача
            // Получаем адрес кошелька токенов для пользователя
            // Get token wallet address for user
            Address walletAddress = jetton.getWalletAddress(owner);
            System.out.println("Token wallet address: " + walletAddress.toRaw());
            
            System.out.println("Jetton example completed!");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}