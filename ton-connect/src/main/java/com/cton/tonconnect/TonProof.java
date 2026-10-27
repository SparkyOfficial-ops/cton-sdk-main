package com.cton.tonconnect;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.cton.sdk.Address;
import com.cton.sdk.Crypto;

/**
 * ton_proof за специфікацією TonConnect 2:
 * Ed25519(sha256(0xffff || "ton-connect" || sha256(message))).
 */
final class TonProof {
    private TonProof() {
    }

    static byte[] sign(Crypto.PrivateKey privateKey, Address address, String domain,
                        long timestamp, String payload) {
        byte[] message = message(address, domain, timestamp, payload);
        byte[] messageHash = sha256(message);
        byte[] prefix = "ton-connect".getBytes(StandardCharsets.UTF_8);
        byte[] wrapped = new byte[2 + prefix.length + messageHash.length];
        wrapped[0] = (byte) 0xff;
        wrapped[1] = (byte) 0xff;
        System.arraycopy(prefix, 0, wrapped, 2, prefix.length);
        System.arraycopy(messageHash, 0, wrapped, 2 + prefix.length, messageHash.length);
        return Crypto.sign(privateKey, sha256(wrapped));
    }

    private static byte[] message(Address address, String domain, long timestamp, String payload) {
        byte[] prefix = "ton-proof-item-v2/".getBytes(StandardCharsets.UTF_8);
        byte[] domainBytes = domain.getBytes(StandardCharsets.UTF_8);
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        byte[] hash = address.getHashPart();
        ByteBuffer buffer = ByteBuffer.allocate(prefix.length + 4 + hash.length + 4 + domainBytes.length + 8 + payloadBytes.length);
        buffer.put(prefix);
        buffer.order(ByteOrder.BIG_ENDIAN).putInt(address.getWorkchain());
        buffer.put(hash);
        buffer.order(ByteOrder.LITTLE_ENDIAN).putInt(domainBytes.length);
        buffer.put(domainBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN).putLong(timestamp);
        buffer.put(payloadBytes);
        return buffer.array();
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
