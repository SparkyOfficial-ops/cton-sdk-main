package com.cton.tonconnect;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import com.iwebpp.crypto.TweetNacl;

/**
 * Ключі сесії TonConnect: client id — hex публічного ключа X25519.
 * Повідомлення мосту — nonce (24 байти) і crypto_box.
 */
final class SessionCrypto {
    private final byte[] publicKey;
    private final byte[] secretKey;
    private final SecureRandom random = new SecureRandom();

    SessionCrypto() {
        TweetNacl.Box.KeyPair keyPair = TweetNacl.Box.keyPair();
        this.publicKey = keyPair.getPublicKey();
        this.secretKey = keyPair.getSecretKey();
    }

    String clientId() {
        return hex(publicKey);
    }

    String encrypt(String message, byte[] receiverPublicKey) {
        byte[] nonce = new byte[TweetNacl.Box.nonceLength];
        random.nextBytes(nonce);
        TweetNacl.Box box = new TweetNacl.Box(receiverPublicKey, secretKey);
        byte[] cipher = box.box(message.getBytes(StandardCharsets.UTF_8), nonce);
        if (cipher == null) {
            throw new IllegalStateException("TonConnect encryption failed");
        }
        byte[] payload = new byte[nonce.length + cipher.length];
        System.arraycopy(nonce, 0, payload, 0, nonce.length);
        System.arraycopy(cipher, 0, payload, nonce.length, cipher.length);
        return Base64.getEncoder().encodeToString(payload);
    }

    String decrypt(String payloadBase64, byte[] senderPublicKey) {
        byte[] payload = Base64.getDecoder().decode(payloadBase64);
        if (payload.length <= TweetNacl.Box.nonceLength) {
            throw new IllegalArgumentException("TonConnect message is too short");
        }
        byte[] nonce = new byte[TweetNacl.Box.nonceLength];
        byte[] cipher = new byte[payload.length - nonce.length];
        System.arraycopy(payload, 0, nonce, 0, nonce.length);
        System.arraycopy(payload, nonce.length, cipher, 0, cipher.length);
        TweetNacl.Box box = new TweetNacl.Box(senderPublicKey, secretKey);
        byte[] opened = box.open(cipher, nonce);
        if (opened == null) {
            throw new IllegalArgumentException("TonConnect message failed authentication");
        }
        return new String(opened, StandardCharsets.UTF_8);
    }

    static byte[] hexToBytes(String hex) {
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }

    private static String hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(Character.forDigit((value >> 4) & 0xf, 16));
            builder.append(Character.forDigit(value & 0xf, 16));
        }
        return builder.toString();
    }
}
