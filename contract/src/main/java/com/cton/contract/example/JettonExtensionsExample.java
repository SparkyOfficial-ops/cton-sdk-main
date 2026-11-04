package com.cton.contract.example;

import java.io.IOException;
import java.math.BigInteger;

import com.cton.api.TonApiClient;
import com.cton.contract.JettonExtensions;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;

/**
 * Builds a TEP-74 internal transfer. The owner's wallet signs and sends it.
 */
public class JettonExtensionsExample {

    public static void main(String[] args) {
        try {
            TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/jsonRPC");
            Address jettonAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            JettonExtensions jetton = new JettonExtensions(jettonAddress, apiClient);

            Address owner = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            Address recipient = new Address("Ef8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzM");
            BigInteger gas = new BigInteger("50000000");
            Cell internal = jetton.feelessTransfer(owner, recipient, BigInteger.valueOf(1000), gas);
            System.out.println("Jetton internal message bits: " + internal.getBitSize());
            System.out.println("Send this cell from the owner's TON wallet to the owner's jetton wallet.");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
