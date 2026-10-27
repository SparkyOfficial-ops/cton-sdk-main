package com.cton.contract;

import java.math.BigInteger;

import com.cton.sdk.Address;
import com.cton.sdk.Cell;

/**
 * Одне внутрішнє повідомлення всередині зовнішнього повідомлення гаманця.
 */
public final class OutgoingTransfer {
    final Address destination;
    final BigInteger amount;
    final Cell body;
    final Cell stateInit;
    final int sendMode;
    final boolean bounce;

    public OutgoingTransfer(Address destination, BigInteger amount, Cell body,
                            Cell stateInit, int sendMode, boolean bounce) {
        this.destination = destination;
        this.amount = amount;
        this.body = body;
        this.stateInit = stateInit;
        this.sendMode = sendMode;
        this.bounce = bounce;
    }
}
