// NativeInterface.h - C-стиловий інтерфейс для JNA
// Author: Андрій Будильников (Sparky)
// C-style interface for JNA bindings
// C-стильовой интерфейс для JNA биндингов

#ifndef CTON_SDK_CORE_NATIVE_INTERFACE_H
#define CTON_SDK_CORE_NATIVE_INTERFACE_H

#include "Cell.h"
#include "Address.h"
#include "Crypto.h"
#include "Boc.h"

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

// Cell functions
extern "C" {
    CTON_SDK_CORE_API void* cell_create();
    CTON_SDK_CORE_API void cell_destroy(void* cell);
    CTON_SDK_CORE_API bool cell_store_uint(void* cell, int bits, uint64_t value);
    CTON_SDK_CORE_API bool cell_store_int(void* cell, int bits, int64_t value);
    CTON_SDK_CORE_API bool cell_store_bytes(void* cell, const uint8_t* data, int length);
    CTON_SDK_CORE_API bool cell_store_ref(void* cell, void* refCell);
    CTON_SDK_CORE_API int cell_get_data(void* cell, uint8_t* buffer, int bufferSize);
    CTON_SDK_CORE_API int cell_get_bit_size(void* cell);
    CTON_SDK_CORE_API int cell_get_refs_count(void* cell);
    CTON_SDK_CORE_API void* cell_get_ref(void* cell, int index);

    // Address functions
    CTON_SDK_CORE_API void* address_create();
    CTON_SDK_CORE_API void* address_create_from_string(const char* addressStr);
    CTON_SDK_CORE_API void address_destroy(void* address);
    CTON_SDK_CORE_API int8_t address_get_workchain(void* address);
    CTON_SDK_CORE_API void address_get_hash_part(void* address, uint8_t* hashPart);
    CTON_SDK_CORE_API char* address_to_raw(void* address);
    CTON_SDK_CORE_API char* address_to_user_friendly(void* address, bool isBounceable, bool isTestOnly);
    CTON_SDK_CORE_API bool address_is_valid(void* address);
    CTON_SDK_CORE_API void address_set_workchain(void* address, int8_t workchain);
    CTON_SDK_CORE_API void address_set_hash_part(void* address, const uint8_t* hashPart);

    // Private key functions
    CTON_SDK_CORE_API void* private_key_create();
    CTON_SDK_CORE_API void* private_key_create_from_data(const uint8_t* keyData, int length);
    CTON_SDK_CORE_API void private_key_destroy(void* privateKey);
    CTON_SDK_CORE_API void* private_key_generate();
    CTON_SDK_CORE_API int private_key_get_data(void* privateKey, uint8_t* buffer, int bufferSize);
    CTON_SDK_CORE_API void* private_key_get_public_key(void* privateKey);

    // Public key functions
    CTON_SDK_CORE_API void* public_key_create();
    CTON_SDK_CORE_API void* public_key_create_from_data(const uint8_t* keyData, int length);
    CTON_SDK_CORE_API void public_key_destroy(void* publicKey);
    CTON_SDK_CORE_API int public_key_get_data(void* publicKey, uint8_t* buffer, int bufferSize);
    CTON_SDK_CORE_API bool public_key_verify_signature(void* publicKey, const uint8_t* message, int messageLen, 
                                    const uint8_t* signature, int signatureLen);

    // Secp256k1 private key functions
    CTON_SDK_CORE_API void* secp256k1_private_key_create();
    CTON_SDK_CORE_API void* secp256k1_private_key_create_from_data(const uint8_t* keyData, int length);
    CTON_SDK_CORE_API void secp256k1_private_key_destroy(void* privateKey);
    CTON_SDK_CORE_API void* secp256k1_private_key_generate();
    CTON_SDK_CORE_API int secp256k1_private_key_get_data(void* privateKey, uint8_t* buffer, int bufferSize);
    CTON_SDK_CORE_API void* secp256k1_private_key_get_public_key(void* privateKey);

    // Secp256k1 public key functions
    CTON_SDK_CORE_API void* secp256k1_public_key_create();
    CTON_SDK_CORE_API void* secp256k1_public_key_create_from_data(const uint8_t* keyData, int length);
    CTON_SDK_CORE_API void secp256k1_public_key_destroy(void* publicKey);
    CTON_SDK_CORE_API int secp256k1_public_key_get_data(void* publicKey, uint8_t* buffer, int bufferSize);
    CTON_SDK_CORE_API bool secp256k1_public_key_verify_signature(void* publicKey, const uint8_t* message, int messageLen, 
                                               const uint8_t* signature, int signatureLen);

    // Crypto functions
    CTON_SDK_CORE_API void* crypto_sign(void* privateKey, const uint8_t* message, int messageLen);
    CTON_SDK_CORE_API bool crypto_verify(void* publicKey, const uint8_t* message, int messageLen, 
                      const uint8_t* signature, int signatureLen);
    CTON_SDK_CORE_API void* crypto_sign_secp256k1(void* privateKey, const uint8_t* message, int messageLen);
    CTON_SDK_CORE_API bool crypto_verify_secp256k1(void* publicKey, const uint8_t* message, int messageLen, 
                         const uint8_t* signature, int signatureLen);
    CTON_SDK_CORE_API char** crypto_generate_mnemonic();
    CTON_SDK_CORE_API void* crypto_mnemonic_to_private_key(char** mnemonic);
    CTON_SDK_CORE_API void* crypto_mnemonic_to_private_key_ton(char** mnemonic, bool fast);
    CTON_SDK_CORE_API void* crypto_chacha20_encrypt(const uint8_t* data, int dataLen, const uint8_t* key, const uint8_t* nonce);

    // BOC functions
    CTON_SDK_CORE_API void* boc_create();
    CTON_SDK_CORE_API void* boc_create_with_root(void* rootCell);
    CTON_SDK_CORE_API void boc_destroy(void* boc);
    CTON_SDK_CORE_API void* boc_serialize(void* boc, bool hasIdx, bool hashCRC);
    CTON_SDK_CORE_API int boc_get_serialized_size(void* boc, bool hasIdx, bool hashCRC);  // New function
    CTON_SDK_CORE_API void* boc_deserialize(const uint8_t* data, int length);
    CTON_SDK_CORE_API void* boc_get_root(void* boc);
    CTON_SDK_CORE_API void boc_set_root(void* boc, void* rootCell);
    
    // Memory management functions
    CTON_SDK_CORE_API void free_string(char* str);
    CTON_SDK_CORE_API void free_string_array(char** strArray);
}

#endif // CTON_SDK_CORE_NATIVE_INTERFACE_H