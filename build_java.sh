#!/bin/bash

# Build script for CTON-SDK Java Components on Linux/macOS
# Author: Андрій Будильников (Sparky)

echo "Building CTON-SDK Java Components..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

# Check if JDK is installed
if ! command -v javac &> /dev/null; then
    echo "JDK is not installed. Please install JDK 11 or higher."
    exit 1
fi

# Build Java bindings
cd java
mvn clean install

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Java components built successfully!"
else
    echo "Java components build failed!"
    exit 1
fi

cd ..

echo "Java build completed successfully!"