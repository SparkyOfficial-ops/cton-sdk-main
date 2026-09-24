// Boc.cpp - робота з Bag of Cells (BOC)
// Author: Андрій Будильников (Sparky)
// Реалізація Bag of Cells - основного формату серіалізації даних в TON
// Implementation of Bag of Cells - the main data serialization format in TON
// Реализация Bag of Cells - основного формата сериализации данных в TON
//
// Сериалізація відповідає стандартному формату TON BoC (TEP-11 / TVM):
//   magic 0xB5EE9C72, дескриптори комірок d1/d2, 1-bit completion tag,
//   індекс та контрольна сума CRC32-C (Castagnoli).

#include "../include/Boc.h"
#include <stdexcept>
#include <algorithm>
#include <cstring>
#include <set>
#include <queue>
#include <map>
#include <unordered_set>
#include <unordered_map>

namespace cton {

    // Кількість байтів, необхідних для представлення значення (мінімум 1)
    static size_t byteLength(uint64_t value) {
        size_t n = 1;
        while (n < 8 && (value >> (static_cast<size_t>(n) * 8)) != 0) {
            ++n;
        }
        return n;
    }

    // Запис беззнакового цілого у форматі big-endian, завдовжки `bytes` байтів
    static void appendUintBE(std::vector<uint8_t>& out, uint64_t value, size_t bytes) {
        for (size_t i = bytes; i > 0; --i) {
            out.push_back(static_cast<uint8_t>((value >> ((i - 1) * 8)) & 0xFF));
        }
    }

    // Читання беззнакового цілого big-endian довільної (1..8) довжини
    static uint64_t readUintBE(const std::vector<uint8_t>& data, size_t& offset, size_t bytes) {
        if (bytes > 8 || offset + bytes > data.size()) {
            throw std::out_of_range("Not enough data to read integer");
        }
        uint64_t value = 0;
        for (size_t i = 0; i < bytes; ++i) {
            value = (value << 8) | data[offset++];
        }
        return value;
    }

    Boc::Boc() : root_(nullptr) {}

    Boc::Boc(std::shared_ptr<Cell> root) : root_(root) {}

