// WalletV4.java - реалізація кошелька версії 4
// Author: Андрій Будильников (Sparky)
// Wallet version 4 implementation
// Реализация кошелька версии 4

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

/**
 * Реалізація кошелька версії 4
 * 
 * Wallet version 4 implementation
 * Реализация кошелька версии 4
 */
public class WalletV4 extends BaseWallet {
    private final int subwalletId;
    
    /**
     * Конструктор
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     * @param subwalletId ідентифікатор підкошелька
     */
    public WalletV4(Address address, TonApiClient apiClient, int subwalletId) {
        super(address, apiClient);
        this.subwalletId = subwalletId;
    }
    
    /**
     * Конструктор зі значенням subwalletId за замовчуванням
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public WalletV4(Address address, TonApiClient apiClient) {
        this(address, apiClient, 698983191);
    }
    
    // createTransfer() успадковується від BaseWallet і повертає стандартне
    // внутрішнє повідомлення (int_msg_info); повний цикл підпису — у BaseWallet.transfer()
    // (signing_body = subwallet_id + valid_until + seqno + send_mode + [Message]).
    
    @Override
    public int getVersion() throws IOException {
        return 4;
    }
    
    @Override
    public int getSubwalletId() throws IOException {
        return this.subwalletId;
    }
}