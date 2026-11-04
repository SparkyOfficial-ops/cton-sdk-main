package com.cton.sdk;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Hashmap from the TON TL-B spec. {@link #build} returns the dictionary root
 * stored in {@code hme_root$1}. The caller writes the HashmapE prefix bit.
 */
public final class Hashmap {
    public interface CtonLibrary extends Library {
        CtonLibrary INSTANCE = Native.load("cton-sdk-core", CtonLibrary.class);

        Pointer hashmap_build(int keyBits, long[] keys, Pointer[] values, int count);
    }

    private Hashmap() {
    }

    public static Cell build(int keyBits, long[] keys, Cell[] values) {
        if (keyBits < 1 || keyBits > 64) {
            throw new IllegalArgumentException("dictionary key length must be 1..64 bits");
        }
        if (keys == null || values == null || keys.length != values.length || keys.length == 0) {
            throw new IllegalArgumentException("dictionary keys and values must be a non-empty pair");
        }
        Pointer[] nativeValues = new Pointer[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null || values[i].nativeCell == null) {
                throw new IllegalArgumentException("dictionary value is null");
            }
            nativeValues[i] = values[i].nativeCell;
        }
        Pointer root = CtonLibrary.INSTANCE.hashmap_build(keyBits, keys, nativeValues, keys.length);
        if (root == null) {
            throw new IllegalStateException("failed to build Hashmap");
        }
        return new Cell(root);
    }
}
