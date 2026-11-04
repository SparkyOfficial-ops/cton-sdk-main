// Boc.h - робота з Bag of Cells (BOC)
// Author: Андрій Будильников (Sparky)
// Bag of Cells - основний формат серіалізації даних в TON

#ifndef CTON_BOC_H
#define CTON_BOC_H

#include "Cell.h"
#include <vector>
#include <memory>
#include <cstdint>
#include <unordered_set>

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
    
    // Forward declarations
    class CTON_SDK_CORE_API BocParser;
    class CTON_SDK_CORE_API BocBuilder;
    
    /**
     * @brief Представляє серіалізований Bag of Cells
     * 
     * BOC (Bag of Cells) - це формат серіалізації дерева комірок (Cell)
     * в бінарне представлення для передачі по мережі або зберігання
     */
    class CTON_SDK_CORE_API Boc {
    public:
        /**
         * @brief Конструктор за замовчуванням
         */
        Boc();
        
        /**
         * @brief Конструктор з кореневої комірки
         * @param root коренева комірка
         */
        Boc(std::shared_ptr<Cell> root);
        
        /**
         * @brief Серіалізувати BOC в бінарне представлення
         * @param hasIdx чи включати індекс
         * @param hashCRC чи включати CRC хеш
         * @return бінарне представлення BOC
         */
        std::vector<uint8_t> serialize(bool hasIdx = true, bool hashCRC = true) const;
        
        /**
         * @brief Десеріалізувати BOC з бінарного представлення
         * @param data бінарні дані BOC
         * @return об'єкт Boc
         */
        static Boc deserialize(const std::vector<uint8_t>& data);
        
        /**
         * @brief Отримати кореневу комірку
         * @return коренева комірка
         */
        std::shared_ptr<Cell> getRoot() const;
        
        /**
         * @brief Встановити кореневу комірку
         * @param root нова коренева комірка
         */
        void setRoot(std::shared_ptr<Cell> root);
        
    private:
        std::shared_ptr<Cell> root_;
        
        /**
         * @brief Обчислити CRC32
         * @param data дані для обчислення CRC
         * @return значення CRC32
         */
        static uint32_t calculateCRC32(const std::vector<uint8_t>& data);
        
        /**
         * @brief Рекурсивно зібрати всі комірки в дереві
         * @param cell поточна комірка
         * @param cells вектор для збору комірок
         * @param visited множина відвіданих комірок
         */
        void collectCells(std::shared_ptr<Cell> cell, 
                         std::vector<std::shared_ptr<Cell>>& cells,
                         std::unordered_set<std::shared_ptr<Cell>>& visited) const;
    };
    
    /**
     * @brief Парсер для десеріалізації BOC
     */
    class CTON_SDK_CORE_API BocParser {
    public:
        /**
         * @brief Конструктор
         * @param data бінарні дані для парсингу
         */
        BocParser(const std::vector<uint8_t>& data);
        
        /**
         * @brief Спарсити BOC
         * @return об'єкт Boc
         */
        Boc parse();
        
    private:
        std::vector<uint8_t> data_;
        size_t offset_;
        
        /**
         * @brief Прочитати байт
         * @return прочитаний байт
         */
        uint8_t readByte();
        
        /**
         * @brief Прочитати 32-бітне беззнакове ціле
         * @return значення
         */
        uint32_t readUint32();
        
        /**
         * @brief Прочитати вектор байтів
         * @param size кількість байтів
         * @return вектор байтів
         */
        std::vector<uint8_t> readBytes(size_t size);
    };
    
    /**
     * @brief Будівельник для серіалізації BOC
     */
    class CTON_SDK_CORE_API BocBuilder {
    public:
        /**
         * @brief Конструктор
         * @param root коренева комірка
         */
        BocBuilder(std::shared_ptr<Cell> root);
        
        /**
         * @brief Побудувати BOC
         * @param hasIdx чи включати індекс
         * @param hashCRC чи включати CRC хеш
         * @return бінарне представлення BOC
         */
        std::vector<uint8_t> build(bool hasIdx = true, bool hashCRC = true);
        
    private:
        std::shared_ptr<Cell> root_;
    };
}

#endif // CTON_BOC_H