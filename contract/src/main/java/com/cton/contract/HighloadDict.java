package com.cton.contract;

import java.util.List;

import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;
import com.cton.sdk.Hashmap;

/**
 * HashmapE 16 used by Highload Wallet v2. Each value is {@code send_mode:uint8}
 * followed by a reference to the internal message. The tree itself is built
 * by the native Hashmap implementation.
 */
final class HighloadDict {
    private HighloadDict() {
    }

    static Cell root(List<Order> orders) {
        if (orders.isEmpty() || orders.size() > 254) {
            throw new IllegalArgumentException("a highload batch must contain 1..254 transfers");
        }
        long[] keys = new long[orders.size()];
        Cell[] values = new Cell[orders.size()];
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            keys[i] = order.index;
            values[i] = new CellBuilder()
                    .storeUInt(8, order.mode)
                    .storeRef(order.message)
                    .build();
        }
        return Hashmap.build(16, keys, values);
    }

    static final class Order {
        final int index;
        final int mode;
        final Cell message;

        Order(int index, int mode, Cell message) {
            if (index < 0 || index > 0xFFFF) {
                throw new IllegalArgumentException("highload dictionary key must fit in 16 bits");
            }
            this.index = index;
            this.mode = mode;
            this.message = message;
        }
    }
}
