// DeFiContractsExample.java - приклад використання DeFi контрактів
// Author: Андрій Будильников (Sparky)
// Example of using DeFi contracts
// Пример использования DeFi контрактов

package com.cton.contract.example;

import java.io.IOException;
import java.math.BigInteger;

import com.cton.api.TonApiClient;
import com.cton.contract.DeFiContracts;
import com.cton.sdk.Address;
import com.cton.sdk.Crypto;

/**
 * Приклад використання DeFi контрактів
 * 
 * Example of using DeFi contracts
 * Пример использования DeFi контрактов
 */
public class DeFiContractsExample {
    
    public static void main(String[] args) {
        try {
            // Ініціалізуємо API клієнт
            // Инициализируем API клиент
            // Initialize API client
            TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/jsonRPC");
            
            // Адреса контракту DeFi
            // Адрес контракта DeFi
            // DeFi contract address
            Address defiAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            
            // Створюємо екземпляр DeFi контрактів
            // Создаем экземпляр DeFi контрактов
            // Create DeFi contracts instance
            DeFiContracts defi = new DeFiContracts(defiAddress, apiClient);
            
            // Створюємо приватний ключ (для прикладу)
            // Создаем приватный ключ (для примера)
            // Create private key (for example)
            Crypto.PrivateKey privateKey = Crypto.PrivateKey.generate();
            
            // Адреса кошелька ліквідності
            // Адрес кошелька ликвидности
            // Liquidity wallet address
            Address liquidityWallet = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            
            // Додавання ліквідності до пулу
            // Добавление ликвидности в пул
            // Add liquidity to pool
            defi.addLiquidity(BigInteger.valueOf(1000), BigInteger.valueOf(2000), 
                            liquidityWallet, privateKey);
            System.out.println("Added liquidity to pool");
            
            // Видалення ліквідності з пулу
            // Удаление ликвидности из пула
            // Remove liquidity from pool
            defi.removeLiquidity(BigInteger.valueOf(500), liquidityWallet, privateKey);
            System.out.println("Removed liquidity from pool");
            
            // Адреса кошелька стейкінгу
            // Адрес кошелька стейкинга
            // Staking wallet address
            Address stakingWallet = new Address("Ef8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzM");
            
            // Стейкінг токенів
            // Стейкинг токенов
            // Stake tokens
            defi.stakeTokens(BigInteger.valueOf(10000), stakingWallet, privateKey);
            System.out.println("Staked tokens");
            
            // Виведення стейкінгу
            // Вывод стейкинга
            // Unstake tokens
            defi.unstakeTokens(BigInteger.valueOf(5000), stakingWallet, privateKey);
            System.out.println("Unstaked tokens");
            
            // Адреса кошелька кредитування
            // Адрес кошелька кредитования
            // Lending wallet address
            Address lendingWallet = new Address("Ef8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzM");
            
            // Взяття кредиту
            // Взятие кредита
            // Take a loan
            defi.takeLoan(BigInteger.valueOf(5000), BigInteger.valueOf(10000), 
                         lendingWallet, privateKey);
            System.out.println("Took a loan");
            
            // Погашення кредиту
            // Погашение кредита
            // Repay a loan
            defi.repayLoan(BigInteger.valueOf(10000), lendingWallet, privateKey);
            System.out.println("Repaid a loan");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}