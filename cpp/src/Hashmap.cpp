#include "../include/Hashmap.h"

#include <stdexcept>
#include <string>

#if defined(_MSC_VER)
#include <intrin.h>
#endif

namespace cton {
namespace {

    struct Item {
        std::string key;
        std::shared_ptr<Cell> value;
    };

    std::string toKeyBits(uint64_t key, int width) {
        if (width < 64 && (key >> width) != 0) {
            throw std::invalid_argument("dictionary key does not fit in the key length");
        }
        std::string bits(static_cast<size_t>(width), '0');
        for (int i = 0; i < width; ++i) {
            if ((key >> (width - 1 - i)) & 1ULL) {
                bits[static_cast<size_t>(i)] = '1';
            }
        }
        return bits;
    }

    bool sameBit(const std::string& bits) {
        if (bits.empty()) {
            return false;
        }
        char first = bits[0];
        for (char bit : bits) {
            if (bit != first) {
                return false;
            }
        }
        return true;
    }

    void writeBits(CellBuilder& builder, const std::string& bits) {
        for (char bit : bits) {
            builder.storeUInt(1, bit == '1' ? 1 : 0);
        }
    }

    /**
     * Shortest of hml_short, hml_long and hml_same. Equal lengths use the long form,
     * matching the builder used by Highload Wallet v2.
     */
    int sizeBitsOf(int maxLength) {
        if (maxLength <= 0) {
            return 0;
        }
#if defined(_MSC_VER)
        unsigned long index = 0;
        _BitScanReverse(&index, static_cast<unsigned long>(maxLength));
        return static_cast<int>(index) + 1;
#else
        return 32 - __builtin_clz(static_cast<unsigned>(maxLength));
#endif
    }

    void writeLabel(CellBuilder& builder, const std::string& bits, int maxLength) {
        int length = static_cast<int>(bits.size());
        int sizeBits = sizeBitsOf(maxLength);
        int shortCost = length <= maxLength ? 2 + 2 * length : 0x7fffffff;
        int longCost = 2 + sizeBits + length;
        bool uniform = length > 0 && sameBit(bits);
        int sameCost = uniform ? 3 + sizeBits : 0x7fffffff;
        if (shortCost < longCost && shortCost < sameCost) {
            builder.storeUInt(1, 0);
            for (int i = 0; i < length; ++i) {
                builder.storeUInt(1, 1);
            }
            builder.storeUInt(1, 0);
            writeBits(builder, bits);
            return;
        }
        if (sameCost < longCost) {
            builder.storeUInt(2, 3);
            builder.storeUInt(1, bits[0] == '1' ? 1 : 0);
            builder.storeUInt(static_cast<size_t>(sizeBits), static_cast<uint64_t>(length));
            return;
        }
        builder.storeUInt(2, 2);
        builder.storeUInt(static_cast<size_t>(sizeBits), static_cast<uint64_t>(length));
        writeBits(builder, bits);
    }

    int sharedPrefix(const std::vector<Item>& items, int prefixLength, int limit) {
        int count = 0;
        while (count < limit) {
            char bit = items[0].key[static_cast<size_t>(prefixLength + count)];
            bool same = true;
            for (size_t i = 1; i < items.size(); ++i) {
                if (items[i].key[static_cast<size_t>(prefixLength + count)] != bit) {
                    same = false;
                    break;
                }
            }
            if (!same) {
                break;
            }
            ++count;
        }
        return count;
    }

    void writeEdge(std::vector<Item>& items, int keyLength, int prefixLength, CellBuilder& builder) {
        int remaining = keyLength - prefixLength;
        int shared = sharedPrefix(items, prefixLength, remaining);
        writeLabel(builder, items[0].key.substr(static_cast<size_t>(prefixLength), static_cast<size_t>(shared)), remaining);
        int after = prefixLength + shared;
        if (after == keyLength) {
            if (items.size() != 1) {
                throw std::invalid_argument("dictionary contains a duplicate key");
            }
            builder.appendCell(*items[0].value);
            return;
        }
        std::vector<Item> left;
        std::vector<Item> right;
        for (const Item& item : items) {
            if (item.key[static_cast<size_t>(after)] == '0') {
                left.push_back(item);
            } else {
                right.push_back(item);
            }
        }
        if (left.empty() || right.empty()) {
            throw std::logic_error("dictionary fork is missing a branch");
        }
        CellBuilder leftCell;
        writeEdge(left, keyLength, after + 1, leftCell);
        CellBuilder rightCell;
        writeEdge(right, keyLength, after + 1, rightCell);
        builder.storeRef(leftCell.build());
        builder.storeRef(rightCell.build());
    }

}

    std::shared_ptr<Cell> buildHashmap(int keyBits, const std::vector<HashmapEntry>& entries) {
        if (keyBits < 1 || keyBits > 64) {
            throw std::invalid_argument("dictionary key length must be 1..64 bits");
        }
        if (entries.empty()) {
            throw std::invalid_argument("dictionary has no entries");
        }
        std::vector<Item> items;
        items.reserve(entries.size());
        for (const HashmapEntry& entry : entries) {
            if (!entry.value) {
                throw std::invalid_argument("dictionary value is null");
            }
            items.push_back(Item{toKeyBits(entry.key, keyBits), entry.value});
        }
        CellBuilder root;
        writeEdge(items, keyBits, 0, root);
        return root.build();
    }

}
