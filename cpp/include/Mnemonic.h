// Mnemonic.h - BIP-39 мнемонічна фраза
// Author: Андрій Будильников (Sparky)
// BIP-39 mnemonic phrase implementation
// Реализация мнемонической фразы BIP-39

#ifndef CTON_MNEMONIC_H
#define CTON_MNEMONIC_H

#include <vector>
#include <string>
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
    
    class CTON_SDK_CORE_API Mnemonic {
    public:
        /**
         * Генерація мнемонічної фрази BIP-39
         * @param wordCount кількість слів (12, 18, або 24)
         * @return вектор слів мнемонічної фрази
         */
        static std::vector<std::string> generate(int wordCount = 24);

        /**
         * BIP-39 фраза, которая проходит проверку TON (первый байт PBKDF2-сида равен 1).
         */
        static std::vector<std::string> generateTon(int wordCount = 24);
        
        /**
         * Перетворення мнемоніки в seed
         * @param mnemonic мнемонічна фраза
         * @param passphrase опціональна парольна фраза
         * @return seed (64 байти)
         */
        static std::vector<uint8_t> toSeed(const std::vector<std::string>& mnemonic, 
                                          const std::string& passphrase = "");
        
        /**
         * Перетворення мнемоніки в seed за алгоритмом TON (Tonkeeper / MyTonWallet / Telegram Wallet)
         * TON-native seed derivation (PBKDF2-HMAC-SHA512 зі сіртами "TON default seed" / "TON quick seed")
         * @param mnemonic мнемонічна фраза
         * @param fast використовувати швидкий режим (1000 ітерацій) замість стандартного (100000)
         * @param passphrase опціональна парольна фраза
         * @return seed (64 байти)
         */
        static std::vector<uint8_t> toTonSeed(const std::vector<std::string>& mnemonic,
                                             bool fast = false,
                                             const std::string& passphrase = "");
        
        /**
         * Перевірка валідності мнемонічної фрази саме за стандартом TON
         * (HMAC/PBKDF2 перевірка першого байта сіду, сумісна з Tonkeeper/MyTonWallet)
         * @param mnemonic мнемонічна фраза
         * @param passphrase парольна фраза (порожня для звичайної перевірки)
         * @return true якщо фраза валідна як TON мнемоніка
         */
        static bool isTonMnemonic(const std::vector<std::string>& mnemonic,
                                  const std::string& passphrase = "");
        
        /**
         * Перевірка валідності мнемонічної фрази
         * @param mnemonic мнемонічна фраза
         * @return true якщо фраза валідна
         */
        static bool isValid(const std::vector<std::string>& mnemonic);
        
        /**
         * Отримати стандартний wordlist BIP-39 (англійська)
         * @return вектор слів
         */
        static const std::vector<std::string>& getWordlist();
        
    private:
        // Приватний конструктор для статичного класу
        Mnemonic() = default;
        
        /**
         * Обчислення контрольної суми для мнемоніки
         * @param entropy ентропія
         * @return контрольна сума
         */
        static std::vector<uint8_t> calculateChecksum(const std::vector<uint8_t>& entropy);
    };
    
}

#endif // CTON_MNEMONIC_H