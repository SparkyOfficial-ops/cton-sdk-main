// MultiSigWalletExample.java - приклад використання багатопідписного кошелька
// Author: Андрій Будильников (Sparky)
// Example of using multi-signature wallet
// Пример использования многоподписного кошелька

package com.cton.contract.example;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.cton.api.TonApiClient;
import com.cton.contract.MultiSigWallet;
import com.cton.sdk.Address;

/**
 * Приклад використання багатопідписного кошелька
 */
public class MultiSigWalletExample {
    
    public static void main(String[] args) {
        try {
            System.out.println("CTON-SDK Multi-Signature Wallet Example");
            System.out.println("=======================================");
            
            // Створюємо API клієнт
            TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/");
            
            // Створюємо адресу кошелька
            Address walletAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            
            // Створюємо список підписувачів
            List<Address> signers = new ArrayList<>();
            signers.add(new Address("EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V"));
            signers.add(new Address("EQB5gf1BwO1KhrKgjuK3SeXBon-GT4s8jOIT2xHBg7mQr8OX"));
            signers.add(new Address("EQD7k5b5Xs1fK8h2d3s4a5b6c7d8e9f0g1h2i3j4k5l6m7n8"));
            
            // Створюємо багатопідписний кошик (2 з 3 підписи необхідні)
            MultiSigWallet multiSigWallet = new MultiSigWallet(
                walletAddress, 
                apiClient, 
                signers, 
                2, // Необхідно 2 підписи з 3
                698983191
            );
            
            // Отримуємо адресу кошелька
            Address address = multiSigWallet.getAddress();
            System.out.println("Multi-signature wallet address: " + address.toRaw());
            
            // Отримуємо список підписувачів
            List<Address> walletSigners = multiSigWallet.getSigners();
            System.out.println("Signers (" + walletSigners.size() + "):");
            for (int i = 0; i < walletSigners.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + walletSigners.get(i).toRaw());
            }
            
            // Отримуємо кількість необхідних підписів
            int requiredSignatures = multiSigWallet.getRequiredSignatures();
            System.out.println("Required signatures: " + requiredSignatures);
            
            // Отримуємо баланс
            BigInteger balance = multiSigWallet.getBalance();
            System.out.println("Wallet balance: " + balance.toString() + " nanotons");
            
            // Створюємо транзакцію, яка потребує багатопідпису
            Address recipient = new Address("EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V");
            BigInteger amount = BigInteger.valueOf(1000000000L); // 1 TON
            String comment = "Multi-signature transfer";
            
            // Створюємо транзакцію для багатопідпису
            com.cton.sdk.Cell multiSigTransaction = multiSigWallet.createMultiSigTransfer(recipient, amount, comment);
            System.out.println("Multi-signature transaction created");
            
            // У реальній реалізації тут має бути підпис транзакції кількома підписувачами
            // In real implementation, transaction should be signed by multiple signers here
            
            System.out.println("\nTo complete a multi-signature transaction:");
            System.out.println("1. Each signer signs the transaction with their private key");
            System.out.println("2. Collect the required number of signatures (" + requiredSignatures + ")");
            System.out.println("3. Combine signatures into final transaction");
            System.out.println("4. Send the final transaction");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}