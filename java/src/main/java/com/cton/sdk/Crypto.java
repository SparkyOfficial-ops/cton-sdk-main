// Crypto.java - Java обгортка для Crypto C++ класу
// Author: Андрій Будильников (Sparky)

package com.cton.sdk;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Java обгортка для Crypto C++ класу
 * 
 * Реалізація криптографічних функцій Ed25519 для TON
 */
public class Crypto {
    // Інтерфейс для взаємодії з нативною бібліотекою
    public interface CtonLibrary extends Library {
        CtonLibrary INSTANCE = (CtonLibrary) Native.load("cton-sdk-core", CtonLibrary.class);
        
        // Створення нового приватного ключа
        Pointer private_key_create();
        
        // Створення приватного ключа з даних
        Pointer private_key_create_from_data(byte[] keyData, int length);
        
        // Звільнення пам'яті приватного ключа
        void private_key_destroy(Pointer privateKey);
        
        // Генерація нового приватного ключа
        Pointer private_key_generate();
        
        // Отримання даних приватного ключа
        int private_key_get_data(Pointer privateKey, byte[] buffer, int bufferSize);
        
        // Отримання публічного ключа з приватного
        Pointer private_key_get_public_key(Pointer privateKey);

        // Secp256k1 private key functions
        Pointer secp256k1_private_key_create();
        Pointer secp256k1_private_key_create_from_data(byte[] keyData, int length);
        void secp256k1_private_key_destroy(Pointer privateKey);
        Pointer secp256k1_private_key_generate();
        int secp256k1_private_key_get_data(Pointer privateKey, byte[] buffer, int bufferSize);
        Pointer secp256k1_private_key_get_public_key(Pointer privateKey);

        // Створення нового публічного ключа
        Pointer public_key_create();
        
        // Створення публічного ключа з даних
        Pointer public_key_create_from_data(byte[] keyData, int length);
        
        // Звільнення пам'яті публічного ключа
        void public_key_destroy(Pointer publicKey);
        
        // Отримання даних публічного ключа
        int public_key_get_data(Pointer publicKey, byte[] buffer, int bufferSize);
        
        // Перевірка підпису
        boolean public_key_verify_signature(Pointer publicKey, byte[] message, int messageLen, byte[] signature, int signatureLen);

        // Secp256k1 public key functions
        Pointer secp256k1_public_key_create();
        Pointer secp256k1_public_key_create_from_data(byte[] keyData, int length);
        void secp256k1_public_key_destroy(Pointer publicKey);
        int secp256k1_public_key_get_data(Pointer publicKey, byte[] buffer, int bufferSize);
        boolean secp256k1_public_key_verify_signature(Pointer publicKey, byte[] message, int messageLen, byte[] signature, int signatureLen);
        
        // Створення підпису
        Pointer crypto_sign(Pointer privateKey, byte[] message, int messageLen);
        
        // Перевірка підпису
        boolean crypto_verify(Pointer publicKey, byte[] message, int messageLen, byte[] signature, int signatureLen);

        // Створення підпису для ключа Secp256k1
        Pointer crypto_sign_secp256k1(Pointer privateKey, byte[] message, int messageLen);
        
        // Перевірка підпису для ключа Secp256k1
        boolean crypto_verify_secp256k1(Pointer publicKey, byte[] message, int messageLen, byte[] signature, int signatureLen);
        
        // Генерація мнемонічної фрази
        Pointer crypto_generate_mnemonic();
        
        // Створення приватного ключа з мнемоніки
        Pointer crypto_mnemonic_to_private_key(Pointer mnemonic);
        
        // Створення приватного ключа з мнемоніки за стандартом TON (toTonSeed)
        Pointer crypto_mnemonic_to_private_key_ton(Pointer mnemonic, boolean fast);
        
        // ChaCha20 encryption
        Pointer crypto_chacha20_encrypt(byte[] data, int dataLen, byte[] key, byte[] nonce);
        
