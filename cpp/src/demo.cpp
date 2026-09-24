// demo.cpp - демонстрація всіх можливостей CTON-SDK
// Author: Андрій Будильников (Sparky)
// Demonstration of all CTON-SDK capabilities
// Демонстрация всех возможностей CTON-SDK

#include "../include/Cell.h"
#include "../include/Boc.h"
#include "../include/Address.h"
#include "../include/Crypto.h"
#include "../include/Mnemonic.h"
#include <iostream>
#include <memory>
#include <vector>

using namespace cton;

int main() {
    std::cout << "CTON-SDK Comprehensive Demonstration" << std::endl;
    std::cout << "=================================" << std::endl;
    
    try {
        // 1. Демонстрація роботи з комірками (Cells)
        // 1. Cell operations demonstration
        // 1. Демонстрация работы с ячейками
        std::cout << "\n1. Cell Operations:" << std::endl;
        CellBuilder builder;
        builder.storeUInt(32, 0x12345678);
        builder.storeBytes({0x01, 0x02, 0x03, 0x04, 0x05});
        builder.storeInt(16, -100);
        
        // Створюємо вкладену комірку
        // Create nested cell
        // Создаем вложенную ячейку
        CellBuilder nestedBuilder;
        nestedBuilder.storeUInt(64, 0xABCDEF0012345678);
        builder.storeRef(nestedBuilder.build());
        
        auto cell = builder.build();
        std::cout << "   Created cell with " << cell->getBitSize() << " bits" << std::endl;
        
        // 2. Демонстрація криптографічних функцій
        // 2. Cryptographic functions demonstration
        // 2. Демонстрация криптографических функций
        std::cout << "\n2. Cryptographic Operations:" << std::endl;
        auto privateKey = PrivateKey::generate();
        auto publicKey = privateKey.getPublicKey();
        std::cout << "   Generated key pair" << std::endl;
        
        std::vector<uint8_t> message = {0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x54, 0x4F, 0x4E, 0x21}; // "Hello TON!"
        auto signature = Crypto::sign(privateKey, message);
        bool verified = Crypto::verify(publicKey, message, signature);
        std::cout << "   Message signed and verified: " << (verified ? "SUCCESS" : "FAILED") << std::endl;
        
        // 3. Демонстрація мнемонічних фраз
        // 3. Mnemonic phrase demonstration
        // 3. Демонстрация мнемонических фраз
        std::cout << "\n3. Mnemonic Operations:" << std::endl;
        auto mnemonic = Mnemonic::generate(12); // Generate 12-word mnemonic for demo
        std::cout << "   Generated 12-word mnemonic phrase" << std::endl;
        std::cout << "   First 3 words: ";
        for (int i = 0; i < 3 && i < mnemonic.size(); ++i) {
            std::cout << mnemonic[i] << " ";
        }
        std::cout << std::endl;
        
        bool isValid = Mnemonic::isValid(mnemonic);
        std::cout << "   Mnemonic validation: " << (isValid ? "VALID" : "INVALID") << std::endl;
        
        // 4. Демонстрація роботи з адресами
        // 4. Address operations demonstration
        // 4. Демонстрация работы с адресами
        std::cout << "\n4. Address Operations:" << std::endl;
        // Create an address with workchain 0 and some hash part
        std::vector<uint8_t> hashPart(32, 0x12);
        Address address(0, hashPart);
        std::cout << "   Created address with workchain 0" << std::endl;
        std::cout << "   Raw address: " << address.toRaw() << std::endl;
        std::cout << "   User-friendly address: " << address.toUserFriendly(true, false) << std::endl;
        
        // 5. Демонстрація BOC (Bag of Cells)
        // 5. BOC (Bag of Cells) demonstration
        // 5. Демонстрация BOC (Bag of Cells)
        std::cout << "\n5. BOC Operations:" << std::endl;
        auto root = std::make_shared<Cell>(*cell);
        Boc boc(root);
        auto serialized = boc.serialize(true, true);
        std::cout << "   Serialized BOC to " << serialized.size() << " bytes" << std::endl;
        
        auto deserialized = Boc::deserialize(serialized);
        auto deserializedRoot = deserialized.getRoot();
        if (deserializedRoot) {
            std::cout << "   Deserialized BOC successfully" << std::endl;
        }
        
        std::cout << "\nAll demonstrations completed successfully!" << std::endl;
        return 0;
        
    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << std::endl;
        return 1;
    } catch (...) {
        std::cerr << "Unknown error occurred" << std::endl;
        return 1;
    }
}