    std::vector<uint8_t> Boc::serialize(bool hasIdx, bool hashCRC) const {
        // Серіалізація BOC у стандартний бінарний формат TON
        // Serialize BOC into the standard TON binary representation

        if (!root_) {
            return std::vector<uint8_t>();
        }

        // Збираємо комірки у топологічному порядку (DFS), корінь отримує індекс 0
        // Collect cells in topological (DFS) order; the root gets index 0
        std::vector<std::shared_ptr<Cell>> cells;
        std::unordered_set<std::shared_ptr<Cell>> visited;
        collectCells(root_, cells, visited);

        std::unordered_map<std::shared_ptr<Cell>, size_t> cellIndices;
        cellIndices.reserve(cells.size());
        for (size_t i = 0; i < cells.size(); ++i) {
            cellIndices[cells[i]] = i;
        }

        const size_t cellCount = cells.size();
        const size_t sizeBytes = byteLength(cellCount); // поле 'size' (кількість байтів для індексів)

        // Спочатку серіалізуємо кожну комірку, щоб дізнатися tot_cells_size (для off_bytes)
        std::vector<std::vector<uint8_t>> cellReprs(cellCount);
        uint64_t totCellsSize = 0;
        for (size_t i = 0; i < cellCount; ++i) {
            const auto& cell = cells[i];
            size_t bits = cell->getBitSize();
            std::vector<uint8_t> data = cell->getData();
            auto refs = cell->getReferences();

            size_t dataBytes = (bits + 7) / 8;
            uint8_t d1 = static_cast<uint8_t>((refs.size() & 0x07) + (cell->isSpecial() ? 8 : 0));
            uint8_t d2 = static_cast<uint8_t>((bits / 8) + dataBytes); // floor(bits/8) + ceil(bits/8)

            std::vector<uint8_t>& repr = cellReprs[i];
            repr.reserve(2 + dataBytes + refs.size() * sizeBytes);
            repr.push_back(d1);
            repr.push_back(d2);

            // Дані комірки; для неприв'язаних до байта бітів додаємо completion tag (біт 1 + нулі)
            for (size_t b = 0; b < dataBytes; ++b) {
                uint8_t byteVal = (b < data.size()) ? data[b] : 0;
                if (b + 1 == dataBytes && (bits % 8) != 0) {
                    size_t rem = bits % 8;
                    byteVal = static_cast<uint8_t>(byteVal & ((0xFF << (8 - rem)) & 0xFF));
                    byteVal = static_cast<uint8_t>(byteVal | (0x80 >> rem));
                }
                repr.push_back(byteVal);
            }

            // Індекси референсів: sizeBytes байтів, big-endian
            for (const auto& ref : refs) {
                size_t refIndex = 0;
                if (auto refCell = ref.lock()) {
                    auto it = cellIndices.find(refCell);
                    if (it != cellIndices.end()) {
                        refIndex = it->second;
                    }
                }
                appendUintBE(repr, refIndex, sizeBytes);
            }

            totCellsSize += repr.size();
        }

        const size_t offBytes = byteLength(totCellsSize);

        std::vector<uint8_t> result;
        result.reserve(14 + totCellsSize + (hasIdx ? cellCount * offBytes : 0) + 4);

        // Magic: 0xB5 0xEE 0x9C 0x72
        result.push_back(0xB5);
        result.push_back(0xEE);
        result.push_back(0x9C);
        result.push_back(0x72);

        // Прапорці: has_idx(1) has_crc32c(1) has_cache_bits(1) flags(2) size(3)
        uint8_t flags = static_cast<uint8_t>(sizeBytes & 0x07);
        if (hasIdx) flags |= 0x80;
        if (hashCRC) flags |= 0x40;
        result.push_back(flags);

        // off_bytes
        result.push_back(static_cast<uint8_t>(offBytes & 0xFF));

        // cells, roots, absent
        appendUintBE(result, cellCount, sizeBytes);
        appendUintBE(result, 1, sizeBytes);   // одна коренева комірка
        appendUintBE(result, 0, sizeBytes);   // немає відсутніх комірок

        // tot_cells_size
        appendUintBE(result, totCellsSize, offBytes);

        // root_list: індекс кореня = 0
        appendUintBE(result, 0, sizeBytes);

        // index: кумулятивні зміщення (в кінці кожного рядка комірки)
        if (hasIdx) {
            uint64_t cumulative = 0;
            for (size_t i = 0; i < cellCount; ++i) {
                cumulative += cellReprs[i].size();
                appendUintBE(result, cumulative, offBytes);
            }
        }

        // cell_data
        for (size_t i = 0; i < cellCount; ++i) {
            result.insert(result.end(), cellReprs[i].begin(), cellReprs[i].end());
        }

        // crc32c (CRC32-C Castagnoli) по всій попередній послідовності
        if (hashCRC) {
            uint32_t crc = calculateCRC32(result);
            appendUintBE(result, crc, 4);
        }

        return result;
    }

    Boc Boc::deserialize(const std::vector<uint8_t>& data) {
        BocParser parser(data);
        return parser.parse();
    }

    std::shared_ptr<Cell> Boc::getRoot() const {
        return root_;
    }

    void Boc::setRoot(std::shared_ptr<Cell> root) {
        root_ = root;
    }

    void Boc::collectCells(std::shared_ptr<Cell> cell,
                         std::vector<std::shared_ptr<Cell>>& cells,
                         std::unordered_set<std::shared_ptr<Cell>>& visited) const {
        // Перевірка, чи комірка вже відвідана
        if (visited.find(cell) != visited.end()) {
            return;
        }

        visited.insert(cell);
        cells.push_back(cell);

        // Рекурсивно обробити референси
        auto references = cell->getReferences();
        for (const auto& ref : references) {
            if (auto refCell = ref.lock()) {
                collectCells(refCell, cells, visited);
            }
        }
    }

    BocParser::BocParser(const std::vector<uint8_t>& data) : data_(data), offset_(0) {}

