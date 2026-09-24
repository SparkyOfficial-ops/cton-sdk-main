#!/bin/bash

# Build script for CTON-SDK C++ Core on Linux/macOS
# Author: Андрій Будильников (Sparky)

echo "Building CTON-SDK C++ Core..."

# Check if required tools are available
if ! command -v cmake &> /dev/null; then
    echo "CMake is not installed. Please install CMake 3.10 or higher."
    exit 1
fi

if ! command -v make &> /dev/null && ! command -v g++ &> /dev/null && ! command -v clang++ &> /dev/null; then
    echo "No C++ compiler found. Please install GCC, Clang, or another C++ compiler."
    exit 1
fi

# Check if OpenSSL is installed
if ! command -v openssl &> /dev/null; then
    echo "OpenSSL is not installed. Please install OpenSSL development libraries."
    echo "On Ubuntu/Debian: sudo apt-get install libssl-dev"
    echo "On CentOS/RHEL: sudo yum install openssl-devel"
    echo "On macOS: brew install openssl"
    exit 1
fi

# Create build directory
mkdir -p cpp/build

# Build C++ core
cd cpp/build
cmake .. -DCMAKE_BUILD_TYPE=Release
make

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "C++ core built successfully!"
else
    echo "C++ core build failed!"
    exit 1
fi

cd ../..

echo "C++ build completed successfully!"