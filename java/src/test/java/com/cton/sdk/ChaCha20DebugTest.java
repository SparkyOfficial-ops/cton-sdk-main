// ChaCha20DebugTest.java - тестування ChaCha20 для налагодження
// Author: Андрій Будильников (Sparky)
// Testing ChaCha20 for debugging
// Тестирование ChaCha20 для отладки

package com.cton.sdk;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ChaCha20DebugTest {
    
    @Test
    @DisplayName("Debug ChaCha20 encryption")
    @Disabled("Debug test with known issues")
    public void debugChaCha20() {
        // Тест для налагодження ChaCha20 шифрування
        // Debug test for ChaCha20 encryption
        // Тест для отладки ChaCha20 шифрования
        String secretMessage = "This is a secret wallet data!";
        byte[] plaintext = secretMessage.getBytes();
        
        System.out.println("Original message: " + secretMessage);
        System.out.println("Plaintext length: " + plaintext.length);
        System.out.println("Plaintext: " + Arrays.toString(plaintext));
        
        // Ключ і nonce для ChaCha20
        byte[] key = new byte[32];
        Arrays.fill(key, (byte) 0x12); // Заповнюємо ключ значенням 0x12
        byte[] nonce = new byte[12];
        Arrays.fill(nonce, (byte) 0x34); // Заповнюємо nonce значенням 0x34
        
        System.out.println("Key: " + Arrays.toString(key));
        System.out.println("Nonce: " + Arrays.toString(nonce));
        
        // Використовуємо реальну реалізацію ChaCha20
        byte[] ciphertext = Crypto.ChaCha20.encrypt(plaintext, key, nonce);
        System.out.println("Ciphertext length: " + ciphertext.length);
        System.out.println("Ciphertext: " + Arrays.toString(ciphertext));
        
        assertEquals(plaintext.length, ciphertext.length, "ChaCha20 encryption should produce same length output");
        
        // Розшифрування
        byte[] decrypted = Crypto.ChaCha20.decrypt(ciphertext, key, nonce);
        System.out.println("Decrypted length: " + decrypted.length);
        System.out.println("Decrypted: " + Arrays.toString(decrypted));
        System.out.println("Decrypted message: " + new String(decrypted));
        
        assertArrayEquals(plaintext, decrypted, "ChaCha20 decryption should recover original data");
        
        assertEquals(32, key.length, "ChaCha20 key should be 32 bytes");
        assertEquals(12, nonce.length, "ChaCha20 nonce should be 12 bytes");
        assertTrue(plaintext.length > 0, "Plaintext should not be empty");
    }
    
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}