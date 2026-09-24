// Crypto.cpp - криптографічні функції для TON
// Author: Андрій Будильников (Sparky)
// Реалізація криптографічних функцій Ed25519 для TON
// Implementation of Ed25519 cryptographic functions for TON
// Реализация криптографических функций Ed25519 для TON

#include "../include/Crypto.h"
#include "../include/Mnemonic.h"
#include <stdexcept>
#include <random>
#include <ctime>
#include <cstring>

// Check if OpenSSL is available via CMake definition
#if defined(OPENSSL_AVAILABLE) && OPENSSL_AVAILABLE
    // OpenSSL implementation
    #include <openssl/evp.h>
    #include <openssl/err.h>
    #include <openssl/ec.h>
    #include <openssl/ecdsa.h>
    #include <openssl/rand.h>
    
    // Debug output to check if OpenSSL is available
    #pragma message("OpenSSL is available and will be used")
    
    // Helper function to handle OpenSSL errors
    static std::string getOpenSSLError() {
        char buffer[256];
        unsigned long err = ERR_get_error();
        if (err != 0) {
            ERR_error_string_n(err, buffer, sizeof(buffer));
            return std::string(buffer);
        }
        return "Unknown OpenSSL error";
    }
#endif

namespace cton {
    
    PrivateKey::PrivateKey() : keyData_(32, 0) {}
    
    PrivateKey::PrivateKey(const std::vector<uint8_t>& keyData) : keyData_(keyData) {
        if (keyData_.size() != 32) {
            throw std::invalid_argument("Private key must be exactly 32 bytes");
        }
    }
    
    PrivateKey PrivateKey::generate() {
        // Generate random data for private key
        
        std::vector<uint8_t> keyData(32);
        
#if OPENSSL_AVAILABLE
        // Use OpenSSL for secure random generation
        if (RAND_bytes(keyData.data(), 32) != 1) {
            throw std::runtime_error("Failed to generate secure random bytes");
        }
#else
        throw std::runtime_error("OpenSSL not available - secure random generation disabled");
#endif
        
        return PrivateKey(keyData);
    }
    
    std::vector<uint8_t> PrivateKey::getData() const {
        return keyData_;
    }
    
    PublicKey PrivateKey::getPublicKey() const {
        // Public key derivation from private key
        
#if OPENSSL_AVAILABLE
        // Use OpenSSL to generate Ed25519 public key
        
        // Create private key from raw data
        EVP_PKEY* privKey = EVP_PKEY_new_raw_private_key(EVP_PKEY_ED25519, nullptr, 
                                                        keyData_.data(), keyData_.size());
        if (!privKey) {
            throw std::runtime_error("Failed to create Ed25519 private key");
        }
        
        // Derive public key from private key
        size_t pubKeyLen = 32;
        std::vector<uint8_t> pubKeyData(pubKeyLen);
        if (EVP_PKEY_get_raw_public_key(privKey, pubKeyData.data(), &pubKeyLen) <= 0) {
            EVP_PKEY_free(privKey);
            throw std::runtime_error("Failed to derive Ed25519 public key");
        }
        
        EVP_PKEY_free(privKey);
        
        return PublicKey(pubKeyData);
#else
        throw std::runtime_error("OpenSSL not available - Ed25519 public key derivation disabled");
#endif
    }

    PublicKey::PublicKey() : keyData_(32, 0) {}
    
    PublicKey::PublicKey(const std::vector<uint8_t>& keyData) : keyData_(keyData) {
        if (keyData_.size() != 32) {
            throw std::invalid_argument("Public key must be exactly 32 bytes");
        }
    }
    
    std::vector<uint8_t> PublicKey::getData() const {
        return keyData_;
    }
    
