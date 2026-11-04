package com.cton.contract.example;

import java.util.List;

import com.cton.api.TonApiClient;
import com.cton.contract.HardwareWallet;
import com.cton.contract.LedgerApdu;
import com.cton.sdk.Address;

/**
 * Ledger support stops at the APDU. A {@code LedgerTransport} has to carry
 * the bytes to the device; this example does not pretend a device answered.
 */
public class HardwareWalletExample {

    public static void main(String[] args) {
        TonApiClient apiClient = new TonApiClient("https://toncenter.com/api/v2/");
        Address walletAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
        HardwareWallet hardwareWallet = new HardwareWallet(
                walletAddress, apiClient, "Ledger", "ledger_device_id_12345", 0);

        System.out.println("Device type: " + hardwareWallet.getDeviceType());
        System.out.println("Transport open: " + hardwareWallet.isDeviceConnected());
        byte[] apdu = LedgerApdu.command(LedgerApdu.INS_GET_ADDRESS, 0, 0, null);
        System.out.println("GET_ADDRESS APDU length: " + apdu.length);
        List<String> supportedDevices = HardwareWallet.getSupportedDevices();
        System.out.println("Supported hardware wallets: " + supportedDevices);
        System.out.println("Attach a LedgerTransport before signTransactionOnDevice or getAddressFromDevice.");
    }
}
