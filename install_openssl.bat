@echo off
REM Install OpenSSL via vcpkg
REM Author: Андрій Будильников (Sparky)

echo Installing OpenSSL via vcpkg...

REM Check if vcpkg directory exists
if not exist vcpkg (
    echo Cloning vcpkg...
    git clone https://github.com/Microsoft/vcpkg.git
    if %ERRORLEVEL% NEQ 0 (
        echo Failed to clone vcpkg
        exit /b %ERRORLEVEL%
    )
)

cd vcpkg

REM Bootstrap vcpkg if not already done
if not exist vcpkg.exe (
    echo Bootstrapping vcpkg...
    cmd /c bootstrap-vcpkg.bat
    if %ERRORLEVEL% NEQ 0 (
        echo Failed to bootstrap vcpkg
        cd ..
        exit /b %ERRORLEVEL%
    )
)

REM Install OpenSSL
echo Installing OpenSSL...
cmd /c vcpkg install openssl:x64-windows

if %ERRORLEVEL% EQU 0 (
    echo OpenSSL installed successfully!
    echo Libraries should be available at vcpkg\installed\x64-windows\lib\
) else (
    echo Failed to install OpenSSL
    cd ..
    exit /b %ERRORLEVEL%
)

cd ..