        void cton_free(Pointer ptr);

        // Функція для звільнення пам'яті рядків
        void free_string(Pointer str);
        
        // Функція для звільнення масиву рядків
        void free_string_array(Pointer strArray);
    }
    
    /**
     * Представляє приватний ключ Ed25519
     */
    public static class PrivateKey implements Closeable {
        private Pointer nativePrivateKey;
        private boolean closed = false;
        
        /**
         * Конструктор за замовчуванням
         */
        public PrivateKey() {
            this.nativePrivateKey = CtonLibrary.INSTANCE.private_key_create();
        }
        
        /**
         * Конструктор з даних
         * @param keyData дані ключа (32 байти)
         */
        public PrivateKey(byte[] keyData) {
            if (keyData.length != 32) {
                throw new IllegalArgumentException("Private key data must be exactly 32 bytes");
            }
            this.nativePrivateKey = CtonLibrary.INSTANCE.private_key_create_from_data(keyData, keyData.length);
        }
        
        /**
         * Приватний конструктор для внутрішнього використання
         */
        private PrivateKey(Pointer nativePrivateKey) {
            this.nativePrivateKey = nativePrivateKey;
        }
        
        /**
         * Згенерувати новий приватний ключ
         * @return новий приватний ключ
         */
        public static PrivateKey generate() {
            Pointer ptr = CtonLibrary.INSTANCE.private_key_generate();
            return new PrivateKey(ptr);
        }
        
        /**
         * Отримати дані ключа
         * @return вектор байтів ключа
         */
        public byte[] getData() {
            if (closed) {
                throw new IllegalStateException("PrivateKey has been closed");
            }
            byte[] buffer = new byte[32];
            int result = CtonLibrary.INSTANCE.private_key_get_data(nativePrivateKey, buffer, 32);
            if (result != 32) {
                throw new RuntimeException("Failed to get private key data");
            }
            return buffer;
        }
        
        /**
         * Отримати відповідний публічний ключ
         * @return публічний ключ
         */
        public PublicKey getPublicKey() {
            if (closed) {
                throw new IllegalStateException("PrivateKey has been closed");
            }
            Pointer ptr = CtonLibrary.INSTANCE.private_key_get_public_key(nativePrivateKey);
            return new PublicKey(ptr);
        }
        
        /**
         * Закрити об'єкт і звільнити нативні ресурси
         */
        @Override
        public void close() {
            if (!closed && nativePrivateKey != null) {
                CtonLibrary.INSTANCE.private_key_destroy(nativePrivateKey);
                nativePrivateKey = null;
                closed = true;
            }
        }
    }
    
    /**
     * Представляє публічний ключ Ed25519
     */
    public static class PublicKey implements Closeable {
        private Pointer nativePublicKey;
        private boolean closed = false;
        
        /**
         * Конструктор за замовчуванням
         */
        public PublicKey() {
            this.nativePublicKey = CtonLibrary.INSTANCE.public_key_create();
        }
        
        /**
         * Конструктор з даних
         * @param keyData дані ключа (32 байти)
         */
        public PublicKey(byte[] keyData) {
            if (keyData.length != 32) {
                throw new IllegalArgumentException("Public key data must be exactly 32 bytes");
            }
            this.nativePublicKey = CtonLibrary.INSTANCE.public_key_create_from_data(keyData, keyData.length);
        }
        
        /**
         * Приватний конструктор для внутрішнього використання
         */
        private PublicKey(Pointer nativePublicKey) {
            this.nativePublicKey = nativePublicKey;
        }
        
        /**
         * Отримати дані ключа
         * @return вектор байтів ключа
         */
        public byte[] getData() {
            if (closed) {
                throw new IllegalStateException("PublicKey has been closed");
            }
            byte[] buffer = new byte[32];
            int result = CtonLibrary.INSTANCE.public_key_get_data(nativePublicKey, buffer, 32);
            if (result != 32) {
                throw new RuntimeException("Failed to get public key data");
            }
            return buffer;
        }
        
