package com.cton.contract.example;

import java.math.BigInteger;

import com.cton.api.TonApiClient;
import com.cton.contract.DeFiContracts;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;

/**
 * Builds STON.fi and DeDust internal-message bodies.
 */
public class DeFiContractsExample {

    public static void main(String[] args) {
        TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/jsonRPC");
        Address router = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
        DeFiContracts defi = new DeFiContracts(router, apiClient);

        Address wallet = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
        Address pool = new Address("Ef8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzM");

        Cell swap = defi.stonfiSwap(wallet, BigInteger.valueOf(1), wallet, null);
        Cell lp = defi.stonfiProvideLp(wallet, BigInteger.ONE);
        Cell dedust = defi.dedustSwap(0, BigInteger.valueOf(1_000_000_000L), pool, wallet, 0);
        System.out.println("STON.fi swap bits: " + swap.getBitSize());
        System.out.println("STON.fi provide LP bits: " + lp.getBitSize());
        System.out.println("DeDust swap bits: " + dedust.getBitSize());
    }
}
