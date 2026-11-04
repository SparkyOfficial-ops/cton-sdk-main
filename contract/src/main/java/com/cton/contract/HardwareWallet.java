package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Boc;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

/**
 * Ledger account. Signing goes through a caller-supplied {@link LedgerTransport}
 * and the APDU layout in {@link LedgerApdu}. There is no simulated device.
 */
public class HardwareWallet extends BaseWallet {
    private final String deviceType;
    private final String deviceId;
    private final int accountIndex;
    private LedgerTransport transport;

    public HardwareWallet(Address address, TonApiClient apiClient, String deviceType,
                          String deviceId, int accountIndex) {
        super(address, apiClient);
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.accountIndex = accountIndex;
    }

    public HardwareWallet(Address address, TonApiClient apiClient, String deviceType, String deviceId) {
        this(address, apiClient, deviceType, deviceId, 0);
    }

    public void setTransport(LedgerTransport transport) {
        this.transport = transport;
    }

    @Override
    public Cell createTransfer(Address destination, BigInteger amount, String comment) throws IOException {
        return super.createTransfer(destination, amount, comment);
    }

    /**
     * Asks the Ledger to sign the transaction BOC and returns {@code signature || ^transaction}.
     */
    public Cell signTransactionOnDevice(Cell transaction) throws IOException {
        byte[] bocBytes;
        try (Boc boc = new Boc(transaction)) {
            bocBytes = boc.serialize(true, true);
        }
        if (bocBytes.length > 255) {
            throw new IOException("transaction BOC does not fit in one Ledger APDU");
        }
        byte[] apdu = LedgerApdu.command(
                LedgerApdu.INS_SIGN_TRANSACTION, accountIndex & 0xFF, 0, bocBytes);
        byte[] signature = exchange(apdu);
        if (signature.length != 64) {
            throw new IOException("Ledger signature must be 64 bytes, got " + signature.length);
        }
        return new CellBuilder().storeBytes(signature).storeRef(transaction).build();
    }

    /**
     * Reads the account address from the device. The response payload is
     * {@code workchain:int8 || account:bytes32} after any leading public key
     * when the payload is longer than 33 bytes.
     */
    public Address getAddressFromDevice() throws IOException {
        byte[] apdu = LedgerApdu.command(LedgerApdu.INS_GET_ADDRESS, accountIndex & 0xFF, 0, null);
        byte[] data = exchange(apdu);
        if (data.length < 33) {
            throw new IOException("Ledger address response is " + data.length + " bytes");
        }
        int hashAt = data.length - 32;
        byte workchain = data[hashAt - 1];
        byte[] hash = new byte[32];
        System.arraycopy(data, hashAt, hash, 0, 32);
        Address address = new Address();
        address.setWorkchain(workchain);
        address.setHashPart(hash);
        return address;
    }

    @Override
    public BigInteger getBalance() throws IOException {
        return super.getBalance();
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getAccountIndex() {
        return accountIndex;
    }

    @Override
    public int getVersion() throws IOException {
        byte[] data = exchange(LedgerApdu.command(LedgerApdu.INS_GET_APP_CONFIGURATION, 0, 0, null));
        if (data.length < 1) {
            throw new IOException("Ledger version response is empty");
        }
        return data[0] & 0xFF;
    }

    public boolean isDeviceConnected() {
        return transport != null && transport.isOpen();
    }

    public static List<String> getSupportedDevices() {
        return List.of("Ledger");
    }

    private byte[] exchange(byte[] apdu) throws IOException {
        if (!"Ledger".equals(deviceType)) {
            throw new IOException("APDU exchange is implemented for Ledger only");
        }
        if (transport == null || !transport.isOpen()) {
            throw new IOException("Ledger transport is not open");
        }
        byte[] response = transport.exchange(apdu);
        return LedgerApdu.payload(response);
    }
}
