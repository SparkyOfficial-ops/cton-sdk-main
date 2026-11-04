// Crypto.h - криптографічні функції для TON
// Author: Андрій Будильников (Sparky)
// Криптографічні функції Ed25519 для TON
// Ed25519 cryptographic functions for TON
// Криптографические функции Ed25519 для TON

#ifndef CTON_CRYPTO_H
#define CTON_CRYPTO_H

#include <vector>
#include <string>
#include <memory>
#include <cstdint>

// Export definitions for Windows DLL
#ifdef _WIN32
    #ifdef CTON_SDK_CORE_EXPORTS
        #define CTON_SDK_CORE_API __declspec(dllexport)
    #else
        #define CTON_SDK_CORE_API __declspec(dllimport)
    #endif
#else
    #define CTON_SDK_CORE_API
#endif

namespace cton {
    
    // Forward declaration
    class Mnemonic;
    
    class CTON_SDK_CORE_API PrivateKey {
    public:
        /**
         * Конструктор за замовчуванням
         */
        PrivateKey();
        
        /**
         * Конструктор з даних ключа
         * @param keyData дані ключа (32 байти)
         */
        explicit PrivateKey(const std::vector<uint8_t>& keyData);
        
        /**
         * Генерація нового приватного ключа
         * @return новий приватний ключ
         */
        static PrivateKey generate();
        
        /**
         * Отримати дані ключа
         * @return вектор байтів ключа
         */
        std::vector<uint8_t> getData() const;
        
        /**
         * Отримати відповідний публічний ключ
         * @return публічний ключ
         */
        class PublicKey getPublicKey() const;
        
    private:
        std::vector<uint8_t> keyData_;
    };
    
    class CTON_SDK_CORE_API PublicKey {
    public:
        /**
         * Конструктор за замовчуванням
         */
        PublicKey();
        
        /**
         * Конструктор з даних ключа
         * @param keyData дані ключа (32 байти)
         */
        explicit PublicKey(const std::vector<uint8_t>& keyData);
        
        /**
         * Отримати дані ключа
         * @return вектор байтів ключа
         */
        std::vector<uint8_t> getData() const;
        
        /**
         * Перевірити підпис
         * @param message повідомлення
         * @param signature підпис
         * @return true якщо підпис коректний
         */
        bool verifySignature(const std::vector<uint8_t>& message, 
                           const std::vector<uint8_t>& signature) const;
        
    private:
        std::vector<uint8_t> keyData_;
    };
    
    // Secp256k1 key classes for Bitcoin/Ethereum compatibility
    class CTON_SDK_CORE_API Secp256k1PrivateKey {
    public:
        /**
         * Конструктор за замовчуванням
         */
        Secp256k1PrivateKey();
        
        /**
         * Конструктор з даних ключа
         * @param keyData дані ключа (32 байти)
         */
        explicit Secp256k1PrivateKey(const std::vector<uint8_t>& keyData);
        
        /**
         * Генерація нового приватного ключа secp256k1
         * @return новий приватний ключ
         */
        static Secp256k1PrivateKey generate();
        
        /**
         * Отримати дані ключа
         * @return вектор байтів ключа
         */
        std::vector<uint8_t> getData() const;
        
        /**
         * Отримати відповідний публічний ключ
         * @return публічний ключ
         */
        class Secp256k1PublicKey getPublicKey() const;
        
    private:
        std::vector<uint8_t> keyData_;
    };
    
    class CTON_SDK_CORE_API Secp256k1PublicKey {
    public:
        /**
         * Конструктор за замовчуванням
         */
        Secp256k1PublicKey();
        
        /**
         * Конструктор з даних ключа
         * @param keyData дані ключа (33 байти для compressed, 65 байтів для uncompressed)
         */
        explicit Secp256k1PublicKey(const std::vector<uint8_t>& keyData);
        
        /**
         * Отримати дані ключа
         * @return вектор байтів ключа
         */
        std::vector<uint8_t> getData() const;
        
        /**
         * Перевірити підпис
         * @param message повідомлення
         * @param signature підпис
         * @return true якщо підпис коректний
         */
        bool verifySignature(const std::vector<uint8_t>& message, 
                           const std::vector<uint8_t>& signature) const;
        
    private:
        std::vector<uint8_t> keyData_;
    };
    
    // ChaCha20 encryption/decryption for wallet security
    class CTON_SDK_CORE_API ChaCha20 {
    public:
        /**
         * Шифрування даних за допомогою ChaCha20
         * @param data дані для шифрування
         * @param key ключ шифрування (32 байти)
         * @param nonce nonce (12 байтів)
         * @return зашифровані дані
         */
        static std::vector<uint8_t> encrypt(const std::vector<uint8_t>& data,
                                          const std::vector<uint8_t>& key,
                                          const std::vector<uint8_t>& nonce);
        
        /**
         * Розшифрування даних за допомогою ChaCha20
         * @param data зашифровані дані
         * @param key ключ шифрування (32 байти)
         * @param nonce nonce (12 байтів)
         * @return розшифровані дані
         */
        static std::vector<uint8_t> decrypt(const std::vector<uint8_t>& data,
                                          const std::vector<uint8_t>& key,
                                          const std::vector<uint8_t>& nonce);
    };
    
    class CTON_SDK_CORE_API Crypto {
    public:
        /**
         * Підписати повідомлення приватним ключем
         * @param privateKey приватний ключ
         * @param message повідомлення для підпису
         * @return підпис (64 байти)
         */
        static std::vector<uint8_t> sign(const PrivateKey& privateKey, 
                                       const std::vector<uint8_t>& message);
        
        /**
         * Перевірити підпис публічним ключем
         * @param publicKey публічний ключ
         * @param message повідомлення
         * @param signature підпис
         * @return true якщо підпис коректний
         */
        static bool verify(const PublicKey& publicKey,
                         const std::vector<uint8_t>& message,
                         const std::vector<uint8_t>& signature);
        
        /**
         * Підписати повідомлення приватним ключем secp256k1
         * @param privateKey приватний ключ secp256k1
         * @param message повідомлення для підпису
         * @return підпис (64 байти для recoverable, 65 байтів для DER)
         */
        static std::vector<uint8_t> signSecp256k1(const Secp256k1PrivateKey& privateKey,
                                                const std::vector<uint8_t>& message);
        
        /**
         * Перевірити підпис публічним ключем secp256k1
         * @param publicKey публічний ключ secp256k1
         * @param message повідомлення
         * @param signature підпис
         * @return true якщо підпис коректний
         */
        static bool verifySecp256k1(const Secp256k1PublicKey& publicKey,
                                  const std::vector<uint8_t>& message,
                                  const std::vector<uint8_t>& signature);
        
        /**
         * Генерація mnemonic фрази (24 слова)
         * @return вектор слів
         */
        static std::vector<std::string> generateMnemonic();
        
        /**
         * Створення ключа з mnemonic фрази
         * @param mnemonic мнемонічна фраза
         * @return приватний ключ
         */
        static PrivateKey mnemonicToPrivateKey(const std::vector<std::string>& mnemonic);
        
        /**
         * Створення ключа з mnemonic фрази за алгоритмом TON
         * (сід = Mnemonic::toTonSeed, приватний ключ = перші 32 байти сіду)
         * @param mnemonic мнемонічна фраза
         * @param fast швидкий режим генерації сіду
         * @return приватний ключ
         */
        static PrivateKey mnemonicToPrivateKeyTon(const std::vector<std::string>& mnemonic,
                                                  bool fast = false);
    };
    
}

#endif // CTON_CRYPTO_H