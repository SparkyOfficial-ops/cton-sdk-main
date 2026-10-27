// WalletV4.java - реалізація кошелька версії 4
// Author: Андрій Будильников (Sparky)
// Wallet version 4 implementation
// Реализация кошелька версии 4

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;

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
    
    /**
     * Wallet V4 після seqno очікує opcode. 0 — просте надсилання: далі send_mode і внутрішнє повідомлення.
     */
    @Override
    protected void storeOutgoingAction(CellBuilder builder, int sendMode, Cell orderMessage) {
        storeOutgoingActions(builder, new int[] { sendMode }, java.util.Collections.singletonList(orderMessage));
    }

    @Override
    protected void storeOutgoingActions(CellBuilder builder, int[] sendModes, java.util.List<Cell> orderMessages) {
        builder.storeUInt(8, 0);
        super.storeOutgoingActions(builder, sendModes, orderMessages);
    }

    @Override
    public Cell createTransfer(Address destination, BigInteger amount, String comment) throws IOException {
        // Wallet V4R2 простий переказ: внутрішнє повідомлення (int_msg_info)
        // з тілом op=0x0 (+ коментар). Тіло гаманцевої транзакції (subwallet_id +
        // valid_until + seqno + ref) формує BaseWallet.transfer().
        CellBuilder bodyBuilder = new CellBuilder();
        bodyBuilder.storeUInt(32, 0); // op code
        if (comment != null && !comment.isEmpty()) {
            bodyBuilder.storeBytes(comment.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        Cell body = bodyBuilder.build();
        return buildInternalMessage(destination, amount, body, true);
    }
    
    @Override
    public int getVersion() throws IOException {
        return 4;
    }
    
    @Override
    public int getSubwalletId() throws IOException {
        return this.subwalletId;
    }
}