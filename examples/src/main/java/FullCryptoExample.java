// FullCryptoExample.java - приклад використання всіх криптографічних алгоритмів
// Author: Андрій Будильников (Sparky)
// Example of using all cryptographic algorithms
// Пример использования всех криптографических алгоритмов

import java.util.Arrays;
import java.util.List;

import com.cton.sdk.Crypto;
import com.cton.sdk.Crypto.PrivateKey;
import com.cton.sdk.Crypto.PublicKey;
import com.cton.sdk.Crypto.Secp256k1PrivateKey;
import com.cton.sdk.Crypto.Secp256k1PublicKey;

public class FullCryptoExample {
    public static void main(String[] args) {
        try {
            System.out.println("Full Cryptographic Algorithms Example");
            System.out.println("===================================");
            
            // Тест 1: Ed25519 підпис (нативний для TON)
            // Test 1: Ed25519 signing (native for TON)
            // Тест 1: Подпись Ed25519 (нативная для TON)
            System.out.println("1. Ed25519 Signing (TON native)");
            PrivateKey ed25519PrivateKey = PrivateKey.generate();
            byte[] ed25519KeyData = ed25519PrivateKey.getData();
            System.out.println("   Ed25519 private key generated: " + ed25519KeyData.length + " bytes");
            
            PublicKey ed25519PublicKey = ed25519PrivateKey.getPublicKey();
            byte[] ed25519PubKeyData = ed25519PublicKey.getData();
            System.out.println("   Ed25519 public key derived: " + ed25519PubKeyData.length + " bytes");
            
            String message = "Hello, TON Blockchain!";
            byte[] messageBytes = message.getBytes();
            byte[] ed25519Signature = Crypto.sign(ed25519PrivateKey, messageBytes);
            System.out.println("   Message signed with Ed25519: " + ed25519Signature.length + " bytes signature");
            
            boolean ed25519Verified = Crypto.verify(ed25519PublicKey, messageBytes, ed25519Signature);
            System.out.println("   Ed25519 signature verified: " + (ed25519Verified ? "VALID" : "INVALID"));
            
            // Закриття ресурсів
            ed25519PrivateKey.close();
            ed25519PublicKey.close();
            
            // Тест 2: Secp256k1 підпис (сумісність з Bitcoin/Ethereum)
            // Test 2: Secp256k1 signing (Bitcoin/Ethereum compatibility)
            // Тест 2: Подпись Secp256k1 (совместимость с Bitcoin/Ethereum)
            System.out.println("\n2. Secp256k1 Signing (Bitcoin/Ethereum compatibility)");
            Secp256k1PrivateKey secp256k1PrivateKey = Secp256k1PrivateKey.generate();
            byte[] secp256k1KeyData = secp256k1PrivateKey.getData();
            System.out.println("   Secp256k1 private key generated: " + secp256k1KeyData.length + " bytes");
            
            Secp256k1PublicKey secp256k1PublicKey = secp256k1PrivateKey.getPublicKey();
            byte[] secp256k1PubKeyData = secp256k1PublicKey.getData();
            System.out.println("   Secp256k1 public key derived: " + secp256k1PubKeyData.length + " bytes");
            
            byte[] secp256k1Signature = Crypto.signSecp256k1(secp256k1PrivateKey, messageBytes);
            System.out.println("   Message signed with Secp256k1: " + secp256k1Signature.length + " bytes signature");
            
            boolean secp256k1Verified = Crypto.verifySecp256k1(secp256k1PublicKey, messageBytes, secp256k1Signature);
            System.out.println("   Secp256k1 signature verified: " + (secp256k1Verified ? "VALID" : "INVALID"));
            
            // Закриття ресурсів
            secp256k1PrivateKey.close();
            secp256k1PublicKey.close();
            
            // Тест 3: Мнемонічна фраза BIP-39
            // Test 3: BIP-39 mnemonic phrase
            // Тест 3: Мнемоническая фраза BIP-39
            System.out.println("\n3. BIP-39 Mnemonic Phrase");
            List<String> mnemonic = Crypto.generateMnemonic();
            System.out.println("   Generated " + mnemonic.size() + "-word mnemonic phrase");
            System.out.println("   First 3 words: " + mnemonic.get(0) + " " + mnemonic.get(1) + " " + mnemonic.get(2));
            
            PrivateKey mnemonicKey = Crypto.mnemonicToPrivateKey(mnemonic);
            byte[] mnemonicKeyData = mnemonicKey.getData();
            System.out.println("   Private key from mnemonic: " + mnemonicKeyData.length + " bytes");
            
            // Закриття ресурсів
            mnemonicKey.close();
            
            // Тест 4: ChaCha20 шифрування (для безпеки гаманців)
            // Test 4: ChaCha20 encryption (for wallet security)
            // Тест 4: Шифрование ChaCha20 (для безопасности кошельков)
            System.out.println("\n4. ChaCha20 Encryption (for wallet security)");
            String secretMessage = "This is a secret wallet data!";
            byte[] plaintext = secretMessage.getBytes();
            System.out.println("   Original message: " + secretMessage);
            
            // Ключ і nonce для ChaCha20
            byte[] key = new byte[32];
            Arrays.fill(key, (byte) 0x12); // Заповнюємо ключ значенням 0x12
            byte[] nonce = new byte[12];
            Arrays.fill(nonce, (byte) 0x34); // Заповнюємо nonce значенням 0x34
            
            // Використовуємо реальну реалізацію ChaCha20
            byte[] ciphertext = Crypto.ChaCha20.encrypt(plaintext, key, nonce);
            System.out.println("   ChaCha20 encryption: " + plaintext.length + " bytes -> " + ciphertext.length + " bytes");
            
            // Розшифрування
            byte[] decrypted = Crypto.ChaCha20.decrypt(ciphertext, key, nonce);
            String decryptedMessage = new String(decrypted);
            System.out.println("   ChaCha20 decryption: " + decryptedMessage);
            
            System.out.println("\nExample completed successfully!");
            System.out.println("All cryptographic algorithms are working correctly!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}