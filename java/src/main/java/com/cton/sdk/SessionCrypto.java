package com.cton.sdk;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * TON Connect 2 session keys. The client id is the hex encoding of the
 * X25519 public key. Payloads are NaCl boxes: 24-byte nonce, then the
 * authenticator and ciphertext, encoded as standard Base64.
 */
public final class SessionCrypto {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private final byte[] secretKey;
    private final byte[] publicKey;

    public SessionCrypto() {
        this.secretKey = X25519.generateSecret();
        this.publicKey = X25519.publicFromSecret(secretKey);
    }

    public SessionCrypto(byte[] secretKey) {
        if (secretKey == null || secretKey.length != 32) {
            throw new IllegalArgumentException("session secret must be 32 bytes");
        }
        this.secretKey = secretKey.clone();
        this.publicKey = X25519.publicFromSecret(this.secretKey);
    }

    public byte[] publicKey() {
        return publicKey.clone();
    }

    public String clientId() {
        return hex(publicKey);
    }

    public String encrypt(byte[] message, byte[] receiverPublicKey) {
        byte[] nonce = new byte[24];
        RANDOM.nextBytes(nonce);
        byte[] key = SecretBox.boxKey(secretKey, receiverPublicKey);
        byte[] boxed = SecretBox.seal(key, nonce, message);
        byte[] packed = new byte[nonce.length + boxed.length];
        System.arraycopy(nonce, 0, packed, 0, nonce.length);
        System.arraycopy(boxed, 0, packed, nonce.length, boxed.length);
        return Base64.getEncoder().encodeToString(packed);
    }

    public byte[] decrypt(String payload, byte[] senderPublicKey) {
        byte[] packed = Base64.getDecoder().decode(payload);
        if (packed.length < 24) {
            throw new IllegalArgumentException("session payload is too short");
        }
        byte[] nonce = Arrays.copyOf(packed, 24);
        byte[] boxed = Arrays.copyOfRange(packed, 24, packed.length);
        byte[] key = SecretBox.boxKey(secretKey, senderPublicKey);
        return SecretBox.open(key, nonce, boxed);
    }

    private static String hex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            out[i * 2] = HEX[value >>> 4];
            out[i * 2 + 1] = HEX[value & 0x0F];
        }
        return new String(out);
    }
}
