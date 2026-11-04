#include "../include/Slice.h"

#include <stdexcept>

namespace cton {

    Slice::Slice(const Cell& cell)
        : data_(cell.getData()), bitSize_(cell.getBitSize()), bitPos_(0), refPos_(0) {
        refs_.reserve(cell.references().size());
        for (const auto& ref : cell.references()) {
            refs_.push_back(Cell::cloneTree(ref.get()));
        }
    }

    uint64_t Slice::readBits(size_t bits) {
        if (bits > 64) {
            throw std::invalid_argument("Cannot load more than 64 bits at once");
        }
        if (bitPos_ + bits > bitSize_) {
            throw std::out_of_range("Slice has fewer bits than requested");
        }
        uint64_t value = 0;
        for (size_t i = 0; i < bits; ++i) {
            size_t pos = bitPos_ + i;
            uint8_t bit = static_cast<uint8_t>((data_[pos / 8] >> (7 - (pos % 8))) & 1);
            value = (value << 1) | bit;
        }
        bitPos_ += bits;
        return value;
    }

    uint64_t Slice::loadUint(size_t bits) {
        return readBits(bits);
    }

    int64_t Slice::loadInt(size_t bits) {
        if (bits == 0 || bits > 64) {
            throw std::invalid_argument("signed load must be 1..64 bits");
        }
        uint64_t raw = readBits(bits);
        if (bits == 64) {
            return static_cast<int64_t>(raw);
        }
        uint64_t sign = 1ULL << (bits - 1);
        if (raw & sign) {
            uint64_t extend = (~0ULL) << bits;
            return static_cast<int64_t>(raw | extend);
        }
        return static_cast<int64_t>(raw);
    }

    std::vector<uint8_t> Slice::loadCoins() {
        uint64_t length = readBits(4);
        if (length > 15) {
            throw std::out_of_range("VarUInteger 16 length is out of range");
        }
        std::vector<uint8_t> bytes(static_cast<size_t>(length));
        for (size_t i = 0; i < bytes.size(); ++i) {
            bytes[i] = static_cast<uint8_t>(readBits(8));
        }
        return bytes;
    }

    bool Slice::loadAddress(Address& out) {
        uint64_t tag = readBits(2);
        if (tag == 0) {
            return false;
        }
        if (tag != 2) {
            throw std::invalid_argument("only addr_none and addr_std can be loaded");
        }
        uint64_t anycast = readBits(1);
        if (anycast != 0) {
            throw std::invalid_argument("anycast addresses are not supported");
        }
        int64_t workchain = loadInt(8);
        std::vector<uint8_t> hash(32);
        for (size_t i = 0; i < hash.size(); ++i) {
            hash[i] = static_cast<uint8_t>(readBits(8));
        }
        out = Address(static_cast<int8_t>(workchain), hash);
        return true;
    }

    std::shared_ptr<Cell> Slice::loadRef() {
        if (refPos_ >= refs_.size()) {
            throw std::out_of_range("Slice has no more references");
        }
        return refs_[refPos_++];
    }

    size_t Slice::remainingBits() const {
        return bitSize_ - bitPos_;
    }

    size_t Slice::remainingRefs() const {
        return refs_.size() - refPos_;
    }

}
