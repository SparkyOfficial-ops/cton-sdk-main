package com.cton.sdk;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class SessionCryptoTest {
    @Test
    public void x25519MatchesRfc7748() {
        byte[] alice = hex("77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a");
        byte[] bob = hex("5dab087e624a8a4b79e17f8b83800ee66f3bb1292618b6fd1c2f8b27ff88e0eb");
        byte[] shared = hex("4a5d9d5ba4ce2de1728e3bf480350f25e07e21c947d19e3376f09b3c1e161742");
        assertArrayEquals(shared, X25519.sharedSecret(alice, X25519.publicFromSecret(bob)));
        assertArrayEquals(shared, X25519.sharedSecret(bob, X25519.publicFromSecret(alice)));
    }

    @Test
    public void poly1305MatchesRfc8439() {
        byte[] key = hex("85d6be7857556d337f4452fe42d506a80103808afb0db2fd4abff6af4149f51b");
        byte[] message = "Cryptographic Forum Research Group".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] tag = hex("a8061dc1305136c6c22b8baf0c0127a9");
        assertArrayEquals(tag, SecretBox.poly1305(key, message));
    }

    @Test
    public void sessionRoundTrip() {
        SessionCrypto alice = new SessionCrypto();
        SessionCrypto bob = new SessionCrypto();
        byte[] message = "ton-connect".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String boxed = alice.encrypt(message, bob.publicKey());
        assertArrayEquals(message, bob.decrypt(boxed, alice.publicKey()));
        assertEquals(64, alice.clientId().length());
    }

    private static byte[] hex(String value) {
        byte[] out = new byte[value.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }
}
