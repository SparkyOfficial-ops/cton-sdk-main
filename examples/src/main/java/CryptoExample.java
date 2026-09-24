// CryptoExample.java - приклад використання криптографічних функцій
// Author: Андрій Будильников (Sparky)
// Example of using cryptographic functions
// Пример использования криптографических функций

import java.util.List;

import com.cton.sdk.Crypto;
import com.cton.sdk.Crypto.PrivateKey;
import com.cton.sdk.Crypto.PublicKey;

public class CryptoExample {
    public static void main(String[] args) {
        System.out.println("CTON-SDK Crypto Example");
        System.out.println("======================");
        
        try {
            // Generate a new private key
            System.out.println("1. Generating private key...");
            PrivateKey privateKey = PrivateKey.generate();
            System.out.println("   Private key generated successfully");
            
            // Get the private key data
            byte[] privateKeyData = privateKey.getData();
            System.out.println("   Private key size: " + privateKeyData.length + " bytes");
            
            // Get the corresponding public key
            System.out.println("2. Deriving public key...");
            PublicKey publicKey = privateKey.getPublicKey();
            System.out.println("   Public key derived successfully");
            
            // Get the public key data
            byte[] publicKeyData = publicKey.getData();
            System.out.println("   Public key size: " + publicKeyData.length + " bytes");
            
            // Generate a mnemonic phrase
            System.out.println("3. Generating mnemonic phrase...");
            List<String> mnemonic = Crypto.generateMnemonic();
            System.out.println("   Mnemonic generated: " + String.join(" ", mnemonic.subList(0, 3)) + "...");
            System.out.println("   Total words: " + mnemonic.size());
            
            // Create a private key from mnemonic
            System.out.println("4. Creating private key from mnemonic...");
            PrivateKey mnemonicKey = Crypto.mnemonicToPrivateKey(mnemonic);
            System.out.println("   Private key from mnemonic created successfully");
            
            // Create a message to sign
            System.out.println("5. Signing a message...");
            String messageStr = "Hello, TON!";
            byte[] message = messageStr.getBytes();
            System.out.println("   Message to sign: " + messageStr);
            
            // Sign the message
            byte[] signature = Crypto.sign(privateKey, message);
            System.out.println("   Message signed successfully");
            System.out.println("   Signature size: " + signature.length + " bytes");
            
            // Verify the signature
            System.out.println("6. Verifying signature...");
            boolean isValid = Crypto.verify(publicKey, message, signature);
            System.out.println("   Signature verification: " + (isValid ? "VALID" : "INVALID"));
            
            // Try with a different message
            System.out.println("7. Verifying signature with different message...");
            byte[] differentMessage = "Hello, TON World!".getBytes();
            boolean isInvalid = Crypto.verify(publicKey, differentMessage, signature);
            System.out.println("   Signature verification with different message: " + (isInvalid ? "VALID" : "INVALID"));
            
            System.out.println("\nCrypto example completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error in crypto example: " + e.getMessage());
            e.printStackTrace();
        }
    }
}