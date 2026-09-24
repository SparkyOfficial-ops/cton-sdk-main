// NativeInterface.cpp - Реалізація C-стильного інтерфейсу для JNA
// Author: Андрій Будильников (Sparky)
// Implementation of C-style interface for JNA bindings
// Реализация C-стильного интерфейса для JNA биндингов

#include "../include/NativeInterface.h"
#include "../include/Cell.h"
#include "../include/Address.h"
#include "../include/Crypto.h"
#include "../include/Boc.h"
#include <cstring>
#include <cstdlib>
#include <stdexcept>

using namespace cton;

// Helper function to convert C++ string to C string
char* string_to_cstr(const std::string& str) {
    if (str.empty()) {
        return nullptr;
    }
    
    try {
        char* cstr = (char*)malloc(str.length() + 1);
        if (cstr) {
            std::strcpy(cstr, str.c_str());
        }
        return cstr;
    } catch (...) {
        return nullptr;
    }
}

// Function to free C string allocated by string_to_cstr
void free_string(char* str) {
    if (str) {
        free(str);
    }
}

// Function to free array of C strings
void free_string_array(char** strArray) {
    if (strArray) {
        for (int i = 0; strArray[i] != nullptr; ++i) {
            free_string(strArray[i]);
        }
        free(strArray);
    }
}

