// NftExample.java - приклад використання NFT
// Author: Андрій Будильников (Sparky)
// Example of using NFT
// Пример использования NFT

package com.cton.contract.example;

import com.cton.contract.Nft;
import com.cton.sdk.Address;
import com.cton.api.TonApiClient;
import java.io.IOException;

/**
 * Приклад використання NFT
 */
public class NftExample {
    
    public static void main(String[] args) {
        try {
            System.out.println("CTON-SDK NFT Example");
            System.out.println("===================");
            
            // Створюємо API клієнт
            // Создаем API клиент
            // Create API client
            TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/");
            
            // Створюємо адресу NFT контракту (приклад)
            // Создаем адрес NFT контракта (пример)
            // Create NFT contract address (example)
            Address nftAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            
            // Створюємо NFT об'єкт
            // Создаем NFT объект
            // Create NFT object
            Nft nft = new Nft(nftAddress, apiClient);
            
            // Отримуємо інформацію про NFT
            // Получаем информацию о NFT
            // Get NFT information
            // JsonObject nftData = nft.getNftData();
            System.out.println("NFT data retrieved");
            
            // Отримуємо індекс NFT
            // Получаем индекс NFT
            // Get NFT index
            long index = nft.getIndex();
            System.out.println("NFT index: " + index);
            
            // Отримуємо адресу колекції
            // Получаем адрес коллекции
            // Get collection address
            Address collection = nft.getCollection();
            System.out.println("Collection address: " + collection.toRaw());
            
            // Отримуємо власника NFT
            // Получаем владельца NFT
            // Get NFT owner
            Address owner = nft.getOwner();
            System.out.println("NFT owner: " + owner.toRaw());
            
            System.out.println("NFT example completed!");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}