        /**
         * Перевірити підпис
         * @param message повідомлення
         * @param signature підпис
         * @return true якщо підпис коректний
         */
        public boolean verifySignature(byte[] message, byte[] signature) {
            if (closed) {
                throw new IllegalStateException("PublicKey has been closed");
            }
            return CtonLibrary.INSTANCE.public_key_verify_signature(
                nativePublicKey, message, message.length, signature, signature.length);
        }
        
        /**
         * Закрити об'єкт і звільнити нативні ресурси
         */
        @Override
        public void close() {
            if (!closed && nativePublicKey != null) {
                CtonLibrary.INSTANCE.public_key_destroy(nativePublicKey);
                nativePublicKey = null;
                closed = true;
            }
        }
    }
    
    /**
     * Представляє приватний ключ Secp256k1
     */
    public static class Secp256k1PrivateKey implements Closeable {
        private Pointer nativePrivateKey;
        private boolean closed = false;
        
        /**
         * Конструктор за замовчуванням
         */
        public Secp256k1PrivateKey() {
            this.nativePrivateKey = CtonLibrary.INSTANCE.secp256k1_private_key_create();
        }
        
        /**
         * Конструктор з даних
         * @param keyData дані ключа (32 байти)
         */
        public Secp256k1PrivateKey(byte[] keyData) {
            if (keyData.length != 32) {
                throw new IllegalArgumentException("Secp256k1 private key data must be exactly 32 bytes");
            }
            this.nativePrivateKey = CtonLibrary.INSTANCE.secp256k1_private_key_create_from_data(keyData, keyData.length);
        }
        
        /**
         * Приватний конструктор для внутрішнього використання
         */
        private Secp256k1PrivateKey(Pointer nativePrivateKey) {
            this.nativePrivateKey = nativePrivateKey;
        }
        
        /**
         * Згенерувати новий приватний ключ
         * @return новий приватний ключ
         */
        public static Secp256k1PrivateKey generate() {
            Pointer ptr = CtonLibrary.INSTANCE.secp256k1_private_key_generate();
            return new Secp256k1PrivateKey(ptr);
        }
        
        /**
         * Отримати дані ключа
         * @return вектор байтів ключа
         */
        public byte[] getData() {
            if (closed) {
                throw new IllegalStateException("Secp256k1PrivateKey has been closed");
            }
            byte[] buffer = new byte[32];
            int result = CtonLibrary.INSTANCE.secp256k1_private_key_get_data(nativePrivateKey, buffer, 32);
            if (result != 32) {
                throw new RuntimeException("Failed to get secp256k1 private key data");
            }
            return buffer;
        }
        
        /**
         * Отримати відповідний публічний ключ
         * @return публічний ключ
         */
        public Secp256k1PublicKey getPublicKey() {
            if (closed) {
                throw new IllegalStateException("Secp256k1PrivateKey has been closed");
            }
            Pointer ptr = CtonLibrary.INSTANCE.secp256k1_private_key_get_public_key(nativePrivateKey);
            return new Secp256k1PublicKey(ptr);
        }
        
        /**
         * Закрити об'єкт і звільнити нативні ресурси
         */
        @Override
        public void close() {
            if (!closed && nativePrivateKey != null) {
                CtonLibrary.INSTANCE.secp256k1_private_key_destroy(nativePrivateKey);
                nativePrivateKey = null;
                closed = true;
            }
        }
    }
    
    /**
     * Представляє публічний ключ Secp256k1
     */
    public static class Secp256k1PublicKey implements Closeable {
        private Pointer nativePublicKey;
        private boolean closed = false;
        
        /**
         * Конструктор за замовчуванням
         */
        public Secp256k1PublicKey() {
            this.nativePublicKey = CtonLibrary.INSTANCE.secp256k1_public_key_create();
        }
        
