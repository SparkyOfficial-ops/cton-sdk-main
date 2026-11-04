package com.cton.contract;

import java.io.IOException;

/**
 * HID or USB transport that exchanges one APDU with a Ledger device.
 * This SDK does not open the device itself.
 */
public interface LedgerTransport {
    boolean isOpen();

    byte[] exchange(byte[] apdu) throws IOException;
}
