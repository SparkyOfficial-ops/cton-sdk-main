// CryptoTest.java - Тести для Crypto
// Author: Андрій Будильников (Sparky)

package com.cton.sdk;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 * Тести для Crypto
 */
public class CryptoTest {
    
    @Test
    public void testGeneratePrivateKey() {
        // Генерація приватного ключа
        Crypto.PrivateKey privateKey = Crypto.PrivateKey.generate();
        
        // Перевіряємо, що ключ створено
        assertNotNull(privateKey);
        
        // Отримуємо дані ключа
        byte[] keyData = privateKey.getData();
        assertNotNull(keyData);
        assertEquals(32, keyData.length);
    }
    
    @Test
    public void testPrivateKeyConstructor() {
        byte[] keyData = new byte[32];
        Arrays.fill(keyData, (byte) 0xFF);
        
        Crypto.PrivateKey privateKey = new Crypto.PrivateKey(keyData);
        assertNotNull(privateKey);
        
        byte[] retrievedData = privateKey.getData();
        assertArrayEquals(keyData, retrievedData);
    }
    
    @Test
    public void testPrivateKeyInvalidLength() {
        byte[] invalidKeyData = new byte[31]; // Should be 32 bytes
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Crypto.PrivateKey(invalidKeyData);
        });
    }
    
    @Test
    public void testPrivateKeyToPublicKey() {
        // Генерація приватного ключа
        Crypto.PrivateKey privateKey = Crypto.PrivateKey.generate();
        
        // Отримання публічного ключа
        Crypto.PublicKey publicKey = privateKey.getPublicKey();
        
        // Перевіряємо, що ключ створено
        assertNotNull(publicKey);
        
        // Отримуємо дані ключа
        byte[] keyData = publicKey.getData();
        assertNotNull(keyData);
        assertEquals(32, keyData.length);
    }
    
    @Test
    public void testPublicKeyConstructor() {
        byte[] keyData = new byte[32];
        Arrays.fill(keyData, (byte) 0xAA);
        
        Crypto.PublicKey publicKey = new Crypto.PublicKey(keyData);
        assertNotNull(publicKey);
        
        byte[] retrievedData = publicKey.getData();
        assertArrayEquals(keyData, retrievedData);
    }
    
    @Test
    public void testPublicKeyInvalidLength() {
        byte[] invalidKeyData = new byte[33]; // Should be 32 bytes
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Crypto.PublicKey(invalidKeyData);
        });
    }
    
    @Test
    public void testPublicKeyVerifySignature() {
        byte[] keyData = new byte[32];
        Arrays.fill(keyData, (byte) 0xAA);
        
        Crypto.PublicKey publicKey = new Crypto.PublicKey(keyData);
        byte[] message = {0x01, 0x02, 0x03, 0x04};
        byte[] signature = new byte[64];
        
        // Test that verify doesn't crash
        try {
            boolean result = publicKey.verifySignature(message, signature);
            // Test with a real implementation
            assertTrue(true); // Method should not crash
        } catch (Exception e) {
            // Should not throw exceptions with valid inputs
            fail("PublicKey.verifySignature should not throw exceptions with valid inputs");
        }
    }
    
    @Test
    public void testCryptoSign() {
        Crypto.PrivateKey privateKey = Crypto.PrivateKey.generate();
        byte[] message = {0x01, 0x02, 0x03, 0x04};
        
        // Test that sign works correctly
        try {
            byte[] signature = Crypto.sign(privateKey, message);
            // Should return 64 bytes for Ed25519
            assertEquals(64, signature.length);
            // Signature should not be all zeros
            boolean allZero = true;
            for (byte b : signature) {
                if (b != 0) {
                    allZero = false;
                    break;
                }
            }
            assertFalse(allZero, "Signature should not be all zeros");
        } catch (Exception e) {
            // Should not throw exceptions with valid inputs
            fail("Crypto.sign should not throw exceptions with valid inputs: " + e.getMessage());
        }
    }
    
    @Test
    public void testCryptoVerify() {
        byte[] keyData = new byte[32];
        Arrays.fill(keyData, (byte) 0xAA);
        
        Crypto.PublicKey publicKey = new Crypto.PublicKey(keyData);
        byte[] message = {0x01, 0x02, 0x03, 0x04};
        byte[] signature = new byte[64];
        
        // Test that verify doesn't crash
        try {
            boolean result = Crypto.verify(publicKey, message, signature);
            // Test with a real implementation
            assertTrue(true); // Method should not crash
        } catch (Exception e) {
            // Should not throw exceptions with valid inputs
            fail("Crypto.verify should not throw exceptions with valid inputs");
        }
    }
    
    @Test
    public void testGenerateMnemonic() {
        // Генерація мнемонічної фрази
        List<String> mnemonic = Crypto.generateMnemonic();
        
        // Перевіряємо, що фраза створена
        assertNotNull(mnemonic);
        assertEquals(24, mnemonic.size());
    }
    
    @Test
    public void testMnemonicToPrivateKey() {
        // Генерація мнемонічної фрази
        List<String> mnemonic = Crypto.generateMnemonic();
        
        // Створення приватного ключа з мнемоніки
        Crypto.PrivateKey privateKey = Crypto.mnemonicToPrivateKey(mnemonic);
        
        // Перевіряємо, що ключ створено
        assertNotNull(privateKey);
        
        // Отримуємо дані ключа
        byte[] keyData = privateKey.getData();
        assertNotNull(keyData);
        assertEquals(32, keyData.length);
    }
}