package com.cton.contract;

import java.math.BigInteger;

import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

/**
 * Message bodies for STON.fi router v1.
 * Swap opcode {@code 0x25938561}, provide-liquidity opcode {@code 0xfcf9e58f}.
 * These cells are the forward payload of a jetton transfer to the router
 * wallet, or the body of a pTON transfer. They are not sent to the router
 * as an external message.
 */
public final class StonFi {
    public static final int OP_SWAP = 0x25938561;
    public static final int OP_PROVIDE_LP = 0xfcf9e58f;

    private StonFi() {
    }

    public static Cell swap(Address askWallet, BigInteger minAskAmount, Address recipient, Address referral) {
        CellBuilder body = new CellBuilder();
        body.storeUInt(32, OP_SWAP);
        body.storeAddress(askWallet);
        body.storeCoins(minAskAmount == null ? BigInteger.ZERO : minAskAmount);
        body.storeAddress(recipient);
        if (referral != null) {
            body.storeUInt(1, 1);
            body.storeAddress(referral);
        } else {
            body.storeUInt(1, 0);
        }
        return body.build();
    }

    public static Cell provideLp(Address otherRouterWallet, BigInteger minLpOut) {
        CellBuilder body = new CellBuilder();
        body.storeUInt(32, OP_PROVIDE_LP);
        body.storeAddress(otherRouterWallet);
        body.storeCoins(minLpOut == null ? BigInteger.ZERO : minLpOut);
        return body.build();
    }
}
