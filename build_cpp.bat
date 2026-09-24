@echo off
REM Build script for CTON-SDK C++ Core
REM Author: Андрій Будильников (Sparky)

echo Building CTON-SDK C++ Core...

REM Check if vcpkg directory exists
if not exist vcpkg (
    echo Cloning vcpkg...
    git clone https://github.com/Microsoft/vcpkg.git
    if %ERRORLEVEL% NEQ 0 (
        echo Failed to clone vcpkg
        exit /b %ERRORLEVEL%
    )
)

REM Check if vcpkg is bootstrapped
cd vcpkg
if not exist vcpkg.exe (
    echo Bootstrapping vcpkg...
    cmd /c bootstrap-vcpkg.bat
    if %ERRORLEVEL% NEQ 0 (
        echo Failed to bootstrap vcpkg
        cd ..
        exit /b %ERRORLEVEL%
    )
)

REM Try to integrate vcpkg with the system
echo Integrating vcpkg...
cmd /c vcpkg integrate install
if %ERRORLEVEL% NEQ 0 (
    echo Warning: Failed to integrate vcpkg, continuing anyway...
)

cd ..

REM Create build directory
if not exist cpp\build mkdir cpp\build

REM Build C++ core without explicit toolchain (rely on system OpenSSL)
cd cpp\build
cmake .. -G "Visual Studio 17 2022" -A x64
cmake --build . --config Release

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo C++ core built successfully!
) else (
    echo C++ core build failed!
    exit /b %ERRORLEVEL%
)

cd ..\..

echo C++ build completed successfully!