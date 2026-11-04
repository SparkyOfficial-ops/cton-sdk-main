package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;

/**
 * Jetton helpers that stay on TEP-74. A transfer is an internal message to
 * the sender's jetton wallet, with no forward TON.
 */
public class JettonExtensions extends Jetton {

    public JettonExtensions(Address address, TonApiClient apiClient) {
        super(address, apiClient);
    }

    /**
     * Jetton transfer with {@code forward_ton_amount = 0}. The returned cell
     * is the internal message the owner's wallet sends to its jetton wallet.
     * {@code tonForGas} is the TON attached so the jetton wallet can run.
     */
    public Cell feelessTransfer(Address owner, Address recipient, BigInteger amount,
                                BigInteger tonForGas) throws IOException {
        return JettonWallet.ofOwner(this, owner).createInternalTransfer(
                owner, recipient, amount, tonForGas, BigInteger.ZERO, null, true);
    }
}
