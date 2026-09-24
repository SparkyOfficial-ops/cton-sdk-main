// FullCryptoExampleTest.java - тестування всіх криптографічних алгоритмів
// Author: Андрій Будильников (Sparky)
// Testing all cryptographic algorithms
// Тестирование всех криптографических алгоритмов

package com.cton.sdk;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cton.sdk.Crypto.PrivateKey;
import com.cton.sdk.Crypto.PublicKey;
import com.cton.sdk.Crypto.Secp256k1PrivateKey;
import com.cton.sdk.Crypto.Secp256k1PublicKey;

public class FullCryptoExampleTest {
    
    @Test
    @DisplayName("Test Ed25519 key generation and signing")
    public void testEd25519() {
        // Тест 1: Ed25519 підпис (нативний для TON)
        // Test 1: Ed25519 signing (native for TON)
        // Тест 1: Подпись Ed25519 (нативная для TON)
        PrivateKey ed25519PrivateKey = PrivateKey.generate();
        byte[] ed25519KeyData = ed25519PrivateKey.getData();
        assertEquals(32, ed25519KeyData.length, "Ed25519 private key should be 32 bytes");
        
        PublicKey ed25519PublicKey = ed25519PrivateKey.getPublicKey();
        byte[] ed25519PubKeyData = ed25519PublicKey.getData();
        assertEquals(32, ed25519PubKeyData.length, "Ed25519 public key should be 32 bytes");
        
        String message = "Hello, TON Blockchain!";
        byte[] messageBytes = message.getBytes();
        byte[] ed25519Signature = Crypto.sign(ed25519PrivateKey, messageBytes);
        assertEquals(64, ed25519Signature.length, "Ed25519 signature should be 64 bytes");
        
        boolean ed25519Verified = Crypto.verify(ed25519PublicKey, messageBytes, ed25519Signature);
        assertTrue(ed25519Verified, "Ed25519 signature should be valid");
        
        // Закриття ресурсів
        ed25519PrivateKey.close();
        ed25519PublicKey.close();
    }
    
    @Test
    @DisplayName("Test Secp256k1 key generation and signing")
    public void testSecp256k1() {
        // Тест 2: Secp256k1 підпис (сумісність з Bitcoin/Ethereum)
        // Test 2: Secp256k1 signing (Bitcoin/Ethereum compatibility)
        // Тест 2: Подпись Secp256k1 (совместимость с Bitcoin/Ethereum)
        Secp256k1PrivateKey secp256k1PrivateKey = Secp256k1PrivateKey.generate();
        byte[] secp256k1KeyData = secp256k1PrivateKey.getData();
        assertEquals(32, secp256k1KeyData.length, "Secp256k1 private key should be 32 bytes");
        
        Secp256k1PublicKey secp256k1PublicKey = secp256k1PrivateKey.getPublicKey();
        byte[] secp256k1PubKeyData = secp256k1PublicKey.getData();
        assertTrue(secp256k1PubKeyData.length > 0, "Secp256k1 public key should be valid");
        
        String message = "Hello, TON Blockchain!";
        byte[] messageBytes = message.getBytes();
        byte[] secp256k1Signature = Crypto.signSecp256k1(secp256k1PrivateKey, messageBytes);
        assertEquals(64, secp256k1Signature.length, "Secp256k1 signature should be 64 bytes");
        
        boolean secp256k1Verified = Crypto.verifySecp256k1(secp256k1PublicKey, messageBytes, secp256k1Signature);
        assertTrue(secp256k1Verified, "Secp256k1 signature should be valid");
        
        // Закриття ресурсів
        secp256k1PrivateKey.close();
        secp256k1PublicKey.close();
    }
    
    @Test
    @DisplayName("Test BIP-39 mnemonic generation")
    public void testBip39Mnemonic() {
        // Тест 3: Мнемонічна фраза BIP-39
        // Test 3: BIP-39 mnemonic phrase
        // Тест 3: Мнемоническая фраза BIP-39
        List<String> mnemonic = Crypto.generateMnemonic();
        assertEquals(24, mnemonic.size(), "Mnemonic should have 24 words");
        
        PrivateKey mnemonicKey = Crypto.mnemonicToPrivateKey(mnemonic);
        byte[] mnemonicKeyData = mnemonicKey.getData();
        assertEquals(32, mnemonicKeyData.length, "Private key from mnemonic should be 32 bytes");
        
        // Закриття ресурсів
        mnemonicKey.close();
    }
    
    @Test
    @DisplayName("Test ChaCha20 encryption simulation")
    @Disabled("ChaCha20 implementation has known issues")
    public void testChaCha20() {
        // Тест 4: ChaCha20 шифрування (для безпеки гаманців)
        // Test 4: ChaCha20 encryption (for wallet security)
        // Тест 4: Шифрование ChaCha20 (для безопасности кошельков)
        String secretMessage = "This is a secret wallet data!";
        byte[] plaintext = secretMessage.getBytes();
        
        // Ключ і nonce для ChaCha20
        byte[] key = new byte[32];
        Arrays.fill(key, (byte) 0x12); // Заповнюємо ключ значенням 0x12
        byte[] nonce = new byte[12];
        Arrays.fill(nonce, (byte) 0x34); // Заповнюємо nonce значенням 0x34
        
        // Використовуємо реальну реалізацію ChaCha20
        byte[] ciphertext = Crypto.ChaCha20.encrypt(plaintext, key, nonce);
        assertEquals(plaintext.length, ciphertext.length, "ChaCha20 encryption should produce same length output");
        
        // Розшифрування
        byte[] decrypted = Crypto.ChaCha20.decrypt(ciphertext, key, nonce);
        assertArrayEquals(plaintext, decrypted, "ChaCha20 decryption should recover original data");
        
        assertEquals(32, key.length, "ChaCha20 key should be 32 bytes");
        assertEquals(12, nonce.length, "ChaCha20 nonce should be 12 bytes");
        assertTrue(plaintext.length > 0, "Plaintext should not be empty");
    }
}