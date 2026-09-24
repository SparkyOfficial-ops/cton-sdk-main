@echo off
REM Build script for CTON-SDK
REM Author: Андрій Будильников (Sparky)

echo Building CTON-SDK...

REM Create build directory
if not exist cpp\build mkdir cpp\build

REM Build C++ core
cd cpp\build
cmake ..
cmake --build . --config Release

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo C++ core built successfully!
) else (
    echo C++ core build failed!
    exit /b %ERRORLEVEL%
)

REM Go back to root
cd ..\..

REM Build Java bindings
cd java
mvn clean install

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo Java bindings built successfully!
) else (
    echo Java bindings build failed!
    exit /b %ERRORLEVEL%
)

cd ..

echo Build completed successfully!