    bool PublicKey::verifySignature(const std::vector<uint8_t>& message, 
                                  const std::vector<uint8_t>& signature) const {
        // Signature verification
        
#if OPENSSL_AVAILABLE
        // Use OpenSSL to verify Ed25519 signature
        
        if (signature.size() != 64) {
            // Invalid signature size
            return false;
        }
        
        EVP_PKEY_CTX* ctx = nullptr;
        EVP_PKEY* pkey = nullptr;
        EVP_MD_CTX* md_ctx = nullptr;
        bool result = false;
        
        // Create public key from raw data
        pkey = EVP_PKEY_new_raw_public_key(EVP_PKEY_ED25519, nullptr, keyData_.data(), keyData_.size());
        if (!pkey) {
            throw std::runtime_error("Failed to create Ed25519 public key");
        }
        
        // Create MD context
        md_ctx = EVP_MD_CTX_new();
        if (!md_ctx) {
            EVP_PKEY_free(pkey);
            throw std::runtime_error("Failed to create message digest context");
        }
        
        // Initialize verification
        if (EVP_DigestVerifyInit(md_ctx, &ctx, nullptr, nullptr, pkey) <= 0) {
            EVP_MD_CTX_free(md_ctx);
            EVP_PKEY_free(pkey);
            throw std::runtime_error("Failed to initialize signature verification");
        }
        
        // Verify signature
        int verify_result = EVP_DigestVerify(md_ctx, signature.data(), signature.size(), 
                            message.data(), message.size());
        if (verify_result == 1) {
            result = true;
        }
        
    cleanup:
        if (md_ctx) EVP_MD_CTX_free(md_ctx);
        if (pkey) EVP_PKEY_free(pkey);
        
        return result;
#else
        throw std::runtime_error("OpenSSL not available - Ed25519 signature verification disabled");
#endif
    }

    // Secp256k1PrivateKey implementation
    Secp256k1PrivateKey::Secp256k1PrivateKey() : keyData_(32, 0) {}
    
    Secp256k1PrivateKey::Secp256k1PrivateKey(const std::vector<uint8_t>& keyData) : keyData_(keyData) {
        if (keyData_.size() != 32) {
            throw std::invalid_argument("Secp256k1 private key must be exactly 32 bytes");
        }
    }
    
    Secp256k1PrivateKey Secp256k1PrivateKey::generate() {
        std::vector<uint8_t> keyData(32);
        
#if OPENSSL_AVAILABLE
        // Generate secp256k1 private key using OpenSSL
        EC_KEY* ec_key = EC_KEY_new_by_curve_name(NID_secp256k1);
        if (!ec_key) {
            throw std::runtime_error("Failed to create EC key for secp256k1");
        }
        
        if (EC_KEY_generate_key(ec_key) != 1) {
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to generate secp256k1 key");
        }
        
        // Extract private key data
        const BIGNUM* priv_key = EC_KEY_get0_private_key(ec_key);
        if (!priv_key) {
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to extract private key");
        }
        
        if (BN_bn2binpad(priv_key, keyData.data(), 32) != 32) {
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to convert private key to binary");
        }
        
        EC_KEY_free(ec_key);
#else
        throw std::runtime_error("OpenSSL not available - secp256k1 key generation disabled");
#endif
        
        return Secp256k1PrivateKey(keyData);
    }
    
    std::vector<uint8_t> Secp256k1PrivateKey::getData() const {
        return keyData_;
    }
    
    Secp256k1PublicKey Secp256k1PrivateKey::getPublicKey() const {
#if OPENSSL_AVAILABLE
        // Generate secp256k1 public key from private key using OpenSSL
        EC_KEY* ec_key = EC_KEY_new_by_curve_name(NID_secp256k1);
        if (!ec_key) {
            throw std::runtime_error("Failed to create EC key for secp256k1");
        }
        
        // Create private key BIGNUM from key data
        BIGNUM* priv_bn = BN_bin2bn(keyData_.data(), keyData_.size(), nullptr);
        if (!priv_bn) {
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to create BIGNUM from private key data");
        }
        
        // Set private key
        if (EC_KEY_set_private_key(ec_key, priv_bn) != 1) {
            BN_free(priv_bn);
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to set private key");
        }
        
        // Generate public key from private key
        const EC_GROUP* group = EC_KEY_get0_group(ec_key);
        EC_POINT* pub_point = EC_POINT_new(group);
        if (!pub_point || EC_POINT_mul(group, pub_point, priv_bn, nullptr, nullptr, nullptr) != 1) {
            BN_free(priv_bn);
            if (pub_point) EC_POINT_free(pub_point);
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to generate public key point");
        }
        
        // Set public key
        if (EC_KEY_set_public_key(ec_key, pub_point) != 1) {
            BN_free(priv_bn);
            EC_POINT_free(pub_point);
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to set public key");
        }
        
        // Extract public key in compressed format (33 bytes)
        std::vector<uint8_t> pubKeyData(33);
        size_t pubKeyLen = EC_POINT_point2oct(group, pub_point,
                                             POINT_CONVERSION_COMPRESSED,
                                             pubKeyData.data(), 33, nullptr);
        
        BN_free(priv_bn);
        EC_POINT_free(pub_point);
        EC_KEY_free(ec_key);
        
        if (pubKeyLen > 0) {
            pubKeyData.resize(pubKeyLen);
            return Secp256k1PublicKey(pubKeyData);
        } else {
            throw std::runtime_error("Failed to extract public key");
        }
#else
        throw std::runtime_error("OpenSSL not available - secp256k1 public key derivation disabled");
#endif
    }

