#!/bin/bash

# Build script for CTON-SDK on Linux/macOS
# Author: Андрій Будильников (Sparky)

echo "Building CTON-SDK..."
echo "==================="

# Build C++ core
echo
echo "[1/3] Building C++ Core..."
chmod +x build_cpp.sh
./build_cpp.sh

if [ $? -ne 0 ]; then
    echo "C++ build failed!"
    exit 1
fi

# Copy SO to Java resources (Linux)
echo
echo "[2/3] Copying native libraries..."
if [ -f cpp/build/libcton-sdk-core.so ]; then
    mkdir -p java/src/main/resources/linux-x86-64/
    cp cpp/build/libcton-sdk-core.so java/src/main/resources/
    cp cpp/build/libcton-sdk-core.so java/src/main/resources/linux-x86-64/
    if [ $? -eq 0 ]; then
        echo "Native libraries copied successfully!"
    else
        echo "Failed to copy native libraries!"
        exit 1
    fi
else
    echo "libcton-sdk-core.so not found! Build may have failed."
    exit 1
fi

# Build Java components
echo
echo "[3/3] Building Java Components..."
chmod +x build_java.sh
./build_java.sh

if [ $? -ne 0 ]; then
    echo "Java build failed!"
    exit 1
fi

echo
echo "==================="
echo "Build completed successfully!"