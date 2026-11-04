package com.cton.contract;

import java.math.BigInteger;

import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

/**
 * Native-vault swap body for DeDust. Opcode {@code 0xea06185d}.
 * The vault receives this body as an internal message together with the TON
 * being swapped.
 */
public final class Dedust {
    public static final int OP_SWAP = 0xea06185d;
    /** {@code given_in}: the attached amount is the input. */
    public static final int SWAP_KIND_GIVEN_IN = 0;

    private Dedust() {
    }

    public static Cell swap(long queryId, BigInteger amount, Address pool, Address recipient,
                            Address excess, long deadline) {
        CellBuilder params = new CellBuilder();
        params.storeUInt(32, deadline);
        params.storeAddress(recipient);
        params.storeAddress(excess == null ? recipient : excess);
        params.storeUInt(1, 0);
        params.storeUInt(1, 0);

        CellBuilder body = new CellBuilder();
        body.storeUInt(32, OP_SWAP);
        body.storeUInt(64, queryId);
        body.storeCoins(amount == null ? BigInteger.ZERO : amount);
        body.storeAddress(pool);
        body.storeUInt(1, SWAP_KIND_GIVEN_IN);
        body.storeCoins(BigInteger.ZERO);
        body.storeUInt(1, 0);
        body.storeRef(params.build());
        return body.build();
    }
}