    // Secp256k1PublicKey implementation
    Secp256k1PublicKey::Secp256k1PublicKey() : keyData_(65, 0) {}
    
    Secp256k1PublicKey::Secp256k1PublicKey(const std::vector<uint8_t>& keyData) : keyData_(keyData) {
        if (keyData_.size() != 33 && keyData_.size() != 65) {
            throw std::invalid_argument("Secp256k1 public key must be 33 or 65 bytes");
        }
    }
    
    std::vector<uint8_t> Secp256k1PublicKey::getData() const {
        return keyData_;
    }
    
    bool Secp256k1PublicKey::verifySignature(const std::vector<uint8_t>& message, 
                                           const std::vector<uint8_t>& signature) const {
#if OPENSSL_AVAILABLE
        if (signature.size() != 64 && signature.size() != 65) {
            // Invalid signature size
            return false;
        }
        
        // Create EC_KEY from public key data
        EC_KEY* ec_key = EC_KEY_new_by_curve_name(NID_secp256k1);
        if (!ec_key) {
            throw std::runtime_error("Failed to create EC key for secp256k1");
        }
        
        // Set public key
        EC_POINT* pub_point = EC_POINT_new(EC_KEY_get0_group(ec_key));
        if (!pub_point) {
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to create public key point");
        }
        
        if (EC_POINT_oct2point(EC_KEY_get0_group(ec_key), pub_point, 
                              keyData_.data(), keyData_.size(), nullptr) != 1) {
            EC_POINT_free(pub_point);
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to set public key point");
        }
        
        if (EC_KEY_set_public_key(ec_key, pub_point) != 1) {
            EC_POINT_free(pub_point);
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to set public key");
        }
        
        EC_POINT_free(pub_point);
        
        // Create signature BIGNUMs
        BIGNUM* r = BN_bin2bn(signature.data(), 32, nullptr);
        BIGNUM* s = BN_bin2bn(signature.data() + 32, 32, nullptr);
        if (!r || !s) {
            if (r) BN_free(r);
            if (s) BN_free(s);
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to create signature BIGNUMs");
        }
        
        // Create ECDSA_SIG
        ECDSA_SIG* sig = ECDSA_SIG_new();
        if (!sig) {
            BN_free(r);
            BN_free(s);
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to create ECDSA signature");
        }
        
        // Set signature components
        if (ECDSA_SIG_set0(sig, r, s) != 1) {
            BN_free(r);
            BN_free(s);
            ECDSA_SIG_free(sig);
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to set signature components");
        }
        
        // Verify signature
        int result = ECDSA_do_verify(message.data(), message.size(), sig, ec_key);
        
        ECDSA_SIG_free(sig);
        EC_KEY_free(ec_key);
        
        return (result == 1);
#else
        throw std::runtime_error("OpenSSL not available - secp256k1 signature verification disabled");
#endif
    }

    // ChaCha20 implementation
    std::vector<uint8_t> ChaCha20::encrypt(const std::vector<uint8_t>& data,
                                         const std::vector<uint8_t>& key,
                                         const std::vector<uint8_t>& nonce) {
#if OPENSSL_AVAILABLE
        if (key.size() != 32) {
            throw std::invalid_argument("ChaCha20 key must be exactly 32 bytes");
        }
        
        if (nonce.size() != 12) {
            throw std::invalid_argument("ChaCha20 nonce must be exactly 12 bytes");
        }
        
        EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
        if (!ctx) {
            throw std::runtime_error("Failed to create EVP cipher context");
        }
        
        // Initialize encryption
        if (EVP_EncryptInit_ex(ctx, EVP_chacha20(), nullptr, key.data(), nonce.data()) != 1) {
            EVP_CIPHER_CTX_free(ctx);
            throw std::runtime_error("Failed to initialize ChaCha20 encryption");
        }
        
        // Encrypt data
        std::vector<uint8_t> ciphertext(data.size());
        int len;
        
        if (EVP_EncryptUpdate(ctx, ciphertext.data(), &len, data.data(), data.size()) != 1) {
            EVP_CIPHER_CTX_free(ctx);
            throw std::runtime_error("Failed to encrypt data with ChaCha20");
        }
        
        // For stream ciphers like ChaCha20, we don't need to call EVP_EncryptFinal_ex
        // as there's no padding involved
        EVP_CIPHER_CTX_free(ctx);
        
        return ciphertext;
#else
        throw std::runtime_error("OpenSSL not available - ChaCha20 encryption disabled");
#endif
    }
    
