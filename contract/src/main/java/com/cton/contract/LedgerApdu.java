package com.cton.contract;

/**
 * ISO 7816-4 APDU codec for the TON Ledger application.
 * CLA is {@code 0xE0}. Instruction codes follow the app:
 * configuration {@code 0x01}, public key {@code 0x02}, sign {@code 0x03},
 * address {@code 0x04}, transaction {@code 0x05}.
 */
public final class LedgerApdu {
    public static final int CLA = 0xE0;
    public static final int INS_GET_APP_CONFIGURATION = 0x01;
    public static final int INS_GET_PUBLIC_KEY = 0x02;
    public static final int INS_SIGN = 0x03;
    public static final int INS_GET_ADDRESS = 0x04;
    public static final int INS_SIGN_TRANSACTION = 0x05;
    public static final int SW_OK = 0x9000;

    private LedgerApdu() {
    }

    public static byte[] command(int ins, int p1, int p2, byte[] data) {
        byte[] payload = data == null ? new byte[0] : data;
        if (payload.length > 255) {
            throw new IllegalArgumentException("APDU data must be 255 bytes or less; send the next chunk separately");
        }
        byte[] apdu = new byte[5 + payload.length];
        apdu[0] = (byte) CLA;
        apdu[1] = (byte) ins;
        apdu[2] = (byte) p1;
        apdu[3] = (byte) p2;
        apdu[4] = (byte) payload.length;
        System.arraycopy(payload, 0, apdu, 5, payload.length);
        return apdu;
    }

    public static int statusWord(byte[] response) {
        if (response == null || response.length < 2) {
            throw new IllegalArgumentException("Ledger response has no status word");
        }
        int sw1 = response[response.length - 2] & 0xFF;
        int sw2 = response[response.length - 1] & 0xFF;
        return (sw1 << 8) | sw2;
    }

    public static byte[] payload(byte[] response) {
        int status = statusWord(response);
        if (status != SW_OK) {
            throw new IllegalArgumentException(String.format("Ledger status 0x%04X", status));
        }
        byte[] data = new byte[response.length - 2];
        System.arraycopy(response, 0, data, 0, data.length);
        return data;
    }
}
