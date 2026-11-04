// Cell.cpp - реалізація комірок для TON
// Author: Андрій Будильников (Sparky)

#include "../include/Cell.h"
#include <stdexcept>
#include <cstring>
#include <sstream>
#include <unordered_map>
#include <algorithm>
#include <functional>

namespace cton {
    
    Cell::Cell() : bitSize_(0), isSpecial_(false) {}
    
    Cell::Cell(const std::vector<uint8_t>& data, 
               size_t bitSize, 
               const std::vector<std::shared_ptr<Cell>>& references,
               bool isSpecial)
        : data_(data), bitSize_(bitSize), references_(references), isSpecial_(isSpecial) {
        // Validate parameters
        if (bitSize > MAX_BITS) {
            throw std::invalid_argument("Bit size exceeds maximum allowed");
        }
        
        if (references.size() > MAX_REFS) {
            throw std::invalid_argument("Number of references exceeds maximum allowed");
        }
        
        // Validate that data size is consistent with bit size
        size_t expectedByteSize = (bitSize + 7) / 8;
        if (data_.size() < expectedByteSize) {
            throw std::invalid_argument("Data size is smaller than expected for given bit size");
        }
    }
    
    // Допоміжна функція запису бітів у буфер у форматі TON (big-endian, MSB-first)
    // Helper that appends bits to a buffer, most-significant bit first, as required by TON cells.
    // Добавляет биты в буфер (старший бит сначала), как требует стандарт TON.
    static void appendBits(std::vector<uint8_t>& buffer, size_t& bitSize, size_t bits, uint64_t value) {
        if (bits == 0) {
            return;
        }
        if (bits > 64) {
            throw std::invalid_argument("Cannot store more than 64 bits in a single value");
        }
        if (bitSize + bits > Cell::MAX_BITS) {
            throw std::overflow_error("Not enough space in cell");
        }

        size_t requiredBytes = (bitSize + bits + 7) / 8;
        if (buffer.size() < requiredBytes) {
            buffer.resize(requiredBytes, 0);
        }

        for (size_t i = 0; i < bits; ++i) {
            uint8_t bit = static_cast<uint8_t>((value >> (bits - 1 - i)) & 1ULL);
            size_t pos = bitSize + i;
            size_t byteIndex = pos / 8;
            size_t bitIndex = pos % 8; // 0 == most significant bit
            uint8_t mask = static_cast<uint8_t>(0x80 >> bitIndex);
            if (bit) {
                buffer[byteIndex] |= mask;
            } else {
                buffer[byteIndex] = static_cast<uint8_t>(buffer[byteIndex] & ~mask);
            }
        }

        bitSize += bits;
    }

    void Cell::storeUInt(size_t bits, uint64_t value) {
        // Дописуємо біти в кінець комірки (апенд, а не перезапис)
        appendBits(data_, bitSize_, bits, value);
    }

    void Cell::storeInt(size_t bits, int64_t value) {
        // Two's complement: зберігаємо молодші `bits` бітів значення
        uint64_t unsignedValue = static_cast<uint64_t>(value);
        if (bits < 64) {
            unsignedValue &= ((1ULL << bits) - 1ULL);
        }
        appendBits(data_, bitSize_, bits, unsignedValue);
    }

    void Cell::storeBytes(const std::vector<uint8_t>& bytes) {
        for (uint8_t b : bytes) {
            appendBits(data_, bitSize_, 8, b);
        }
    }
    
    void Cell::addReference(std::shared_ptr<Cell> cell) {
        if (!cell) {
            throw std::invalid_argument("Cannot add null reference");
        }
        
        if (references_.size() >= MAX_REFS) {
            throw std::overflow_error("Maximum number of references reached");
        }
        
        references_.push_back(cell);
    }
    
    std::vector<uint8_t> Cell::getData() const {
        return data_;
    }
    
    size_t Cell::getBitSize() const {
        return bitSize_;
    }
    
    std::vector<std::weak_ptr<Cell>> Cell::getReferences() const {
        // Convert shared_ptr to weak_ptr
        std::vector<std::weak_ptr<Cell>> weakRefs;
        weakRefs.reserve(references_.size());
        
        for (const auto& ref : references_) {
            weakRefs.push_back(ref);
        }
        
        return weakRefs;
    }
    
