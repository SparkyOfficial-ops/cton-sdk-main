// MnemonicExample.java - приклад використання BIP-39 мнемонічних фраз
// Author: Андрій Будильников (Sparky)
// Example of using BIP-39 mnemonic phrases
// Пример использования мнемонических фраз BIP-39

import java.util.List;

import com.cton.sdk.Crypto;
import com.cton.sdk.Crypto.PrivateKey;
import com.cton.sdk.Crypto.PublicKey;

public class MnemonicExample {
    public static void main(String[] args) {
        try {
            System.out.println("BIP-39 Mnemonic Example");
            System.out.println("======================");
            
            // Генерація мнемонічної фрази
            // Generate mnemonic phrase
            // Генерация мнемонической фразы
            System.out.println("1. Generating 24-word mnemonic phrase...");
            List<String> mnemonic = Crypto.generateMnemonic();
            System.out.println("   Generated mnemonic (" + mnemonic.size() + " words):");
            for (int i = 0; i < mnemonic.size(); i++) {
                System.out.print(mnemonic.get(i) + " ");
                if ((i + 1) % 6 == 0) System.out.println(); // Перенос рядка кожні 6 слів
            }
            System.out.println();
            
            // Створення приватного ключа з мнемоніки
            // Create private key from mnemonic
            // Создание приватного ключа из мнемоники
            System.out.println("2. Creating private key from mnemonic...");
            PrivateKey privateKey = Crypto.mnemonicToPrivateKey(mnemonic);
            byte[] keyData = privateKey.getData();
            System.out.println("   Private key created: " + keyData.length + " bytes");
            
            // Отримання публічного ключа
            // Get public key
            // Получение публичного ключа
            System.out.println("3. Deriving public key...");
            PublicKey publicKey = privateKey.getPublicKey();
            byte[] pubKeyData = publicKey.getData();
            System.out.println("   Public key derived: " + pubKeyData.length + " bytes");
            
            // Підпис повідомлення
            // Sign message
            // Подпись сообщения
            System.out.println("4. Signing message...");
            String message = "Hello, TON!";
            byte[] messageBytes = message.getBytes();
            byte[] signature = Crypto.sign(privateKey, messageBytes);
            System.out.println("   Message signed: " + signature.length + " bytes signature");
            
            // Перевірка підпису
            // Verify signature
            // Проверка подписи
            System.out.println("5. Verifying signature...");
            boolean verified = Crypto.verify(publicKey, messageBytes, signature);
            System.out.println("   Signature verified: " + (verified ? "VALID" : "INVALID"));
            
            // Закриття ресурсів
            // Close resources
            // Закрытие ресурсов
            privateKey.close();
            publicKey.close();
            
            System.out.println("\nExample completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}