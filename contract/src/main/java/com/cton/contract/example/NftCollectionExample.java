// NftCollectionExample.java - приклад використання NFT Collection
// Author: Андрій Будильников (Sparky)
// Example of using NFT Collection
// Пример использования NFT Collection

package com.cton.contract.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cton.api.TonApiClient;
import com.cton.contract.NftCollection;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;
import com.cton.sdk.Crypto;

/**
 * Приклад використання NFT Collection
 * 
 * Example of using NFT Collection
 * Пример использования NFT Collection
 */
public class NftCollectionExample {
    
    public static void main(String[] args) {
        try {
            // Ініціалізуємо API клієнт
            // Инициализируем API клиент
            // Initialize API client
            TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/jsonRPC");
            
            // Адреса контракту колекції
            // Адрес контракта коллекции
            // Collection contract address
            Address collectionAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            
            // Створюємо екземпляр NFT Collection
            // Создаем экземпляр NFT Collection
            // Create NFT Collection instance
            NftCollection collection = new NftCollection(collectionAddress, apiClient);
            
            // Отримуємо інформацію про колекцію
            // Получаем информацию о коллекции
            // Get collection information
            System.out.println("Next item index: " + collection.getNextItemIndex());
            System.out.println("Owner: " + collection.getOwner().toRaw());
            
            // Створюємо контент для NFT
            // Создаем контент для NFT
            // Create content for NFT
            CellBuilder contentBuilder = new CellBuilder();
            contentBuilder.storeBytes("NFT Content Example".getBytes());
            Cell content = contentBuilder.build();
            
            // Створюємо приватний ключ (для прикладу)
            // Создаем приватный ключ (для примера)
            // Create private key (for example)
            Crypto.PrivateKey privateKey = Crypto.PrivateKey.generate();
            
            // Адреса власника
            // Адрес владельца
            // Owner address
            Address owner = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            
            // Створюємо один NFT
            // Создаем один NFT
            // Create one NFT
            long nextIndex = collection.getNextItemIndex();
            collection.mintNft(owner, content, nextIndex, privateKey);
            System.out.println("Minted NFT with index: " + nextIndex);
            
            // Масове створення NFT
            // Массовое создание NFT
            // Batch creation of NFTs
            List<Cell> contents = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                CellBuilder batchContentBuilder = new CellBuilder();
                batchContentBuilder.storeBytes(("NFT Content #" + (i + 1)).getBytes());
                contents.add(batchContentBuilder.build());
            }
            
            collection.batchMint(owner, contents, privateKey);
            System.out.println("Batch minted 5 NFTs");
            
            // Отримуємо адресу NFT за індексом
            // Получаем адрес NFT по индексу
            // Get NFT address by index
            Address nftAddress = collection.getNftAddressByIndex(0);
            System.out.println("NFT address at index 0: " + nftAddress.toRaw());
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}