package com.cton.sdk;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * X25519 as specified in RFC 7748. Field arithmetic uses {@link BigInteger}.
 */
final class X25519 {
    private static final BigInteger P = BigInteger.TWO.pow(255).subtract(BigInteger.valueOf(19));
    private static final BigInteger A24 = BigInteger.valueOf(121665);
    private static final SecureRandom RANDOM = new SecureRandom();

    private X25519() {
    }

    static byte[] generateSecret() {
        byte[] secret = new byte[32];
        RANDOM.nextBytes(secret);
        clamp(secret);
        return secret;
    }

    static byte[] publicFromSecret(byte[] secret) {
        byte[] base = new byte[32];
        base[0] = 9;
        return scalarmult(secret, base);
    }

    static byte[] sharedSecret(byte[] secret, byte[] peerPublic) {
        return scalarmult(secret, peerPublic);
    }

    static byte[] scalarmult(byte[] scalar, byte[] uCoordinate) {
        if (scalar.length != 32 || uCoordinate.length != 32) {
            throw new IllegalArgumentException("X25519 keys are 32 bytes");
        }
        byte[] k = scalar.clone();
        clamp(k);
        BigInteger x1 = decodeLittle(uCoordinate).and(BigInteger.TWO.pow(255).subtract(BigInteger.ONE));
        BigInteger x2 = BigInteger.ONE;
        BigInteger z2 = BigInteger.ZERO;
        BigInteger x3 = x1;
        BigInteger z3 = BigInteger.ONE;
        int swap = 0;
        for (int t = 254; t >= 0; t--) {
            int kt = (k[t >>> 3] >> (t & 7)) & 1;
            swap ^= kt;
            BigInteger[] x = cswap(swap, x2, x3);
            BigInteger[] z = cswap(swap, z2, z3);
            x2 = x[0];
            x3 = x[1];
            z2 = z[0];
            z3 = z[1];
            swap = kt;

            BigInteger a = mod(x2.add(z2));
            BigInteger aa = mod(a.multiply(a));
            BigInteger b = mod(x2.subtract(z2));
            BigInteger bb = mod(b.multiply(b));
            BigInteger e = mod(aa.subtract(bb));
            BigInteger c = mod(x3.add(z3));
            BigInteger d = mod(x3.subtract(z3));
            BigInteger da = mod(d.multiply(a));
            BigInteger cb = mod(c.multiply(b));
            x3 = mod(da.add(cb).pow(2));
            z3 = mod(x1.multiply(mod(da.subtract(cb)).pow(2)));
            x2 = mod(aa.multiply(bb));
            z2 = mod(e.multiply(mod(aa.add(mod(A24.multiply(e))))));
        }
        BigInteger[] x = cswap(swap, x2, x3);
        BigInteger[] z = cswap(swap, z2, z3);
        x2 = x[0];
        z2 = z[0];
        BigInteger result = mod(x2.multiply(z2.modPow(P.subtract(BigInteger.TWO), P)));
        return encodeLittle(result);
    }

    private static void clamp(byte[] secret) {
        secret[0] = (byte) (secret[0] & 248);
        secret[31] = (byte) (secret[31] & 127);
        secret[31] = (byte) (secret[31] | 64);
    }

    private static BigInteger[] cswap(int swap, BigInteger a, BigInteger b) {
        if (swap == 1) {
            return new BigInteger[] {b, a};
        }
        return new BigInteger[] {a, b};
    }

    private static BigInteger mod(BigInteger value) {
        return value.mod(P);
    }

    private static BigInteger decodeLittle(byte[] data) {
        byte[] big = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            big[data.length - 1 - i] = data[i];
        }
        return new BigInteger(1, big);
    }

    private static byte[] encodeLittle(BigInteger value) {
        byte[] big = value.toByteArray();
        byte[] out = new byte[32];
        for (int i = 0; i < 32 && i < big.length; i++) {
            out[i] = big[big.length - 1 - i];
        }
        return out;
    }
}
