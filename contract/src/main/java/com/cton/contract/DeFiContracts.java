package com.cton.contract;

import java.math.BigInteger;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;

/**
 * Builders for STON.fi and DeDust messages. The cells are internal-message
 * bodies. Nothing here is broadcast, and there is no invented opcode.
 */
public class DeFiContracts extends Contract {
    
    public DeFiContracts(Address address, TonApiClient apiClient) {
        super(address, apiClient);
    }
    
    public Cell stonfiSwap(Address askWallet, BigInteger minAskAmount, Address recipient, Address referral) {
        return StonFi.swap(askWallet, minAskAmount, recipient, referral);
    }

    public Cell stonfiProvideLp(Address otherRouterWallet, BigInteger minLpOut) {
        return StonFi.provideLp(otherRouterWallet, minLpOut);
    }

    public Cell dedustSwap(long queryId, BigInteger amount, Address pool, Address recipient, long deadline) {
        return Dedust.swap(queryId, amount, pool, recipient, recipient, deadline);
    }
}
