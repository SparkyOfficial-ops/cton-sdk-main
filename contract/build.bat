@echo off
REM Build script for CTON-SDK Contract Module
REM Author: Андрій Будильников (Sparky)

echo Building CTON-SDK Contract Module...

REM Build the contract module
mvn clean install

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo Contract Module built successfully!
) else (
    echo Contract Module build failed!
    exit /b %ERRORLEVEL%
)

echo Build completed successfully!