    size_t Cell::getRefsCount() const {
        return references_.size();
    }
    
    bool Cell::isSpecial() const {
        return isSpecial_;
    }

    const std::vector<std::shared_ptr<Cell>>& Cell::references() const {
        return references_;
    }

    std::shared_ptr<Cell> Cell::cloneTree(const Cell* cell) {
        if (!cell) {
            throw std::invalid_argument("Cannot clone a null cell");
        }
        std::unordered_map<const Cell*, std::shared_ptr<Cell>> seen;
        std::function<std::shared_ptr<Cell>(const Cell*)> copy =
            [&](const Cell* node) -> std::shared_ptr<Cell> {
                auto found = seen.find(node);
                if (found != seen.end()) {
                    return found->second;
                }
                auto shell = std::shared_ptr<Cell>(new Cell());
                seen.emplace(node, shell);
                std::vector<std::shared_ptr<Cell>> refs;
                refs.reserve(node->references_.size());
                for (const auto& ref : node->references_) {
                    if (!ref) {
                        throw std::invalid_argument("Cell tree contains a null reference");
                    }
                    refs.push_back(copy(ref.get()));
                }
                shell->data_ = node->data_;
                shell->bitSize_ = node->bitSize_;
                shell->references_ = std::move(refs);
                shell->isSpecial_ = node->isSpecial_;
                return shell;
            };
        return copy(cell);
    }

    Cell* Cell::releaseCopy(const std::shared_ptr<Cell>& cell) {
        if (!cell) {
            return nullptr;
        }
        return new Cell(cell->data_, cell->bitSize_, cell->references_, cell->isSpecial_);
    }
    
    void Cell::checkCapacity(size_t bitCount) {
        if (bitSize_ + bitCount > MAX_BITS) {
            throw std::overflow_error("Not enough space in cell");
        }
    }
    
    CellBuilder::CellBuilder() : bitOffset_(0) {}
    
    CellBuilder& CellBuilder::storeUInt(size_t bits, uint64_t value) {
        // Запис у форматі TON (big-endian, MSB-first) через спільний апенд-хелпер
        appendBits(buffer_, bitOffset_, bits, value);
        return *this;
    }
    
    CellBuilder& CellBuilder::storeInt(size_t bits, int64_t value) {
        // Two's complement: зберігаємо молодші `bits` бітів значення
        uint64_t unsignedValue = static_cast<uint64_t>(value);
        if (bits < 64) {
            unsignedValue &= ((1ULL << bits) - 1ULL);
        }
        appendBits(buffer_, bitOffset_, bits, unsignedValue);
        return *this;
    }
    
    CellBuilder& CellBuilder::storeBytes(const std::vector<uint8_t>& data) {
        for (uint8_t b : data) {
            appendBits(buffer_, bitOffset_, 8, b);
        }
        return *this;
    }
    
    CellBuilder& CellBuilder::storeRef(std::shared_ptr<Cell> cell) {
        if (!cell) {
            throw std::invalid_argument("Cannot store null cell reference");
        }
        
        if (references_.size() >= Cell::MAX_REFS) {
            throw std::overflow_error("Maximum number of references reached");
        }
        
        references_.push_back(cell);
        return *this;
    }

    CellBuilder& CellBuilder::appendCell(const Cell& cell) {
        const std::vector<uint8_t> data = cell.getData();
        const size_t bitCount = cell.getBitSize();
        size_t pos = 0;
        while (pos < bitCount) {
            size_t chunk = std::min(static_cast<size_t>(64), bitCount - pos);
            uint64_t value = 0;
            for (size_t i = 0; i < chunk; ++i) {
                size_t bitPos = pos + i;
                uint8_t bit = static_cast<uint8_t>((data[bitPos / 8] >> (7 - (bitPos % 8))) & 1);
                value = (value << 1) | bit;
            }
            appendBits(buffer_, bitOffset_, chunk, value);
            pos += chunk;
        }
        for (const auto& ref : cell.references()) {
            storeRef(ref);
        }
        return *this;
    }
    
    std::shared_ptr<Cell> CellBuilder::build() {
        // Створення комірки через new і shared_ptr
        // This approach avoids the private constructor issue with make_shared
        return std::shared_ptr<Cell>(new Cell(buffer_, bitOffset_, references_, false));
    }
}