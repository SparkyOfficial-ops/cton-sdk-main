package com.cton.sdk;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * NaCl {@code crypto_box}: X25519, then XSalsa20-Poly1305.
 * The packed form used by TON Connect is {@code nonce || mac || ciphertext}.
 */
final class SecretBox {
    private static final int[] SIGMA = {
            0x61707865, 0x3320646e, 0x79622d32, 0x6b206574
    };
    private static final BigInteger P130 = BigInteger.TWO.pow(130).subtract(BigInteger.valueOf(5));
    private static final BigInteger MASK128 = BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE);

    private SecretBox() {
    }

    static byte[] boxKey(byte[] senderSecret, byte[] receiverPublic) {
        byte[] shared = X25519.sharedSecret(senderSecret, receiverPublic);
        return hsalsa20(shared, new byte[16]);
    }

    static byte[] seal(byte[] key, byte[] nonce, byte[] message) {
        byte[] padded = new byte[32 + message.length];
        System.arraycopy(message, 0, padded, 32, message.length);
        byte[] streamed = new byte[padded.length];
        xsalsa20Xor(streamed, padded, nonce, key);
        byte[] polyKey = Arrays.copyOf(streamed, 32);
        byte[] cipher = Arrays.copyOfRange(streamed, 32, streamed.length);
        byte[] mac = poly1305(polyKey, cipher);
        byte[] out = new byte[16 + cipher.length];
        System.arraycopy(mac, 0, out, 0, 16);
        System.arraycopy(cipher, 0, out, 16, cipher.length);
        return out;
    }

    static byte[] open(byte[] key, byte[] nonce, byte[] boxed) {
        if (boxed.length < 16) {
            throw new IllegalArgumentException("secretbox is shorter than the authenticator");
        }
        byte[] mac = Arrays.copyOf(boxed, 16);
        byte[] cipher = Arrays.copyOfRange(boxed, 16, boxed.length);
        byte[] prefix = new byte[32];
        xsalsa20Xor(prefix, new byte[32], nonce, key);
        byte[] expect = poly1305(prefix, cipher);
        if (!Arrays.equals(mac, expect)) {
            throw new IllegalArgumentException("secretbox authentication failed");
        }
        byte[] padded = new byte[32 + cipher.length];
        System.arraycopy(cipher, 0, padded, 32, cipher.length);
        byte[] streamed = new byte[padded.length];
        xsalsa20Xor(streamed, padded, nonce, key);
        return Arrays.copyOfRange(streamed, 32, streamed.length);
    }

    static byte[] poly1305(byte[] key, byte[] message) {
        byte[] rBytes = Arrays.copyOf(key, 16);
        rBytes[3] &= 15;
        rBytes[7] &= 15;
        rBytes[11] &= 15;
        rBytes[15] &= 15;
        rBytes[4] &= 252;
        rBytes[8] &= 252;
        rBytes[12] &= 252;
        BigInteger r = little(rBytes, 16);
        BigInteger s = little(key, 16, 16);
        BigInteger acc = BigInteger.ZERO;
        for (int offset = 0; offset < message.length; offset += 16) {
            int n = Math.min(16, message.length - offset);
            byte[] block = new byte[n + 1];
            System.arraycopy(message, offset, block, 0, n);
            block[n] = 1;
            acc = acc.add(little(block, block.length)).multiply(r).mod(P130);
        }
        return encodeLittle(acc.add(s).and(MASK128), 16);
    }

    private static byte[] hsalsa20(byte[] key, byte[] nonce16) {
        int[] state = new int[16];
        state[0] = SIGMA[0];
        state[1] = load32(key, 0);
        state[2] = load32(key, 4);
        state[3] = load32(key, 8);
        state[4] = load32(key, 12);
        state[5] = SIGMA[1];
        state[6] = load32(nonce16, 0);
        state[7] = load32(nonce16, 4);
        state[8] = load32(nonce16, 8);
        state[9] = load32(nonce16, 12);
        state[10] = SIGMA[2];
        state[11] = load32(key, 16);
        state[12] = load32(key, 20);
        state[13] = load32(key, 24);
        state[14] = load32(key, 28);
        state[15] = SIGMA[3];
        rounds(state, false);
        byte[] out = new byte[32];
        int[] words = {0, 5, 10, 15, 6, 7, 8, 9};
        for (int i = 0; i < words.length; i++) {
            store32(out, i * 4, state[words[i]]);
        }
        return out;
    }

    private static void xsalsa20Xor(byte[] out, byte[] in, byte[] nonce24, byte[] key) {
        byte[] subkey = hsalsa20(key, Arrays.copyOf(nonce24, 16));
        salsa20Xor(out, in, Arrays.copyOfRange(nonce24, 16, 24), subkey);
    }

    private static void salsa20Xor(byte[] out, byte[] in, byte[] nonce8, byte[] key) {
        int[] state = new int[16];
        state[0] = SIGMA[0];
        state[1] = load32(key, 0);
        state[2] = load32(key, 4);
        state[3] = load32(key, 8);
        state[4] = load32(key, 12);
        state[5] = SIGMA[1];
        state[6] = load32(nonce8, 0);
        state[7] = load32(nonce8, 4);
        state[8] = 0;
        state[9] = 0;
        state[10] = SIGMA[2];
        state[11] = load32(key, 16);
        state[12] = load32(key, 20);
        state[13] = load32(key, 24);
        state[14] = load32(key, 28);
        state[15] = SIGMA[3];
        int offset = 0;
        while (offset < in.length) {
            int[] block = state.clone();
            rounds(block, true);
            int n = Math.min(64, in.length - offset);
            for (int i = 0; i < n; i++) {
                int word = block[i >>> 2];
                int stream = (word >>> ((i & 3) * 8)) & 0xFF;
                out[offset + i] = (byte) ((in[offset + i] & 0xFF) ^ stream);
            }
            state[8]++;
            if (state[8] == 0) {
                state[9]++;
            }
            offset += n;
        }
    }

    private static void rounds(int[] state, boolean addInput) {
        int[] x = state.clone();
        for (int i = 0; i < 10; i++) {
            x[4] ^= rotl(x[0] + x[12], 7);
            x[8] ^= rotl(x[4] + x[0], 9);
            x[12] ^= rotl(x[8] + x[4], 13);
            x[0] ^= rotl(x[12] + x[8], 18);
            x[9] ^= rotl(x[5] + x[1], 7);
            x[13] ^= rotl(x[9] + x[5], 9);
            x[1] ^= rotl(x[13] + x[9], 13);
            x[5] ^= rotl(x[1] + x[13], 18);
            x[14] ^= rotl(x[10] + x[6], 7);
            x[2] ^= rotl(x[14] + x[10], 9);
            x[6] ^= rotl(x[2] + x[14], 13);
            x[10] ^= rotl(x[6] + x[2], 18);
            x[3] ^= rotl(x[15] + x[11], 7);
            x[7] ^= rotl(x[3] + x[15], 9);
            x[11] ^= rotl(x[7] + x[3], 13);
            x[15] ^= rotl(x[11] + x[7], 18);
            x[1] ^= rotl(x[0] + x[3], 7);
            x[2] ^= rotl(x[1] + x[0], 9);
            x[3] ^= rotl(x[2] + x[1], 13);
            x[0] ^= rotl(x[3] + x[2], 18);
            x[6] ^= rotl(x[5] + x[4], 7);
            x[7] ^= rotl(x[6] + x[5], 9);
            x[4] ^= rotl(x[7] + x[6], 13);
            x[5] ^= rotl(x[4] + x[7], 18);
            x[11] ^= rotl(x[10] + x[9], 7);
            x[8] ^= rotl(x[11] + x[10], 9);
            x[9] ^= rotl(x[8] + x[11], 13);
            x[10] ^= rotl(x[9] + x[8], 18);
            x[12] ^= rotl(x[15] + x[14], 7);
            x[13] ^= rotl(x[12] + x[15], 9);
            x[14] ^= rotl(x[13] + x[12], 13);
            x[15] ^= rotl(x[14] + x[13], 18);
        }
        if (addInput) {
            for (int i = 0; i < 16; i++) {
                x[i] += state[i];
            }
        }
        System.arraycopy(x, 0, state, 0, 16);
    }

    private static int rotl(int value, int bits) {
        return (value << bits) | (value >>> (32 - bits));
    }

    private static int load32(byte[] data, int offset) {
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    private static void store32(byte[] data, int offset, int value) {
        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >>> 8);
        data[offset + 2] = (byte) (value >>> 16);
        data[offset + 3] = (byte) (value >>> 24);
    }

    private static BigInteger little(byte[] data, int length) {
        return little(data, 0, length);
    }

    private static BigInteger little(byte[] data, int offset, int length) {
        byte[] big = new byte[length];
        for (int i = 0; i < length; i++) {
            big[length - 1 - i] = data[offset + i];
        }
        return new BigInteger(1, big);
    }

    private static byte[] encodeLittle(BigInteger value, int length) {
        byte[] big = value.toByteArray();
        byte[] out = new byte[length];
        for (int i = 0; i < length && i < big.length; i++) {
            out[i] = big[big.length - 1 - i];
        }
        return out;
    }
}