// Cell functions
void* cell_create() {
    try {
        return new Cell();
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void cell_destroy(void* cell) {
    if (cell) {
        try {
            delete static_cast<Cell*>(cell);
        } catch (...) {
            // Ignore exceptions during destruction
        }
    }
}

bool cell_store_uint(void* cell, int bits, uint64_t value) {
    if (!cell) {
        return false;
    }
    
    try {
        static_cast<Cell*>(cell)->storeUInt(bits, value);
        return true;
    } catch (const std::exception&) {
        // Handle standard exceptions
        return false;
    } catch (...) {
        // Handle any other unexpected exceptions
        return false;
    }
}

bool cell_store_int(void* cell, int bits, int64_t value) {
    if (!cell) {
        return false;
    }
    
    try {
        static_cast<Cell*>(cell)->storeInt(bits, value);
        return true;
    } catch (const std::exception&) {
        // Handle standard exceptions
        return false;
    } catch (...) {
        // Handle any other unexpected exceptions
        return false;
    }
}

bool cell_store_bytes(void* cell, const uint8_t* data, int length) {
    if (!cell || !data || length <= 0) {
        return false;
    }
    
    try {
        std::vector<uint8_t> vec(data, data + length);
        static_cast<Cell*>(cell)->storeBytes(vec);
        return true;
    } catch (const std::exception&) {
        // Handle standard exceptions
        return false;
    } catch (...) {
        // Handle any other unexpected exceptions
        return false;
    }
}

bool cell_store_ref(void* cell, void* refCell) {
    if (!cell || !refCell) {
        return false;
    }
    
    try {
        // Convert void* to shared_ptr<Cell>
        std::shared_ptr<Cell> ref(static_cast<Cell*>(refCell));
        static_cast<Cell*>(cell)->addReference(ref);
        return true;
    } catch (const std::exception&) {
        // Handle standard exceptions
        return false;
    } catch (...) {
        // Handle any other unexpected exceptions
        return false;
    }
}

int cell_get_data(void* cell, uint8_t* buffer, int bufferSize) {
    if (!cell || !buffer || bufferSize <= 0) {
        return -1;
    }
    
    try {
        auto data = static_cast<Cell*>(cell)->getData();
        int copySize = std::min(bufferSize, (int)data.size());
        std::memcpy(buffer, data.data(), copySize);
        return copySize;
    } catch (const std::exception&) {
        // Handle standard exceptions
        return -1;
    } catch (...) {
        // Handle any other unexpected exceptions
        return -1;
    }
}

int cell_get_bit_size(void* cell) {
    if (!cell) {
        return -1;
    }
    
    try {
        return static_cast<Cell*>(cell)->getBitSize();
    } catch (const std::exception&) {
        // Handle standard exceptions
        return -1;
    } catch (...) {
        // Handle any other unexpected exceptions
        return -1;
    }
}

int cell_get_refs_count(void* cell) {
    if (!cell) {
        return -1;
    }
    
    try {
        auto refs = static_cast<Cell*>(cell)->getReferences();
        return static_cast<int>(refs.size());
    } catch (const std::exception&) {
        // Handle standard exceptions
        return -1;
    } catch (...) {
        // Handle any other unexpected exceptions
        return -1;
    }
}

void* cell_get_ref(void* cell, int index) {
    if (!cell || index < 0) {
        return nullptr;
    }
    
    try {
        auto refs = static_cast<Cell*>(cell)->getReferences();
        if (index >= static_cast<int>(refs.size())) {
            return nullptr;
        }
        
        // Lock the weak_ptr to get the shared_ptr
        auto ref = refs[index].lock();
        if (!ref) {
            return nullptr;
        }
        
        // Return the raw pointer to the Cell object
        return ref.get();
    } catch (const std::exception&) {
        // Handle standard exceptions
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

// Address functions
void* address_create() {
    try {
        return new Address();
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void* address_create_from_string(const char* addressStr) {
    if (!addressStr) {
        return nullptr;
    }
    
    try {
        return new Address(std::string(addressStr));
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void address_destroy(void* address) {
    if (address) {
        try {
            delete static_cast<Address*>(address);
        } catch (...) {
            // Ignore exceptions during destruction
        }
    }
}

int8_t address_get_workchain(void* address) {
    if (!address) {
        return 0;
    }
    
    try {
        return static_cast<Address*>(address)->getWorkchain();
    } catch (const std::exception&) {
        // Handle standard exceptions
        return 0;
    } catch (...) {
        // Handle any other unexpected exceptions
        return 0;
    }
}

void address_get_hash_part(void* address, uint8_t* hashPart) {
    if (!address || !hashPart) {
        return;
    }
    
    try {
        auto hash = static_cast<Address*>(address)->getHashPart();
        std::memcpy(hashPart, hash.data(), std::min(hash.size(), (size_t)32));
    } catch (...) {
        // Нічого не робимо
        // Do nothing
        // Ничего не делаем
    }
}

char* address_to_raw(void* address) {
    if (!address) {
        return nullptr;
    }
    
    try {
        std::string raw = static_cast<Address*>(address)->toRaw();
        return string_to_cstr(raw);
    } catch (...) {
        return nullptr;
    }
}

char* address_to_user_friendly(void* address, bool isBounceable, bool isTestOnly) {
    if (!address) {
        return nullptr;
    }
    
    try {
        std::string uf = static_cast<Address*>(address)->toUserFriendly(isBounceable, isTestOnly);
        return string_to_cstr(uf);
    } catch (...) {
        return nullptr;
    }
}

bool address_is_valid(void* address) {
    if (!address) {
        return false;
    }
    
    try {
        return static_cast<Address*>(address)->isValid();
    } catch (...) {
        return false;
    }
}

void address_set_workchain(void* address, int8_t workchain) {
    if (!address) {
        return;
    }
    
    try {
        static_cast<Address*>(address)->setWorkchain(workchain);
    } catch (...) {
        // Нічого не робимо
        // Do nothing
        // Ничего не делаем
    }
}

void address_set_hash_part(void* address, const uint8_t* hashPart) {
    if (!address || !hashPart) {
        return;
    }
    
    try {
        std::vector<uint8_t> hash(hashPart, hashPart + 32);
        static_cast<Address*>(address)->setHashPart(hash);
    } catch (...) {
        // Нічого не робимо
        // Do nothing
        // Ничего не делаем
    }
}

// Private key functions
void* private_key_create() {
    try {
        return new PrivateKey();
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void* private_key_create_from_data(const uint8_t* keyData, int length) {
    if (!keyData || length != 32) {
        return nullptr;
    }
    
    try {
        std::vector<uint8_t> data(keyData, keyData + length);
        return new PrivateKey(data);
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void private_key_destroy(void* privateKey) {
    if (privateKey) {
        try {
            delete static_cast<PrivateKey*>(privateKey);
        } catch (...) {
            // Ignore exceptions during destruction
        }
    }
}

void* private_key_generate() {
    try {
        PrivateKey key = PrivateKey::generate();
        // Create a new PrivateKey object and return it
        return new PrivateKey(key);
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

int private_key_get_data(void* privateKey, uint8_t* buffer, int bufferSize) {
    if (!privateKey || !buffer || bufferSize < 32) {
        return -1;
    }
    
    try {
        auto data = static_cast<PrivateKey*>(privateKey)->getData();
        std::memcpy(buffer, data.data(), 32);
        return 32;
    } catch (const std::exception&) {
        // Handle standard exceptions
        return -1;
    } catch (...) {
        // Handle any other unexpected exceptions
        return -1;
    }
}

void* private_key_get_public_key(void* privateKey) {
    if (!privateKey) {
        return nullptr;
    }
    
    try {
        PublicKey publicKey = static_cast<PrivateKey*>(privateKey)->getPublicKey();
        // Create a new PublicKey object and return it
        return new PublicKey(publicKey);
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

// Public key functions
void* public_key_create() {
    try {
        return new PublicKey();
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void* public_key_create_from_data(const uint8_t* keyData, int length) {
    if (!keyData || length != 32) {
        return nullptr;
    }
    
    try {
        std::vector<uint8_t> data(keyData, keyData + length);
        return new PublicKey(data);
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void public_key_destroy(void* publicKey) {
    if (publicKey) {
        try {
            delete static_cast<PublicKey*>(publicKey);
        } catch (...) {
            // Ignore exceptions during destruction
        }
    }
}

int public_key_get_data(void* publicKey, uint8_t* buffer, int bufferSize) {
    if (!publicKey || !buffer || bufferSize < 32) {
        return -1;
    }
    
    try {
        auto data = static_cast<PublicKey*>(publicKey)->getData();
        std::memcpy(buffer, data.data(), 32);
        return 32;
    } catch (const std::exception&) {
        // Handle standard exceptions
        return -1;
    } catch (...) {
        // Handle any other unexpected exceptions
        return -1;
    }
}

bool public_key_verify_signature(void* publicKey, const uint8_t* message, int messageLen, 
                                const uint8_t* signature, int signatureLen) {
    if (!publicKey || !message || messageLen <= 0 || !signature || signatureLen != 64) {
        return false;
    }
    
    try {
        std::vector<uint8_t> msg(message, message + messageLen);
        std::vector<uint8_t> sig(signature, signature + signatureLen);
        return static_cast<PublicKey*>(publicKey)->verifySignature(msg, sig);
    } catch (const std::exception&) {
        // Handle standard exceptions
        return false;
    } catch (...) {
        // Handle any other unexpected exceptions
        return false;
    }
}

// Secp256k1 private key functions
void* secp256k1_private_key_create() {
    try {
        return new Secp256k1PrivateKey();
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void* secp256k1_private_key_create_from_data(const uint8_t* keyData, int length) {
    if (!keyData || length != 32) {
        return nullptr;
    }
    
    try {
        std::vector<uint8_t> data(keyData, keyData + length);
        return new Secp256k1PrivateKey(data);
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void secp256k1_private_key_destroy(void* privateKey) {
    if (privateKey) {
        try {
            delete static_cast<Secp256k1PrivateKey*>(privateKey);
        } catch (...) {
            // Ignore exceptions during destruction
        }
    }
}

void* secp256k1_private_key_generate() {
    try {
        Secp256k1PrivateKey key = Secp256k1PrivateKey::generate();
        // Create a new Secp256k1PrivateKey object and return it
        return new Secp256k1PrivateKey(key);
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

int secp256k1_private_key_get_data(void* privateKey, uint8_t* buffer, int bufferSize) {
    if (!privateKey || !buffer || bufferSize < 32) {
        return -1;
    }
    
    try {
        auto data = static_cast<Secp256k1PrivateKey*>(privateKey)->getData();
        std::memcpy(buffer, data.data(), 32);
        return 32;
    } catch (const std::exception&) {
        // Handle standard exceptions
        return -1;
    } catch (...) {
        // Handle any other unexpected exceptions
        return -1;
    }
}

void* secp256k1_private_key_get_public_key(void* privateKey) {
    if (!privateKey) {
        return nullptr;
    }
    
    try {
        Secp256k1PublicKey publicKey = static_cast<Secp256k1PrivateKey*>(privateKey)->getPublicKey();
        // Create a new Secp256k1PublicKey object and return it
        return new Secp256k1PublicKey(publicKey);
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

// Secp256k1 public key functions
void* secp256k1_public_key_create() {
    try {
        return new Secp256k1PublicKey();
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void* secp256k1_public_key_create_from_data(const uint8_t* keyData, int length) {
    if (!keyData || (length != 33 && length != 65)) {
        return nullptr;
    }
    
    try {
        std::vector<uint8_t> data(keyData, keyData + length);
        return new Secp256k1PublicKey(data);
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void secp256k1_public_key_destroy(void* publicKey) {
    if (publicKey) {
        try {
            delete static_cast<Secp256k1PublicKey*>(publicKey);
        } catch (...) {
            // Ignore exceptions during destruction
        }
    }
}

int secp256k1_public_key_get_data(void* publicKey, uint8_t* buffer, int bufferSize) {
    if (!publicKey || !buffer || bufferSize < 33) {
        return -1;
    }
    
    try {
        auto data = static_cast<Secp256k1PublicKey*>(publicKey)->getData();
        int copySize = std::min(bufferSize, (int)data.size());
        std::memcpy(buffer, data.data(), copySize);
        return copySize;
    } catch (const std::exception&) {
        // Handle standard exceptions
        return -1;
    } catch (...) {
        // Handle any other unexpected exceptions
        return -1;
    }
}

bool secp256k1_public_key_verify_signature(void* publicKey, const uint8_t* message, int messageLen, 
                                          const uint8_t* signature, int signatureLen) {
    if (!publicKey || !message || messageLen <= 0 || !signature || (signatureLen != 64 && signatureLen != 65)) {
        return false;
    }
    
    try {
        std::vector<uint8_t> msg(message, message + messageLen);
        std::vector<uint8_t> sig(signature, signature + signatureLen);
        return static_cast<Secp256k1PublicKey*>(publicKey)->verifySignature(msg, sig);
    } catch (const std::exception&) {
        // Handle standard exceptions
        return false;
    } catch (...) {
        // Handle any other unexpected exceptions
        return false;
    }
}

// Crypto functions
void* crypto_sign(void* privateKey, const uint8_t* message, int messageLen) {
    if (!privateKey || !message || messageLen <= 0) {
        return nullptr;
    }
    
    try {
        std::vector<uint8_t> msg(message, message + messageLen);
        std::vector<uint8_t> signature = Crypto::sign(*static_cast<PrivateKey*>(privateKey), msg);
        
        // Allocate memory for the signature and copy it
        uint8_t* result = (uint8_t*)malloc(signature.size());
        if (result) {
            std::memcpy(result, signature.data(), signature.size());
        }
        return result;
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        return nullptr;
    }
}

bool crypto_verify(void* publicKey, const uint8_t* message, int messageLen, 
                  const uint8_t* signature, int signatureLen) {
    if (!publicKey || !message || messageLen <= 0 || !signature || signatureLen != 64) {
        return false;
    }
    
    try {
        std::vector<uint8_t> msg(message, message + messageLen);
        std::vector<uint8_t> sig(signature, signature + signatureLen);
        return Crypto::verify(*static_cast<PublicKey*>(publicKey), msg, sig);
    } catch (...) {
        return false;
    }
}

void* crypto_sign_secp256k1(void* privateKey, const uint8_t* message, int messageLen) {
    if (!privateKey || !message || messageLen <= 0) {
        return nullptr;
    }
    
    try {
        std::vector<uint8_t> msg(message, message + messageLen);
        std::vector<uint8_t> signature = Crypto::signSecp256k1(*static_cast<Secp256k1PrivateKey*>(privateKey), msg);
        
        // Allocate memory for the signature and copy it
        uint8_t* result = (uint8_t*)malloc(signature.size());
        if (result) {
            std::memcpy(result, signature.data(), signature.size());
        }
        return result;
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        return nullptr;
    }
}

bool crypto_verify_secp256k1(void* publicKey, const uint8_t* message, int messageLen, 
                            const uint8_t* signature, int signatureLen) {
    if (!publicKey || !message || messageLen <= 0 || !signature || (signatureLen != 64 && signatureLen != 65)) {
        return false;
    }
    
    try {
        std::vector<uint8_t> msg(message, message + messageLen);
        std::vector<uint8_t> sig(signature, signature + signatureLen);
        return Crypto::verifySecp256k1(*static_cast<Secp256k1PublicKey*>(publicKey), msg, sig);
    } catch (...) {
        return false;
    }
}

char** crypto_generate_mnemonic() {
    try {
        auto words = Crypto::generateMnemonic();
        
        // Allocate memory for the array of C-strings
        char** result = (char**)malloc((words.size() + 1) * sizeof(char*));
        if (!result) {
            return nullptr;
        }
        
        // Convert each word to a C-string
        for (size_t i = 0; i < words.size(); ++i) {
            result[i] = string_to_cstr(words[i]);
            if (!result[i]) {
                // If allocation fails, clean up and return nullptr
                for (size_t j = 0; j < i; ++j) {
                    free_string(result[j]);
                }
                free_string_array(result);
                return nullptr;
            }
        }
        
        // Null-terminate the array
        result[words.size()] = nullptr;
        return result;
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        return nullptr;
    }
}

void* crypto_mnemonic_to_private_key(char** mnemonic) {
    if (!mnemonic) {
        return nullptr;
    }
    
    try {
        // Convert array of C-strings to vector of strings
        std::vector<std::string> words;
        for (int i = 0; mnemonic[i] != nullptr; ++i) {
            words.push_back(std::string(mnemonic[i]));
        }
        
        PrivateKey key = Crypto::mnemonicToPrivateKey(words);
        // Create a new PrivateKey object and return it
        return new PrivateKey(key);
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void* crypto_mnemonic_to_private_key_ton(char** mnemonic, bool fast) {
    if (!mnemonic) {
        return nullptr;
    }
    
    try {
        // Convert array of C-strings to vector of strings
        std::vector<std::string> words;
        for (int i = 0; mnemonic[i] != nullptr; ++i) {
            words.push_back(std::string(mnemonic[i]));
        }
        
        // Derive using the TON-native mnemonic standard (toTonSeed)
        PrivateKey key = Crypto::mnemonicToPrivateKeyTon(words, fast);
        return new PrivateKey(key);
    } catch (const std::bad_alloc&) {
        return nullptr;
    } catch (...) {
        return nullptr;
    }
}

// BOC functions
void* boc_create() {
    try {
        return new Boc();
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void* boc_create_with_root(void* rootCell) {
    if (!rootCell) {
        return nullptr;
    }
    
    try {
        // Convert void* to shared_ptr<Cell>
        std::shared_ptr<Cell> cell(static_cast<Cell*>(rootCell));
        // Create a new Boc object with the root cell
        return new Boc(cell);
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void boc_destroy(void* boc) {
    if (boc) {
        try {
            delete static_cast<Boc*>(boc);
        } catch (...) {
            // Ignore exceptions during destruction
        }
    }
}

void* boc_serialize(void* boc, bool hasIdx, bool hashCRC) {
    if (!boc) {
        return nullptr;
    }
    
    try {
        std::vector<uint8_t> data = static_cast<Boc*>(boc)->serialize(hasIdx, hashCRC);
        
        // Allocate memory for the serialized data and copy it
        uint8_t* result = (uint8_t*)malloc(data.size());
        if (result) {
            std::memcpy(result, data.data(), data.size());
        }
        return result;
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        return nullptr;
    }
}

int boc_get_serialized_size(void* boc, bool hasIdx, bool hashCRC) {
    if (!boc) {
        return -1;
    }
    
    try {
        std::vector<uint8_t> data = static_cast<Boc*>(boc)->serialize(hasIdx, hashCRC);
        return static_cast<int>(data.size());
    } catch (const std::exception&) {
        // Handle standard exceptions
        return -1;
    } catch (...) {
        // Handle any other unexpected exceptions
        return -1;
    }
}

void* boc_deserialize(const uint8_t* data, int length) {
    if (!data || length <= 0) {
        return nullptr;
    }
    
    try {
        std::vector<uint8_t> vec(data, data + length);
        Boc boc = Boc::deserialize(vec);
        // Create a new Boc object and return it
        return new Boc(boc);
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void* boc_get_root(void* boc) {
    if (!boc) {
        return nullptr;
    }
    
    try {
        std::shared_ptr<Cell> root = static_cast<Boc*>(boc)->getRoot();
        // Create a new Cell object that's a copy of the root cell
        // This ensures proper memory management
        if (root) {
            std::vector<uint8_t> data = root->getData();
            size_t bitSize = root->getBitSize();
            
            // Create a new cell with the same data
            Cell* newCell = new Cell(data, bitSize, std::vector<std::shared_ptr<Cell>>(), root->isSpecial());
            return newCell;
        }
        return nullptr;
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        // Handle any other unexpected exceptions
        return nullptr;
    }
}

void boc_set_root(void* boc, void* rootCell) {
    if (!boc || !rootCell) {
        return;
    }
    
    try {
        // Convert void* to shared_ptr<Cell>
        std::shared_ptr<Cell> cell(static_cast<Cell*>(rootCell));
        static_cast<Boc*>(boc)->setRoot(cell);
    } catch (...) {
        // Нічого не робимо
        // Do nothing
        // Ничего не делаем
    }
}

// ChaCha20 encryption function
void* crypto_chacha20_encrypt(const uint8_t* data, int dataLen, const uint8_t* key, const uint8_t* nonce) {
    if (!data || dataLen <= 0 || !key || !nonce) {
        return nullptr;
    }
    
    // For ChaCha20, we expect exactly 32 bytes for key and 12 bytes for nonce
    // The caller must ensure these sizes are correct
    
    try {
        // Convert to vectors - using the exact sizes expected by ChaCha20
        // We need to be careful here to not read past the end of the arrays
        std::vector<uint8_t> dataVec(data, data + dataLen);
        
        // Create key vector safely - we know we need exactly 32 bytes
        std::vector<uint8_t> keyVec(32);
        std::memcpy(keyVec.data(), key, 32);
        
        // Create nonce vector safely - we know we need exactly 12 bytes
        std::vector<uint8_t> nonceVec(12);
        std::memcpy(nonceVec.data(), nonce, 12);
        
        // Validate that we have the correct sizes
        if (dataVec.size() != (size_t)dataLen) {
            return nullptr;
        }
        
        if (keyVec.size() != 32) {
            return nullptr;
        }
        
        if (nonceVec.size() != 12) {
            return nullptr;
        }
        
        // Debug output
        // std::cout << "crypto_chacha20_encrypt: dataLen=" << dataLen << std::endl;
        // std::cout << "crypto_chacha20_encrypt: key[0..4]=" << (int)keyVec[0] << " " << (int)keyVec[1] << " " << (int)keyVec[2] << " " << (int)keyVec[3] << std::endl;
        // std::cout << "crypto_chacha20_encrypt: nonce[0..4]=" << (int)nonceVec[0] << " " << (int)nonceVec[1] << " " << (int)nonceVec[2] << " " << (int)nonceVec[3] << std::endl;
        
        // Encrypt using ChaCha20
        auto encrypted = cton::ChaCha20::encrypt(dataVec, keyVec, nonceVec);
        
        // Validate that encryption worked
        if (encrypted.size() != dataVec.size()) {
            return nullptr;
        }
        
        // Additional validation - make sure we have data
        if (encrypted.empty() && !dataVec.empty()) {
            return nullptr;
        }
        
        // Debug output
        // std::cout << "crypto_chacha20_encrypt: encrypted.size()=" << encrypted.size() << std::endl;
        // std::cout << "crypto_chacha20_encrypt: encrypted[0..4]=" << (int)encrypted[0] << " " << (int)encrypted[1] << " " << (int)encrypted[2] << " " << (int)encrypted[3] << std::endl;
        
        // Allocate memory for the result and copy it
        uint8_t* result = (uint8_t*)malloc(encrypted.size());
        if (result && encrypted.size() > 0) {
            std::memcpy(result, encrypted.data(), encrypted.size());
        } else if (result) {
            // Handle zero-size allocation
            // result is already allocated but empty
        } else {
            // malloc failed
            return nullptr;
        }
        return result;
    } catch (const std::bad_alloc&) {
        // Handle memory allocation failure
        return nullptr;
    } catch (...) {
        return nullptr;
    }
}