        /**
         * Конструктор з даних
         * @param keyData дані ключа (33 або 65 байтів)
         */
        public Secp256k1PublicKey(byte[] keyData) {
            if (keyData.length != 33 && keyData.length != 65) {
                throw new IllegalArgumentException("Secp256k1 public key data must be 33 or 65 bytes");
            }
            this.nativePublicKey = CtonLibrary.INSTANCE.secp256k1_public_key_create_from_data(keyData, keyData.length);
        }
        
        /**
         * Приватний конструктор для внутрішнього використання
         */
        private Secp256k1PublicKey(Pointer nativePublicKey) {
            this.nativePublicKey = nativePublicKey;
        }
        
        /**
         * Отримати дані ключа
         * @return вектор байтів ключа
         */
        public byte[] getData() {
            if (closed) {
                throw new IllegalStateException("Secp256k1PublicKey has been closed");
            }
            // Use a buffer that can accommodate both compressed (33 bytes) and uncompressed (65 bytes) keys
            byte[] buffer = new byte[65];
            int result = CtonLibrary.INSTANCE.secp256k1_public_key_get_data(nativePublicKey, buffer, 65);
            if (result > 0) {
                // Return only the actual data
                return Arrays.copyOf(buffer, result);
            } else {
                throw new RuntimeException("Failed to get secp256k1 public key data");
            }
        }
        
        /**
         * Перевірити підпис
         * @param message повідомлення
         * @param signature підпис
         * @return true якщо підпис коректний
         */
        public boolean verifySignature(byte[] message, byte[] signature) {
            if (closed) {
                throw new IllegalStateException("Secp256k1PublicKey has been closed");
            }
            return CtonLibrary.INSTANCE.secp256k1_public_key_verify_signature(
                nativePublicKey, message, message.length, signature, signature.length);
        }
        
        /**
         * Закрити об'єкт і звільнити нативні ресурси
         */
        @Override
        public void close() {
            if (!closed && nativePublicKey != null) {
                CtonLibrary.INSTANCE.secp256k1_public_key_destroy(nativePublicKey);
                nativePublicKey = null;
                closed = true;
            }
        }
    }
    
    /**
     * Шифрування даних за допомогою ChaCha20
     */
    public static class ChaCha20 {
        /**
         * Шифрування даних за допомогою ChaCha20
         * @param data дані для шифрування
         * @param key ключ шифрування (32 байти)
         * @param nonce nonce (12 байтів)
         * @return зашифровані дані
         */
        public static byte[] encrypt(byte[] data, byte[] key, byte[] nonce) {
            // Використовуємо нативну реалізацію ChaCha20 через OpenSSL
            // Use native ChaCha20 implementation through OpenSSL
            return encryptNative(data, key, nonce);
        }
        
        /**
         * Розшифрування даних за допомогою ChaCha20
         * @param data зашифровані дані
         * @param key ключ шифрування (32 байти)
         * @param nonce nonce (12 байтів)
         * @return розшифровані дані
         */
        public static byte[] decrypt(byte[] data, byte[] key, byte[] nonce) {
            // ChaCha20 decryption is the same as encryption
            return encrypt(data, key, nonce);
        }
        
        // Нативна реалізація ChaCha20
        // Native ChaCha20 implementation
        private static byte[] encryptNative(byte[] data, byte[] key, byte[] nonce) {
            if (data == null || key == null || nonce == null) {
                throw new IllegalArgumentException("Data, key, and nonce cannot be null");
            }
            
            if (key.length != 32) {
                throw new IllegalArgumentException("Key must be exactly 32 bytes");
            }
            
            if (nonce.length != 12) {
                throw new IllegalArgumentException("Nonce must be exactly 12 bytes");
            }
            
            Pointer resultPtr = CtonLibrary.INSTANCE.crypto_chacha20_encrypt(
                data, data.length, key, nonce);
            
            if (resultPtr == null) {
                // Return empty array if encryption failed
                return new byte[0];
            }
            
            // For ChaCha20, the output size is the same as the input size
            byte[] result = resultPtr.getByteArray(0, data.length);
            
            // Free the memory allocated in C++
            CtonLibrary.INSTANCE.cton_free(resultPtr);
            
            return result;
        }
    }
    
