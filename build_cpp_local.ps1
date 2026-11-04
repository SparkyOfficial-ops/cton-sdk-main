$ErrorActionPreference = 'Continue'
$root = "c:\Users\Bogdan2\Desktop\cton-sdk-main"
$ssl  = "$root\openssl-4.1.0-beta1"
$cm   = "C:\cmake-4.1.0-rc1-windows-x86_64\bin\cmake.exe"
$log  = "$root\_cppbuild.log"
"=== cpp build start $(Get-Date -Format 'HH:mm:ss') ===" | Set-Content $log

# MSVC + Windows SDK environment
$m   = "E:\VisualStudio\VC\Tools\MSVC\14.44.35207"
$sdk = "C:\Program Files (x86)\Windows Kits\10"
$v   = "10.0.26100.0"
$env:PATH    = "$m\bin\Hostx64\x64;$sdk\bin\$v\x64;$env:PATH"
$env:INCLUDE = "$m\include;$sdk\Include\$v\ucrt;$sdk\Include\$v\um;$sdk\Include\$v\shared;$sdk\Include\$v\winrt"
$env:LIB     = "$m\lib\x64;$sdk\Lib\$v\ucrt\x64;$sdk\Lib\$v\um\x64"

Remove-Item -Recurse -Force "$root\cpp\build" -ErrorAction SilentlyContinue

& $cm -S "$root\cpp" -B "$root\cpp\build" -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=Release `
    -DOPENSSL_ROOT_DIR="$ssl" `
    -DOPENSSL_INCLUDE_DIR="$ssl/include" `
    -DOPENSSL_CRYPTO_LIBRARY="$ssl/libcrypto.lib" `
    -DOPENSSL_SSL_LIBRARY="$ssl/libssl.lib" *>> $log
"cfg_exit=$LASTEXITCODE" | Add-Content $log

if ($LASTEXITCODE -eq 0) {
    & $cm --build "$root\cpp\build" *>> $log
    "build_exit=$LASTEXITCODE" | Add-Content $log
}
"dll: " + (Test-Path "$root\cpp\build\cton-sdk-core.dll") | Add-Content $log
"test exe: " + (Test-Path "$root\cpp\build\cton-sdk-test.exe") | Add-Content $log
"=== cpp build end $(Get-Date -Format 'HH:mm:ss') ===" | Add-Content $log
