// JettonExtensionsExample.java - приклад використання розширень Jetton
// Author: Андрій Будильников (Sparky)
// Example of using Jetton Extensions
// Пример использования расширений Jetton

package com.cton.contract.example;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

import com.cton.api.TonApiClient;
import com.cton.contract.JettonExtensions;
import com.cton.sdk.Address;
import com.cton.sdk.Crypto;

/**
 * Приклад використання розширень Jetton
 * 
 * Example of using Jetton Extensions
 * Пример использования расширений Jetton
 */
public class JettonExtensionsExample {
    
    public static void main(String[] args) {
        try {
            // Ініціалізуємо API клієнт
            // Инициализируем API клиент
            // Initialize API client
            TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/jsonRPC");
            
            // Адреса контракту токена
            // Адрес контракта токена
            // Token contract address
            Address jettonAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            
            // Створюємо екземпляр Jetton Extensions
            // Создаем экземпляр Jetton Extensions
            // Create Jetton Extensions instance
            JettonExtensions jetton = new JettonExtensions(jettonAddress, apiClient);
            
            // Отримуємо баланс користувача
            // Получаем баланс пользователя
            // Get user balance
            Address userAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            BigInteger balance = jetton.getBalance(userAddress);
            System.out.println("User balance: " + balance.toString());
            
            // Створюємо приватний ключ (для прикладу)
            // Создаем приватный ключ (для примера)
            // Create private key (for example)
            Crypto.PrivateKey privateKey = Crypto.PrivateKey.generate();
            
            // Адреса кошелька відправника
            // Адрес кошелька отправителя
            // Sender wallet address
            Address senderWallet = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            
            // Адреса одержувача
            // Адрес получателя
            // Recipient address
            Address recipient = new Address("Ef8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzM");
            
            // Безкомісійний переказ
            // Бесплатный перевод
            // Feeless transfer
            jetton.feelessTransfer(senderWallet, recipient, BigInteger.valueOf(1000), privateKey);
            System.out.println("Feeless transfer completed");
            
            // Створення vesting розкладу
            // Создание vesting расписания
            // Create vesting schedule
            Date startDate = new Date();
            Date endDate = new Date(startDate.getTime() + 365L * 24 * 60 * 60 * 1000); // 1 рік
            Date cliffDate = new Date(startDate.getTime() + 30L * 24 * 60 * 60 * 1000); // 30 днів
            
            Address beneficiary = new Address("Ef8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzM");
            jetton.createVestingSchedule(beneficiary, BigInteger.valueOf(1000000), 
                                       startDate, endDate, cliffDate, privateKey);
            System.out.println("Vesting schedule created");
            
            // Голосування за пропозицію governance
            // Голосование за предложение governance
            // Vote on governance proposal
            long proposalId = 12345;
            jetton.voteOnProposal(proposalId, true, senderWallet, privateKey);
            System.out.println("Voted on proposal #" + proposalId);
            
            // Отримання інформації про пропозицію
            // Получение информации о предложении
            // Get proposal information
            System.out.println("Proposal info: " + jetton.getProposalInfo(proposalId).toString());
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}