    /**
     * Підписати повідомлення приватним ключем
     * @param privateKey приватний ключ
     * @param message повідомлення для підпису
     * @return підпис (64 байти)
     */
    public static byte[] sign(PrivateKey privateKey, byte[] message) {
        if (privateKey.closed) {
            throw new IllegalStateException("PrivateKey has been closed");
        }
        Pointer signaturePtr = CtonLibrary.INSTANCE.crypto_sign(
            privateKey.nativePrivateKey, message, message.length);
        
        // Якщо підпис успішно створено, конвертуємо його в byte[]
        // If signature is successfully created, convert it to byte[]
        if (signaturePtr != null) {
            // Convert Pointer to byte[]
            byte[] signature = signaturePtr.getByteArray(0, 64);
            // Free memory allocated in C++
            CtonLibrary.INSTANCE.cton_free(signaturePtr);
            return signature;
        } else {
            // У випадку помилки повертаємо порожній масив
            // In case of error, return empty array
            return new byte[0];
        }
    }
    
    /**
     * Перевірити підпис публічним ключем
     * @param publicKey публічний ключ
     * @param message повідомлення
     * @param signature підпис
     * @return true якщо підпис коректний
     */
    public static boolean verify(PublicKey publicKey, byte[] message, byte[] signature) {
        if (publicKey.closed) {
            throw new IllegalStateException("PublicKey has been closed");
        }
        return CtonLibrary.INSTANCE.crypto_verify(
            publicKey.nativePublicKey, message, message.length, signature, signature.length);
    }
    
    /**
     * Підписати повідомлення приватним ключем Secp256k1
     * @param privateKey приватний ключ Secp256k1
     * @param message повідомлення для підпису
     * @return підпис (64 байти)
     */
    public static byte[] signSecp256k1(Secp256k1PrivateKey privateKey, byte[] message) {
        if (privateKey.closed) {
            throw new IllegalStateException("Secp256k1PrivateKey has been closed");
        }
        Pointer signaturePtr = CtonLibrary.INSTANCE.crypto_sign_secp256k1(
            privateKey.nativePrivateKey, message, message.length);
        
        // Якщо підпис успішно створено, конвертуємо його в byte[]
        // If signature is successfully created, convert it to byte[]
        if (signaturePtr != null) {
            // Convert Pointer to byte[]
            byte[] signature = signaturePtr.getByteArray(0, 64);
            // Free memory allocated in C++
            CtonLibrary.INSTANCE.cton_free(signaturePtr);
            return signature;
        } else {
            // У випадку помилки повертаємо порожній масив
            // In case of error, return empty array
            return new byte[0];
        }
    }
    
    /**
     * Перевірити підпис публічним ключем Secp256k1
     * @param publicKey публічний ключ Secp256k1
     * @param message повідомлення
     * @param signature підпис
     * @return true якщо підпис коректний
     */
    public static boolean verifySecp256k1(Secp256k1PublicKey publicKey, byte[] message, byte[] signature) {
        if (publicKey.closed) {
            throw new IllegalStateException("Secp256k1PublicKey has been closed");
        }
        return CtonLibrary.INSTANCE.crypto_verify_secp256k1(
            publicKey.nativePublicKey, message, message.length, signature, signature.length);
    }
    
    /**
     * Генерація mnemonic фрази (24 слова)
     * @return вектор слів
     */
    public static List<String> generateMnemonic() {
        Pointer mnemonicPtr = CtonLibrary.INSTANCE.crypto_generate_mnemonic();
        
        // Convert Pointer to List<String>
        if (mnemonicPtr == null) {
            return new ArrayList<>();
        }
        
        List<String> result = new ArrayList<>();
        int index = 0;
        Pointer stringPtr;
        while ((stringPtr = mnemonicPtr.getPointer(index * Native.POINTER_SIZE)) != null) {
            String word = stringPtr.getString(0);
            result.add(word);
            index++;
        }
        
        // Free the array itself
        CtonLibrary.INSTANCE.free_string_array(mnemonicPtr);
        
        return result;
    }
    
