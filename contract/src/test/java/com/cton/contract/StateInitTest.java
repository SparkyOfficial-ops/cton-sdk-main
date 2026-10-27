package com.cton.contract;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class StateInitTest {

    @Test
    public void walletV3R2AndV4R2CodesDeserialize() {
        byte[] publicKey = new byte[32];
        StateInit v3 = StateInit.forWallet(3, 698983191L, publicKey);
        StateInit v4 = StateInit.forWallet(4, 698983191L, publicKey);
        assertNotNull(v3.toCell());
        assertNotNull(v4.toCell());
        assertNotNull(v3.getCode());
        assertNotNull(v4.getData());
    }

    @Test
    public void rejectsUnknownWalletVersion() {
        assertThrows(IllegalArgumentException.class,
            () -> StateInit.forWallet(2, 0, new byte[32]));
    }
}
