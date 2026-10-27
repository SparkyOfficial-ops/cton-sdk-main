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
    #include <openssl/core_names.h>
    #include <openssl/params.h>
    #include <openssl/bn.h>
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

    static EVP_PKEY* secp256k1FromPrivate(const uint8_t* key, size_t length) {
        if (!key || length != 32) {
            return nullptr;
        }
        unsigned char priv[32];
        std::memcpy(priv, key, 32);
        char group[] = "secp256k1";
        OSSL_PARAM params[] = {
            OSSL_PARAM_construct_utf8_string(OSSL_PKEY_PARAM_GROUP_NAME, group, 0),
            OSSL_PARAM_construct_BN(OSSL_PKEY_PARAM_PRIV_KEY, priv, sizeof(priv)),
            OSSL_PARAM_construct_end()
        };
        EVP_PKEY_CTX* ctx = EVP_PKEY_CTX_new_from_name(nullptr, "EC", nullptr);
        if (!ctx || EVP_PKEY_fromdata_init(ctx) != 1) {
            EVP_PKEY_CTX_free(ctx);
            return nullptr;
        }
        EVP_PKEY* pkey = nullptr;
        if (EVP_PKEY_fromdata(ctx, &pkey, EVP_PKEY_KEYPAIR, params) != 1) {
            EVP_PKEY_CTX_free(ctx);
            return nullptr;
        }
        EVP_PKEY_CTX_free(ctx);
        return pkey;
    }

    static EVP_PKEY* secp256k1FromPublic(const uint8_t* key, size_t length) {
        if (!key || (length != 33 && length != 65)) {
            return nullptr;
        }
        std::vector<uint8_t> point(key, key + length);
        char group[] = "secp256k1";
        OSSL_PARAM params[] = {
            OSSL_PARAM_construct_utf8_string(OSSL_PKEY_PARAM_GROUP_NAME, group, 0),
            OSSL_PARAM_construct_octet_string(OSSL_PKEY_PARAM_PUB_KEY, point.data(), point.size()),
            OSSL_PARAM_construct_end()
        };
        EVP_PKEY_CTX* ctx = EVP_PKEY_CTX_new_from_name(nullptr, "EC", nullptr);
        if (!ctx || EVP_PKEY_fromdata_init(ctx) != 1) {
            EVP_PKEY_CTX_free(ctx);
            return nullptr;
        }
        EVP_PKEY* pkey = nullptr;
        if (EVP_PKEY_fromdata(ctx, &pkey, EVP_PKEY_PUBLIC_KEY, params) != 1) {
            EVP_PKEY_CTX_free(ctx);
            return nullptr;
        }
        EVP_PKEY_CTX_free(ctx);
        return pkey;
    }

    static std::vector<uint8_t> secp256k1CompressedPublic(EVP_PKEY* pkey) {
        unsigned char point[65];
        size_t len = 0;
        if (EVP_PKEY_get_octet_string_param(pkey, OSSL_PKEY_PARAM_PUB_KEY, point, sizeof(point), &len) != 1) {
            throw std::runtime_error("Failed to export secp256k1 public key: " + getOpenSSLError());
        }
        if (len == 33) {
            return std::vector<uint8_t>(point, point + 33);
        }
        if (len == 65 && point[0] == 0x04) {
            std::vector<uint8_t> compressed(33);
            compressed[0] = (point[64] & 1) ? 0x03 : 0x02;
            std::memcpy(compressed.data() + 1, point + 1, 32);
            return compressed;
        }
        throw std::runtime_error("Unexpected secp256k1 public key encoding");
    }

    static void appendDerInteger(std::vector<uint8_t>& out, const uint8_t* value32) {
        size_t start = 0;
        while (start < 31 && value32[start] == 0) {
            start++;
        }
        bool pad = (value32[start] & 0x80) != 0;
        size_t length = 32 - start + (pad ? 1 : 0);
        out.push_back(0x02);
        out.push_back(static_cast<uint8_t>(length));
        if (pad) {
            out.push_back(0x00);
        }
        out.insert(out.end(), value32 + start, value32 + 32);
    }

    static std::vector<uint8_t> rsToDer(const uint8_t* signature) {
        std::vector<uint8_t> body;
        appendDerInteger(body, signature);
        appendDerInteger(body, signature + 32);
        std::vector<uint8_t> der;
        der.push_back(0x30);
        der.push_back(static_cast<uint8_t>(body.size()));
        der.insert(der.end(), body.begin(), body.end());
        return der;
    }

    static bool derToRs(const uint8_t* der, size_t length, uint8_t* out) {
        if (length < 8 || der[0] != 0x30 || (der[1] & 0x80) != 0) {
            return false;
        }
        size_t sequenceLength = der[1];
        if (2 + sequenceLength > length) {
            return false;
        }
        size_t pos = 2;
        size_t end = 2 + sequenceLength;
        for (int part = 0; part < 2; part++) {
            if (pos >= end || der[pos++] != 0x02 || pos >= end) {
                return false;
            }
            size_t encodedLength = der[pos++];
            if (encodedLength == 0 || encodedLength > 33 || pos + encodedLength > end) {
                return false;
            }
            const uint8_t* value = der + pos;
            size_t valueLength = encodedLength;
            if (value[0] == 0x00) {
                value++;
                valueLength--;
            }
            if (valueLength == 0 || valueLength > 32) {
                return false;
            }
            std::memset(out + part * 32, 0, 32);
            std::memcpy(out + part * 32 + (32 - valueLength), value, valueLength);
            pos += encodedLength;
        }
        return pos == end;
    }

    static std::vector<uint8_t> secp256k1Sign(EVP_PKEY* pkey, const uint8_t* message, size_t length) {
        EVP_PKEY_CTX* ctx = EVP_PKEY_CTX_new_from_pkey(nullptr, pkey, nullptr);
        if (!ctx || EVP_PKEY_sign_init(ctx) != 1) {
            EVP_PKEY_CTX_free(ctx);
            throw std::runtime_error("Failed to initialize secp256k1 signing: " + getOpenSSLError());
        }
        size_t sigLen = 0;
        if (EVP_PKEY_sign(ctx, nullptr, &sigLen, message, length) != 1) {
            EVP_PKEY_CTX_free(ctx);
            throw std::runtime_error("Failed to size secp256k1 signature: " + getOpenSSLError());
        }
        std::vector<uint8_t> der(sigLen);
        if (EVP_PKEY_sign(ctx, der.data(), &sigLen, message, length) != 1) {
            EVP_PKEY_CTX_free(ctx);
            throw std::runtime_error("Failed to create secp256k1 signature: " + getOpenSSLError());
        }
        EVP_PKEY_CTX_free(ctx);
        der.resize(sigLen);
        std::vector<uint8_t> raw(64);
        if (!derToRs(der.data(), der.size(), raw.data())) {
            throw std::runtime_error("Failed to decode secp256k1 signature");
        }
        return raw;
    }

    static bool secp256k1Verify(EVP_PKEY* pkey, const uint8_t* message, size_t length, const uint8_t* signature) {
        std::vector<uint8_t> der = rsToDer(signature);
        EVP_PKEY_CTX* ctx = EVP_PKEY_CTX_new_from_pkey(nullptr, pkey, nullptr);
        if (!ctx || EVP_PKEY_verify_init(ctx) != 1) {
            EVP_PKEY_CTX_free(ctx);
            throw std::runtime_error("Failed to initialize secp256k1 verification: " + getOpenSSLError());
        }
        int result = EVP_PKEY_verify(ctx, der.data(), der.size(), message, length);
        EVP_PKEY_CTX_free(ctx);
        return result == 1;
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
        EVP_PKEY* pkey = EVP_PKEY_Q_keygen(nullptr, nullptr, "EC", "secp256k1");
        if (!pkey) {
            throw std::runtime_error("Failed to generate secp256k1 key: " + getOpenSSLError());
        }
        BIGNUM* priv = nullptr;
        if (EVP_PKEY_get_bn_param(pkey, OSSL_PKEY_PARAM_PRIV_KEY, &priv) != 1 ||
            BN_bn2binpad(priv, keyData.data(), 32) != 32) {
            BN_clear_free(priv);
            EVP_PKEY_free(pkey);
            throw std::runtime_error("Failed to export secp256k1 private key: " + getOpenSSLError());
        }
        BN_clear_free(priv);
        EVP_PKEY_free(pkey);
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
        EVP_PKEY* pkey = secp256k1FromPrivate(keyData_.data(), keyData_.size());
        if (!pkey) {
            throw std::runtime_error("Failed to import secp256k1 private key: " + getOpenSSLError());
        }
        std::vector<uint8_t> pubKeyData = secp256k1CompressedPublic(pkey);
        EVP_PKEY_free(pkey);
        return Secp256k1PublicKey(pubKeyData);
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
            return false;
        }
        EVP_PKEY* pkey = secp256k1FromPublic(keyData_.data(), keyData_.size());
        if (!pkey) {
            throw std::runtime_error("Failed to import secp256k1 public key: " + getOpenSSLError());
        }
        bool valid = secp256k1Verify(pkey, message.data(), message.size(), signature.data());
        EVP_PKEY_free(pkey);
        return valid;
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
        
        // EVP_chacha20() читає 16 байт IV: 4 байти лічильника (0) і 12 байт nonce.
        // Коротший буфер змушує OpenSSL прочитати 4 байти за межею nonce.
        uint8_t iv[16] = {0, 0, 0, 0};
        std::memcpy(iv + 4, nonce.data(), 12);
        if (EVP_EncryptInit_ex(ctx, EVP_chacha20(), nullptr, key.data(), iv) != 1) {
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
        std::vector<uint8_t> keyData = privateKey.getData();
        EVP_PKEY* pkey = secp256k1FromPrivate(keyData.data(), keyData.size());
        if (!pkey) {
            throw std::runtime_error("Failed to import secp256k1 private key: " + getOpenSSLError());
        }
        std::vector<uint8_t> signature = secp256k1Sign(pkey, message.data(), message.size());
        EVP_PKEY_free(pkey);
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

}