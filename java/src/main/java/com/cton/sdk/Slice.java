package com.cton.sdk;

import java.math.BigInteger;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;

/**
 * Cursor over a cell. Bits and references are read in the order a TVM slice
 * reads them: {@code loadUint}, {@code loadInt}, {@code loadCoins},
 * {@code loadAddress}, {@code loadRef}.
 */
public final class Slice implements AutoCloseable {
    public interface CtonLibrary extends Library {
        CtonLibrary INSTANCE = Native.load("cton-sdk-core", CtonLibrary.class);

        Pointer slice_create(Pointer cell);
        void slice_destroy(Pointer slice);
        boolean slice_load_uint(Pointer slice, int bits, LongByReference out);
        boolean slice_load_int(Pointer slice, int bits, LongByReference out);
        int slice_load_coins(Pointer slice, byte[] buffer, int bufferSize);
        int slice_load_address(Pointer slice, byte[] workchain, byte[] hash);
        Pointer slice_load_ref(Pointer slice);
        int slice_remaining_bits(Pointer slice);
        int slice_remaining_refs(Pointer slice);

        Pointer address_create();
        void address_set_workchain(Pointer address, byte workchain);
        void address_set_hash_part(Pointer address, byte[] hashPart);
    }

    private Pointer nativeSlice;
    private boolean closed;

    public Slice(Cell cell) {
        if (cell == null || cell.nativeCell == null) {
            throw new IllegalArgumentException("cell is null");
        }
        this.nativeSlice = CtonLibrary.INSTANCE.slice_create(cell.nativeCell);
        if (this.nativeSlice == null) {
            throw new IllegalStateException("failed to open slice");
        }
    }

    public long loadUint(int bits) {
        LongByReference out = new LongByReference();
        if (!CtonLibrary.INSTANCE.slice_load_uint(nativeSlice, bits, out)) {
            throw new IllegalStateException("failed to load uint" + bits);
        }
        return out.getValue();
    }

    public long loadUInt(int bits) {
        return loadUint(bits);
    }

    public byte[] loadBytes(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must be non-negative");
        }
        byte[] out = new byte[length];
        for (int i = 0; i < length; i++) {
            out[i] = (byte) loadUint(8);
        }
        return out;
    }

    public long loadInt(int bits) {
        LongByReference out = new LongByReference();
        if (!CtonLibrary.INSTANCE.slice_load_int(nativeSlice, bits, out)) {
            throw new IllegalStateException("failed to load int" + bits);
        }
        return out.getValue();
    }

    public BigInteger loadCoins() {
        byte[] buffer = new byte[15];
        int length = CtonLibrary.INSTANCE.slice_load_coins(nativeSlice, buffer, buffer.length);
        if (length < 0) {
            throw new IllegalStateException("failed to load coins");
        }
        if (length == 0) {
            return BigInteger.ZERO;
        }
        byte[] magnitude = new byte[length];
        System.arraycopy(buffer, 0, magnitude, 0, length);
        return new BigInteger(1, magnitude);
    }

    /**
     * @return the standard address, or {@code null} for {@code addr_none}
     */
    public Address loadAddress() {
        byte[] workchain = new byte[1];
        byte[] hash = new byte[32];
        int kind = CtonLibrary.INSTANCE.slice_load_address(nativeSlice, workchain, hash);
        if (kind < 0) {
            throw new IllegalStateException("failed to load address");
        }
        if (kind == 0) {
            return null;
        }
        Pointer nativeAddress = CtonLibrary.INSTANCE.address_create();
        if (nativeAddress == null) {
            throw new IllegalStateException("failed to allocate address");
        }
        CtonLibrary.INSTANCE.address_set_workchain(nativeAddress, workchain[0]);
        CtonLibrary.INSTANCE.address_set_hash_part(nativeAddress, hash);
        return new Address(nativeAddress);
    }

    public Cell loadRef() {
        Pointer ref = CtonLibrary.INSTANCE.slice_load_ref(nativeSlice);
        if (ref == null) {
            throw new IllegalStateException("failed to load reference");
        }
        return new Cell(ref);
    }

    public int remainingBits() {
        return CtonLibrary.INSTANCE.slice_remaining_bits(nativeSlice);
    }

    public int remainingRefs() {
        return CtonLibrary.INSTANCE.slice_remaining_refs(nativeSlice);
    }

    @Override
    public void close() {
        if (!closed && nativeSlice != null) {
            CtonLibrary.INSTANCE.slice_destroy(nativeSlice);
            nativeSlice = null;
            closed = true;
        }
    }
}
