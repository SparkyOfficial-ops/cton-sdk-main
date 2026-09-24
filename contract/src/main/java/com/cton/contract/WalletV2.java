// WalletV2.java - реалізація кошелька версії 2
// Author: Андрій Будильников (Sparky)
// Wallet version 2 implementation
// Реализация кошелька версии 2

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

/**
 * Реалізація кошелька версії 2
 * 
 * Wallet version 2 implementation
 * Реализация кошелька версии 2
 */
public class WalletV2 extends BaseWallet {
    private final int subwalletId;
    
    /**
     * Конструктор
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     * @param subwalletId ідентифікатор підкошелька
     */
    public WalletV2(Address address, TonApiClient apiClient, int subwalletId) {
        super(address, apiClient);
        this.subwalletId = subwalletId;
    }
    
    /**
     * Конструктор зі значенням subwalletId за замовчуванням
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public WalletV2(Address address, TonApiClient apiClient) {
        this(address, apiClient, 698983191);
    }
    
    @Override
    public Cell createTransfer(Address destination, BigInteger amount, String comment) throws IOException {
        // Отримуємо послідовний номер
        long seqno = getSeqno();
        
        // Створюємо комірку з тілом повідомлення
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо код операції (0 для простого переказу)
        bodyBuilder.storeUInt(32, 0);
        
        // Додаємо коментар якщо він є
        if (comment != null && !comment.isEmpty()) {
            bodyBuilder.storeUInt(32, 0); // Текст коментаря
            bodyBuilder.storeBytes(comment.getBytes());
        }
        
        Cell body = bodyBuilder.build();
        
        // Створюємо зовнішнє повідомлення
        CellBuilder messageBuilder = new CellBuilder();
        
        // Додаємо ідентифікатор підкошелька
        messageBuilder.storeUInt(32, this.subwalletId);
        
        // Додаємо послідовний номер
        messageBuilder.storeUInt(32, seqno);
        
        // Додаємо адресу одержувача
        messageBuilder.storeAddress(destination);
        
        // Додаємо суму
        messageBuilder.storeCoins(amount);
        
        // Додаємо тіло повідомлення
        messageBuilder.storeRef(body);
        
        return messageBuilder.build();
    }
    
    @Override
    public int getVersion() throws IOException {
        return 2;
    }
    
    @Override
    public int getSubwalletId() throws IOException {
        return this.subwalletId;
    }
}