@echo off
REM Build script for CTON-SDK API Client
REM Author: Андрій Будильников (Sparky)

echo Building CTON-SDK API Client...

REM Build the API client
mvn clean install

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo API Client built successfully!
) else (
    echo API Client build failed!
    exit /b %ERRORLEVEL%
)

echo Build completed successfully!