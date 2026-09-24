// HighloadWallet.java - реалізація високонавантаженого кошелька
// Author: Андрій Будильников (Sparky)
// Highload wallet implementation
// Реализация высоконагруженного кошелька

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;
import com.cton.sdk.Crypto;

/**
 * Реалізація високонавантаженого кошелька (Highload Wallet v2)
 * 
 * Highload wallet implementation (Highload Wallet v2)
 * Реализация высоконагруженного кошелька (Highload Wallet v2)
 */
public class HighloadWallet extends BaseWallet {
    private final int subwalletId;
    
    /**
     * Конструктор
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     * @param subwalletId ідентифікатор підкошелька
     */
    public HighloadWallet(Address address, TonApiClient apiClient, int subwalletId) {
        super(address, apiClient);
        this.subwalletId = subwalletId;
    }
    
    /**
     * Конструктор зі значенням subwalletId за замовчуванням
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public HighloadWallet(Address address, TonApiClient apiClient) {
        this(address, apiClient, 698983191);
    }
    
    @Override
    public Cell createTransfer(Address destination, BigInteger amount, String comment) throws IOException {
        // Для високонавантаженого кошелька створюємо один елемент масиву
        BulkTransfer[] transfers = new BulkTransfer[] {
            new BulkTransfer(destination, amount, comment)
        };
        return createBulkTransfer(transfers);
    }
    
    @Override
    public Cell createBulkTransfer(BulkTransfer[] transfers) throws IOException {
        // Створюємо комірку з багатьма транзакціями
        CellBuilder builder = new CellBuilder();
        
        // Додаємо ідентифікатор підкошелька
        builder.storeUInt(32, this.subwalletId);
        
        // Додаємо час закінчення дії (expire_at) - поточний час + 1 година
        long expireAt = Instant.now().getEpochSecond() + 3600;
        builder.storeUInt(32, expireAt);
        
        // Створюємо комірку для транзакцій
        CellBuilder transferBuilder = new CellBuilder();
        
        // Додаємо транзакції (до 254 транзакцій)
        for (int i = 0; i < Math.min(transfers.length, 254); i++) {
            BulkTransfer transfer = transfers[i];
            
            // Додаємо індекс транзакції
            transferBuilder.storeUInt(16, i);
            
            // Додаємо режим транзакції
            transferBuilder.storeUInt(8, transfer.getMode());
            
            // Створюємо комірку для транзакції
            CellBuilder txBuilder = new CellBuilder();
            
            // Додаємо адресу одержувача
            txBuilder.storeAddress(transfer.getDestination());
            
            // Додаємо суму
            txBuilder.storeCoins(transfer.getAmount());
            
            // Додаємо коментар якщо він є
            if (transfer.getComment() != null && !transfer.getComment().isEmpty()) {
                CellBuilder commentBuilder = new CellBuilder();
                commentBuilder.storeUInt(32, 0); // Текст коментаря
                commentBuilder.storeBytes(transfer.getComment().getBytes());
                txBuilder.storeRef(commentBuilder.build());
            } else {
                // Порожній ref для коментаря
                txBuilder.storeRef(new CellBuilder().build());
            }
            
            // Додаємо транзакцію до комірки транзакцій
            transferBuilder.storeRef(txBuilder.build());
        }
        
        // Додаємо комірку транзакцій
        builder.storeRef(transferBuilder.build());
        
        // Додаємо підпис (буде додано під час відправки)
        return builder.build();
    }
    
    @Override
    public int getVersion() throws IOException {
        return 2; // Highload Wallet v2
    }
    
    @Override
    public int getSubwalletId() throws IOException {
        return this.subwalletId;
    }
    
    /**
     * Надіслати багато транзакцій
     * @param transfers масив транзакцій
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void sendBulkTransfer(BulkTransfer[] transfers, Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо багато транзакцій
        Cell message = createBulkTransfer(transfers);
        
        // Підписуємо повідомлення приватним ключем
        com.cton.sdk.Boc boc = new com.cton.sdk.Boc(message);
        byte[] messageBytes = boc.serialize(true, true);
        byte[] signature = com.cton.sdk.Crypto.sign(privateKey, messageBytes);
        
        // Створюємо зовнішнє повідомлення з підписом
        CellBuilder externalMessageBuilder = new CellBuilder();
        externalMessageBuilder.storeBytes(signature); // Додаємо підпис
        externalMessageBuilder.storeRef(message); // Додаємо повідомлення
        
        Cell externalMessage = externalMessageBuilder.build();
        
        // Надсилаємо через API
        sendTransaction(externalMessage);
    }
}