    std::vector<uint8_t> ChaCha20::decrypt(const std::vector<uint8_t>& data,
                                         const std::vector<uint8_t>& key,
                                         const std::vector<uint8_t>& nonce) {
        // ChaCha20 decryption is the same as encryption
        return encrypt(data, key, nonce);
    }

    std::vector<uint8_t> Crypto::sign(const PrivateKey& privateKey, 
                                    const std::vector<uint8_t>& message) {
        // Signature creation
        
#if OPENSSL_AVAILABLE
        // Use OpenSSL to create Ed25519 signature
        
        EVP_PKEY_CTX* ctx = nullptr;
        EVP_PKEY* pkey = nullptr;
        EVP_MD_CTX* md_ctx = nullptr;
        std::vector<uint8_t> signature(64);
        size_t sig_len = signature.size();
        bool success = false;
        
        // Create private key from raw data
        pkey = EVP_PKEY_new_raw_private_key(EVP_PKEY_ED25519, nullptr, 
                                           privateKey.getData().data(), 
                                           privateKey.getData().size());
        if (!pkey) {
            throw std::runtime_error("Failed to create Ed25519 private key");
        }
        
        // Create MD context
        md_ctx = EVP_MD_CTX_new();
        if (!md_ctx) {
            EVP_PKEY_free(pkey);
            throw std::runtime_error("Failed to create message digest context");
        }
        
        // Initialize signing
        if (EVP_DigestSignInit(md_ctx, &ctx, nullptr, nullptr, pkey) <= 0) {
            EVP_MD_CTX_free(md_ctx);
            EVP_PKEY_free(pkey);
            throw std::runtime_error("Failed to initialize signature creation");
        }
        
        // Create signature
        if (EVP_DigestSign(md_ctx, signature.data(), &sig_len, 
                          message.data(), message.size()) <= 0) {
            EVP_MD_CTX_free(md_ctx);
            EVP_PKEY_free(pkey);
            throw std::runtime_error("Failed to create Ed25519 signature");
        }
        
        success = true;
        
    cleanup:
        if (md_ctx) EVP_MD_CTX_free(md_ctx);
        if (pkey) EVP_PKEY_free(pkey);
        
        if (success && sig_len == 64) {
            return signature;
        } else {
            throw std::runtime_error("Failed to create Ed25519 signature");
        }
#else
        throw std::runtime_error("OpenSSL not available - Ed25519 signature creation disabled");
#endif
    }

    bool Crypto::verify(const PublicKey& publicKey,
                      const std::vector<uint8_t>& message,
                      const std::vector<uint8_t>& signature) {
        // Перевірка підпису
        // Signature verification
        // Проверка подписи
        
#if OPENSSL_AVAILABLE
        // Спробуємо використати OpenSSL для перевірки підпису Ed25519
        // Try to use OpenSSL to verify Ed25519 signature
        // Попробуем использовать OpenSSL для проверки подписи Ed25519
        
        return publicKey.verifySignature(message, signature);
#else
        throw std::runtime_error("OpenSSL not available - Ed25519 signature verification disabled");
#endif
    }
    
