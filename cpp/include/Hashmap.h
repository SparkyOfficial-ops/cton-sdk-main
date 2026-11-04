#ifndef CTON_HASHMAP_H
#define CTON_HASHMAP_H

#include "Cell.h"
#include <cstdint>
#include <vector>

namespace cton {

    struct HashmapEntry {
        uint64_t key;
        std::shared_ptr<Cell> value;
    };

    /**
     * Hashmap n X from the TON TL-B spec (binary Patricia tree with
     * hml_short / hml_long / hml_same labels). The returned cell is the
     * dictionary root stored under hme_root$1, not the HashmapE prefix bit.
     * Keys must fit in keyBits, and keyBits must be in 1..64.
     */
    std::shared_ptr<Cell> buildHashmap(int keyBits, const std::vector<HashmapEntry>& entries);

}

#endif
