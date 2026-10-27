// HashmapE.java - дерево HashmapE з TL-B TON
// Author: Андрій Будильников (Sparky)

package com.cton.contract;

import java.util.ArrayList;
import java.util.List;

import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

/**
 * HashmapE n X: порожній словник — один біт 0, інакше біт 1 і посилання на корінь.
 * Мітка ребра — hml_short, розвилка — два дочірні ребра. В одній комірці не більше двох посилань.
 */
final class HashmapE {
    interface ValueWriter {
        void write(CellBuilder builder);
    }

    static final class Entry {
        final int key;
        final ValueWriter writer;

        Entry(int key, ValueWriter writer) {
            this.key = key;
            this.writer = writer;
        }
    }

    private HashmapE() {
    }

    static void store(CellBuilder builder, int keyBits, List<Entry> entries) {
        if (keyBits <= 0 || keyBits > 16) {
            throw new IllegalArgumentException("keyBits must be in 1..16");
        }
        if (entries == null || entries.isEmpty()) {
            builder.storeBit(false);
            return;
        }
        builder.storeBit(true);
        builder.storeRef(buildEdge(keyBits, keyBits, 0, entries));
    }

    private static Cell buildEdge(int keyBits, int remaining, int offset, List<Entry> entries) {
        int common = commonPrefix(keyBits, remaining, offset, entries);
        CellBuilder edge = new CellBuilder();
        writeShortLabel(edge, keyBits, common, offset, entries.get(0).key);
        if (common == remaining) {
            if (entries.size() != 1) {
                throw new IllegalArgumentException("Duplicate hashmap key");
            }
            entries.get(0).writer.write(edge);
            return edge.build();
        }

        int branchAt = offset + common;
        List<Entry> left = new ArrayList<>();
        List<Entry> right = new ArrayList<>();
        for (Entry entry : entries) {
            if (bit(entry.key, keyBits, branchAt)) {
                right.add(entry);
            } else {
                left.add(entry);
            }
        }
        if (left.isEmpty() || right.isEmpty()) {
            throw new IllegalStateException("Hashmap split produced an empty branch");
        }
        edge.storeRef(buildEdge(keyBits, remaining - common - 1, branchAt + 1, left));
        edge.storeRef(buildEdge(keyBits, remaining - common - 1, branchAt + 1, right));
        return edge.build();
    }

    private static int commonPrefix(int keyBits, int remaining, int offset, List<Entry> entries) {
        int length = 0;
        while (length < remaining) {
            boolean value = bit(entries.get(0).key, keyBits, offset + length);
            boolean same = true;
            for (int i = 1; i < entries.size(); i++) {
                if (bit(entries.get(i).key, keyBits, offset + length) != value) {
                    same = false;
                    break;
                }
            }
            if (!same) {
                break;
            }
            length++;
        }
        return length;
    }

    /** hml_short$0: тег 0, унарна довжина, потім біти ключа. */
    private static void writeShortLabel(CellBuilder builder, int keyBits, int length, int offset, int key) {
        builder.storeBit(false);
        for (int i = 0; i < length; i++) {
            builder.storeBit(true);
        }
        builder.storeBit(false);
        for (int i = 0; i < length; i++) {
            builder.storeBit(bit(key, keyBits, offset + i));
        }
    }

    /** Біт ключа зліва направо: index 0 — старший біт. */
    private static boolean bit(int key, int keyBits, int index) {
        return ((key >>> (keyBits - 1 - index)) & 1) == 1;
    }
}