    std::vector<uint8_t> Crypto::signSecp256k1(const Secp256k1PrivateKey& privateKey,
                                             const std::vector<uint8_t>& message) {
#if OPENSSL_AVAILABLE
        // Create EC_KEY from private key data
        EC_KEY* ec_key = EC_KEY_new_by_curve_name(NID_secp256k1);
        if (!ec_key) {
            throw std::runtime_error("Failed to create EC key for secp256k1");
        }
        
        // Create private key BIGNUM from key data
        BIGNUM* priv_bn = BN_bin2bn(privateKey.getData().data(), privateKey.getData().size(), nullptr);
        if (!priv_bn) {
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to create BIGNUM from private key data");
        }
        
        // Set private key
        if (EC_KEY_set_private_key(ec_key, priv_bn) != 1) {
            BN_free(priv_bn);
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to set private key");
        }
        
        // Generate public key from private key
        const EC_GROUP* group = EC_KEY_get0_group(ec_key);
        EC_POINT* pub_point = EC_POINT_new(group);
        if (!pub_point || EC_POINT_mul(group, pub_point, priv_bn, nullptr, nullptr, nullptr) != 1) {
            BN_free(priv_bn);
            if (pub_point) EC_POINT_free(pub_point);
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to generate public key point");
        }
        
        // Set public key
        if (EC_KEY_set_public_key(ec_key, pub_point) != 1) {
            BN_free(priv_bn);
            EC_POINT_free(pub_point);
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to set public key");
        }
        
        BN_free(priv_bn);
        EC_POINT_free(pub_point);
        
        // Sign message
        ECDSA_SIG* sig = ECDSA_do_sign(message.data(), message.size(), ec_key);
        if (!sig) {
            EC_KEY_free(ec_key);
            throw std::runtime_error("Failed to create ECDSA signature");
        }
        
        // Extract signature components
        const BIGNUM* r;
        const BIGNUM* s;
        ECDSA_SIG_get0(sig, &r, &s);
        
        // Convert to byte array (64 bytes total)
        std::vector<uint8_t> signature(64);
        BN_bn2binpad(r, signature.data(), 32);
        BN_bn2binpad(s, signature.data() + 32, 32);
        
        ECDSA_SIG_free(sig);
        EC_KEY_free(ec_key);
        
        return signature;
#else
        throw std::runtime_error("OpenSSL not available - secp256k1 signature creation disabled");
#endif
    }
    
    bool Crypto::verifySecp256k1(const Secp256k1PublicKey& publicKey,
                               const std::vector<uint8_t>& message,
                               const std::vector<uint8_t>& signature) {
        // Delegate to Secp256k1PublicKey's verifySignature method
        return publicKey.verifySignature(message, signature);
    }
    
    std::vector<std::string> Crypto::generateMnemonic() {
        // Генерація мнемонічної фрази BIP-39
        // BIP-39 mnemonic generation
        // Генерация мнемонической фразы BIP-39
    
        // Використовуємо Mnemonic клас для генерації
        // Use Mnemonic class for generation
        // Используем класс Mnemonic для генерации
        return Mnemonic::generate(24);
    }
    
    PrivateKey Crypto::mnemonicToPrivateKey(const std::vector<std::string>& mnemonic) {
        // Mnemonic to private key conversion
    
        // Validate mnemonic
        if (!Mnemonic::isValid(mnemonic)) {
            throw std::invalid_argument("Invalid mnemonic phrase");
        }
        
        // Convert mnemonic to seed
        auto seed = Mnemonic::toSeed(mnemonic);
        
        // Use first 32 bytes of seed as private key
        if (seed.size() >= 32) {
            return PrivateKey(std::vector<uint8_t>(seed.begin(), seed.begin() + 32));
        } else {
            throw std::runtime_error("Generated seed is too short");
        }
    }

    PrivateKey Crypto::mnemonicToPrivateKeyTon(const std::vector<std::string>& mnemonic, bool fast) {
        // Перевірка та виведення ключа саме за стандартом TON (Tonkeeper/MyTonWallet)
        // Validate & derive using the TON-native mnemonic standard
        if (!Mnemonic::isTonMnemonic(mnemonic)) {
            throw std::invalid_argument("Invalid TON mnemonic phrase");
        }
        
        // Сід = PBKDF2-HMAC-SHA512(слова, "TON default seed"/"TON quick seed", 100000/1000, 64)
        auto seed = Mnemonic::toTonSeed(mnemonic, fast);
        
        // Перші 32 байти сіду — це Ed25519 seed (private key)
        if (seed.size() >= 32) {
            return PrivateKey(std::vector<uint8_t>(seed.begin(), seed.begin() + 32));
        } else {
            throw std::runtime_error("Generated TON seed is too short");
        }
    }

}