    /**
     * Створення ключа з mnemonic фрази
     * @param mnemonic мнемонічна фраза
     * @return приватний ключ
     */
    public static PrivateKey mnemonicToPrivateKey(List<String> mnemonic) {
        // Convert List<String> to array of C strings
        if (mnemonic == null || mnemonic.isEmpty()) {
            throw new IllegalArgumentException("Mnemonic cannot be null or empty");
        }
        
        // Create array of strings
        String[] mnemonicArray = mnemonic.toArray(new String[0]);
        
        // Create array of pointers to strings
        Pointer[] pointers = new Pointer[mnemonicArray.length + 1]; // +1 for null terminator
        for (int i = 0; i < mnemonicArray.length; i++) {
            pointers[i] = new Memory(mnemonicArray[i].length() + 1);
            pointers[i].setString(0, mnemonicArray[i]);
        }
        pointers[mnemonicArray.length] = null; // Null terminator
        
        // Create pointer to array of pointers
        Memory pointerArray = new Memory(pointers.length * Native.POINTER_SIZE);
        for (int i = 0; i < pointers.length; i++) {
            pointerArray.setPointer(i * Native.POINTER_SIZE, pointers[i]);
        }
        
        Pointer privateKeyPtr = CtonLibrary.INSTANCE.crypto_mnemonic_to_private_key(pointerArray);
        
        if (privateKeyPtr == null) {
            throw new RuntimeException("Failed to generate private key from mnemonic");
        }
        
        return new PrivateKey(privateKeyPtr);
    }
    
    /**
     * Створення ключа з mnemonic фрази за стандартом TON
     * (Tonkeeper / MyTonWallet / Telegram Wallet: PBKDF2-HMAC-SHA512, сіль "TON default seed")
     * @param mnemonic мнемонічна фраза
     * @param fast швидкий режим (1000 ітерацій) замість стандартного (100000)
     * @return приватний ключ
     */
    public static PrivateKey mnemonicToPrivateKeyFromTon(List<String> mnemonic, boolean fast) {
        if (mnemonic == null || mnemonic.isEmpty()) {
            throw new IllegalArgumentException("Mnemonic cannot be null or empty");
        }
        
        // Create array of strings
        String[] mnemonicArray = mnemonic.toArray(new String[0]);
        
        // Create array of pointers to strings (+1 for null terminator)
        Pointer[] pointers = new Pointer[mnemonicArray.length + 1];
        for (int i = 0; i < mnemonicArray.length; i++) {
            pointers[i] = new Memory(mnemonicArray[i].length() + 1);
            pointers[i].setString(0, mnemonicArray[i]);
        }
        pointers[mnemonicArray.length] = null;
        
        // Create pointer to array of pointers
        Memory pointerArray = new Memory(pointers.length * Native.POINTER_SIZE);
        for (int i = 0; i < pointers.length; i++) {
            pointerArray.setPointer(i * Native.POINTER_SIZE, pointers[i]);
        }
        
        Pointer privateKeyPtr = CtonLibrary.INSTANCE.crypto_mnemonic_to_private_key_ton(pointerArray, fast);
        
        if (privateKeyPtr == null) {
            throw new RuntimeException("Failed to generate private key from TON mnemonic");
        }
        
        return new PrivateKey(privateKeyPtr);
    }
    
    /**
     * Створення ключа з mnemonic фрази за стандартом TON (стандартний режим, 100000 ітерацій)
     * @param mnemonic мнемонічна фраза
     * @return приватний ключ
     */
    public static PrivateKey mnemonicToPrivateKeyFromTon(List<String> mnemonic) {
        return mnemonicToPrivateKeyFromTon(mnemonic, false);
    }
}