    Boc BocParser::parse() {
        // Десеріалізація стандартного TON BoC
        if (data_.size() < 10) {
            throw std::invalid_argument("Invalid BOC data: too short");
        }

        uint32_t magic = static_cast<uint32_t>(readUintBE(data_, offset_, 4));
        bool implicitIdx = true;
        bool implicitCrc = true;
        if (magic == 0xB5EE9C72u) {          // generic: has_idx, has_crc32c
            implicitIdx = true;
            implicitCrc = true;
        } else if (magic == 0x68FF65F3u) {   // не містить індексу, має crc
            implicitIdx = false;
            implicitCrc = true;
        } else if (magic == 0xFF68FF65u) {   // без індексу та без crc
            implicitIdx = false;
            implicitCrc = false;
        } else {
            throw std::invalid_argument("Invalid BOC magic");
        }

        uint8_t flagsByte = static_cast<uint8_t>(readUintBE(data_, offset_, 1));
        bool hasIdx = (flagsByte & 0x80) != 0;
        bool hasCrc = (flagsByte & 0x40) != 0;
        size_t sizeBytes = flagsByte & 0x07;
        size_t offBytes = static_cast<size_t>(readUintBE(data_, offset_, 1));

        (void)implicitIdx;
        (void)implicitCrc;

        if (sizeBytes == 0 || sizeBytes > 4 || offBytes == 0 || offBytes > 8) {
            throw std::invalid_argument("Invalid BOC size/off bytes");
        }

        uint64_t cellCount = readUintBE(data_, offset_, sizeBytes);
        uint64_t rootCount = readUintBE(data_, offset_, sizeBytes);
        uint64_t absentCount = readUintBE(data_, offset_, sizeBytes);
        (void)absentCount;
        uint64_t totCellsSize = readUintBE(data_, offset_, offBytes);
        (void)totCellsSize;

        std::vector<size_t> rootIndices;
        rootIndices.reserve(static_cast<size_t>(rootCount));
        for (uint64_t i = 0; i < rootCount; ++i) {
            rootIndices.push_back(static_cast<size_t>(readUintBE(data_, offset_, sizeBytes)));
        }

        // Розділ індексу (пропускаємо — читаємо комірки послідовно)
        if (hasIdx) {
            offset_ += static_cast<size_t>(cellCount) * offBytes;
        }

        const size_t n = static_cast<size_t>(cellCount);
        std::vector<std::vector<uint8_t>> cellDataVec(n);
        std::vector<size_t> cellBits(n, 0);
        std::vector<bool> cellSpecial(n, false);
        std::vector<std::vector<size_t>> cellRefIdx(n);

        for (size_t i = 0; i < n; ++i) {
            uint8_t d1 = static_cast<uint8_t>(readUintBE(data_, offset_, 1));
            uint8_t d2 = static_cast<uint8_t>(readUintBE(data_, offset_, 1));

            size_t refCount = d1 & 0x07;
            bool isExotic = (d1 & 0x08) != 0;
            bool hasHashes = (d1 & 0x10) != 0;

            size_t dataBytes = (static_cast<size_t>(d2) + 1) / 2; // ceil(bits/8)
            bool isAugmented = (d2 & 1) != 0;

            if (offset_ + dataBytes > data_.size()) {
                throw std::out_of_range("Not enough data to read cell payload");
            }
            std::vector<uint8_t> raw(data_.begin() + offset_, data_.begin() + offset_ + dataBytes);
            offset_ += dataBytes;

            size_t bits = 0;
            if (dataBytes == 0) {
                bits = 0;
            } else if (isAugmented) {
                // Останній байт містить completion tag (наймолодший встановлений біт)
                uint8_t last = raw[dataBytes - 1];
                int trailing = 0;
                uint8_t t = last;
                while (t != 0 && (t & 1) == 0) {
                    t = static_cast<uint8_t>(t >> 1);
                    ++trailing;
                }
                bits = (dataBytes - 1) * 8 + (8 - static_cast<size_t>(trailing) - 1);
                // Прибираємо completion tag та все після нього
                raw[dataBytes - 1] = static_cast<uint8_t>(last & ((0xFF << (trailing + 1)) & 0xFF));
            } else {
                bits = dataBytes * 8;
            }

            // Якщо врятовані хеші/глибини — пропускаємо їх
            if (hasHashes) {
                size_t level = (d1 >> 5) & 0x07;
                size_t skip = (level + 1) * 32 + refCount * 32 + refCount * 2;
                offset_ += skip;
            }

            std::vector<size_t> refIdx;
            refIdx.reserve(refCount);
            for (size_t j = 0; j < refCount; ++j) {
                refIdx.push_back(static_cast<size_t>(readUintBE(data_, offset_, sizeBytes)));
            }

            cellDataVec[i] = std::move(raw);
            cellBits[i] = bits;
            cellSpecial[i] = isExotic;
            cellRefIdx[i] = std::move(refIdx);
        }

        // Створюємо комірки, потім зв'язуємо референси (щоб дозволити back-references)
        std::vector<std::shared_ptr<Cell>> cells(n);
        for (size_t i = 0; i < n; ++i) {
            cells[i] = std::make_shared<Cell>(cellDataVec[i], cellBits[i],
                                              std::vector<std::shared_ptr<Cell>>(), cellSpecial[i]);
        }
        for (size_t i = 0; i < n; ++i) {
            for (size_t r : cellRefIdx[i]) {
                if (r < n && cells[r]) {
                    cells[i]->addReference(cells[r]);
                }
            }
        }

        std::shared_ptr<Cell> root;
        if (!rootIndices.empty() && rootIndices[0] < n) {
            root = cells[rootIndices[0]];
        } else if (n > 0) {
            root = cells[0];
        } else {
            root = std::make_shared<Cell>();
        }
        return Boc(root);
    }

