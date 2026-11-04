package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;

import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

/**
 * A user's jetton wallet (TEP-74). Transfers are internal messages to this
 * contract, never external messages to the minter.
 */
public final class JettonWallet {
    public static final int OP_TRANSFER = 0x0f8a7ea5;

    private final Address walletAddress;

    public JettonWallet(Address walletAddress) {
        if (walletAddress == null) {
            throw new IllegalArgumentException("jetton wallet address is required");
        }
        this.walletAddress = walletAddress;
    }

    /**
     * Resolves the owner's jetton wallet through the minter's {@code get_wallet_address}.
     */
    public static JettonWallet ofOwner(Jetton minter, Address owner) throws IOException {
        if (minter == null) {
            throw new IllegalArgumentException("jetton minter is required");
        }
        return new JettonWallet(minter.getWalletAddress(owner));
    }

    public Address address() {
        return walletAddress;
    }

    /**
     * TEP-74 {@code transfer#0f8a7ea5} body.
     */
    public Cell createTransferBody(long queryId, Address destination, BigInteger jettonAmount,
                                   Address responseDestination, BigInteger forwardTonAmount,
                                   String comment) {
        if (destination == null || responseDestination == null) {
            throw new IllegalArgumentException("destination and response destination are required");
        }
        CellBuilder body = new CellBuilder();
        body.storeUInt(32, OP_TRANSFER);
        body.storeUInt(64, queryId);
        body.storeCoins(jettonAmount == null ? BigInteger.ZERO : jettonAmount);
        body.storeAddress(destination);
        body.storeAddress(responseDestination);
        body.storeUInt(1, 0);
        body.storeCoins(forwardTonAmount == null ? BigInteger.ZERO : forwardTonAmount);
        if (comment != null && !comment.isEmpty()) {
            CellBuilder payload = new CellBuilder();
            payload.storeUInt(32, 0);
            payload.storeBytes(comment.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            body.storeUInt(1, 1);
            body.storeRef(payload.build());
        } else {
            body.storeUInt(1, 0);
        }
        return body.build();
    }

    /**
     * Internal message the owner's TON wallet sends to this jetton wallet.
     * {@code tonAttached} pays for the jetton wallet's execution.
     */
    public Cell createInternalTransfer(Address ownerWallet, Address destination, BigInteger jettonAmount,
                                       BigInteger tonAttached, BigInteger forwardTonAmount,
                                       String comment, boolean bounce) {
        Cell body = createTransferBody(0, destination, jettonAmount, ownerWallet, forwardTonAmount, comment);
        CellBuilder message = new CellBuilder();
        message.storeUInt(1, 0);
        message.storeUInt(1, 1);
        message.storeUInt(1, bounce ? 1 : 0);
        message.storeUInt(1, 0);
        message.storeAddress(ownerWallet);
        message.storeAddress(walletAddress);
        message.storeCoins(tonAttached == null ? BigInteger.ZERO : tonAttached);
        message.storeUInt(1, 0);
        message.storeCoins(BigInteger.ZERO);
        message.storeCoins(BigInteger.ZERO);
        message.storeUInt(64, 0);
        message.storeUInt(32, Instant.now().getEpochSecond());
        message.storeUInt(1, 0);
        message.storeUInt(1, 1);
        message.storeRef(body);
        return message.build();
    }
}
