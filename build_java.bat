@echo off
REM Build script for CTON-SDK Java Components
REM Author: Андрій Будильников (Sparky)

echo Building CTON-SDK Java Components...

REM Build Java bindings
cd java
mvn clean install

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo Java components built successfully!
) else (
    echo Java components build failed!
    exit /b %ERRORLEVEL%
)

cd ..

echo Java build completed successfully!