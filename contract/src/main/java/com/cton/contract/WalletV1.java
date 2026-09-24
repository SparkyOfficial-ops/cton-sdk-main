// WalletV1.java - реалізація кошелька версії 1
// Author: Андрій Будильников (Sparky)
// Wallet version 1 implementation
// Реализация кошелька версии 1

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

/**
 * Реалізація кошелька версії 1
 * 
 * Wallet version 1 implementation
 * Реализация кошелька версии 1
 */
public class WalletV1 extends BaseWallet {
    
    /**
     * Конструктор
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public WalletV1(Address address, TonApiClient apiClient) {
        super(address, apiClient);
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
            bodyBuilder.storeBytes(comment.getBytes());
        }
        
        Cell body = bodyBuilder.build();
        
        // Створюємо зовнішнє повідомлення
        CellBuilder messageBuilder = new CellBuilder();
        
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
        return 1;
    }
}