    uint8_t BocParser::readByte() {
        if (offset_ >= data_.size()) {
            throw std::out_of_range("Not enough data to read byte");
        }
        return data_[offset_++];
    }

    uint32_t BocParser::readUint32() {
        if (offset_ + 4 > data_.size()) {
            throw std::out_of_range("Not enough data to read uint32");
        }

        uint32_t value = 0;
        value |= static_cast<uint32_t>(data_[offset_++]) << 24;
        value |= static_cast<uint32_t>(data_[offset_++]) << 16;
        value |= static_cast<uint32_t>(data_[offset_++]) << 8;
        value |= static_cast<uint32_t>(data_[offset_++]);

        return value;
    }

    std::vector<uint8_t> BocParser::readBytes(size_t size) {
        if (offset_ + size > data_.size()) {
            throw std::out_of_range("Not enough data to read bytes");
        }

        std::vector<uint8_t> result(data_.begin() + offset_, data_.begin() + offset_ + size);
        offset_ += size;
        return result;
    }

    // CRC32-C (Castagnoli) для BOC — відображений поліноміал 0x82F63B78
    // CRC32-C (Castagnoli) — reflected polynomial, as required by the TON BoC format
    uint32_t Boc::calculateCRC32(const std::vector<uint8_t>& data) {
        static uint32_t crcTable[256];
        static bool tableInitialized = false;

        if (!tableInitialized) {
            for (int i = 0; i < 256; i++) {
                uint32_t crc = static_cast<uint32_t>(i);
                for (int j = 0; j < 8; j++) {
                    if (crc & 1) {
                        crc = (crc >> 1) ^ 0x82F63B78u; // CRC32-C reflected polynomial
                    } else {
                        crc >>= 1;
                    }
                }
                crcTable[i] = crc;
            }
            tableInitialized = true;
        }

        uint32_t crc = 0xFFFFFFFFu;
        for (uint8_t byte : data) {
            crc = (crc >> 8) ^ crcTable[(crc ^ byte) & 0xFF];
        }

        return crc ^ 0xFFFFFFFFu;
    }

    BocBuilder::BocBuilder(std::shared_ptr<Cell> root) : root_(root) {}

    std::vector<uint8_t> BocBuilder::build(bool hasIdx, bool hashCRC) {
        if (!root_) {
            return std::vector<uint8_t>();
        }

        Boc boc(root_);
        return boc.serialize(hasIdx, hashCRC);
    }
}
