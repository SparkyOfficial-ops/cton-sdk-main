#ifndef CTON_SLICE_H
#define CTON_SLICE_H

#include "Address.h"
#include "Cell.h"
#include <cstdint>
#include <vector>

namespace cton {

    /**
     * Cursor over a cell, matching the TVM slice primitives used to read
     * contract answers, account state and messages.
     */
    class Slice {
    public:
        explicit Slice(const Cell& cell);

        uint64_t loadUint(size_t bits);
        int64_t loadInt(size_t bits);
        std::vector<uint8_t> loadCoins();
        bool loadAddress(Address& out);
        std::shared_ptr<Cell> loadRef();

        size_t remainingBits() const;
        size_t remainingRefs() const;

    private:
        std::vector<uint8_t> data_;
        size_t bitSize_;
        size_t bitPos_;
        std::vector<std::shared_ptr<Cell>> refs_;
        size_t refPos_;

        uint64_t readBits(size_t bits);
    };

}

#endif
