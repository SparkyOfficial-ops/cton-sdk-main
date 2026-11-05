# CTON-SDK C++ Core Library
# Author: Андрій Будильников (Sparky)

cmake_minimum_required(VERSION 3.22)

project(cton-sdk-core CXX)
set(CMAKE_CXX_STANDARD 17)

# Handle Cyrillic characters in path
cmake_policy(SET CMP0049 NEW)

# Add source files
file(GLOB_RECURSE SOURCES "src/*.cpp")
file(GLOB_RECURSE HEADERS "include/*.h" "include/*.hpp")

# Exclude main.cpp and test files from library sources
# (anchored patterns: unanchored "main.cpp" also matches any "main<c>cpp" substring in paths)
list(FILTER SOURCES EXCLUDE REGEX "/main\\.cpp$")
list(FILTER SOURCES EXCLUDE REGEX "/test")
list(FILTER SOURCES EXCLUDE REGEX "/demo\\.cpp$")
list(FILTER SOURCES EXCLUDE REGEX "/compat_test\\.cpp$")

# Create library
add_library(cton-sdk-core SHARED ${SOURCES} ${HEADERS})

# Define export macro for Windows DLL
target_compile_definitions(cton-sdk-core PRIVATE CTON_SDK_CORE_EXPORTS)

# Add debug information
target_compile_options(cton-sdk-core PRIVATE $<$<CXX_COMPILER_ID:MSVC>:/Zi>)
target_link_options(cton-sdk-core PRIVATE $<$<CXX_COMPILER_ID:MSVC>:/DEBUG>)

# Create test executable
add_executable(cton-sdk-test src/main.cpp)
target_link_libraries(cton-sdk-test cton-sdk-core)

# Create demo executable
add_executable(cton-sdk-demo src/demo.cpp)
target_link_libraries(cton-sdk-demo cton-sdk-core)

# Include directories
target_include_directories(cton-sdk-core PUBLIC 
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Shared OpenSSL is the default. Ubuntu libssl-dev and the vcpkg x64-windows
# triplet ship shared libraries; forcing static makes find_package fail.
# OpenSSL 3.x is enough. Do not require a 4.x that the runners do not have.
if(NOT DEFINED OPENSSL_USE_STATIC_LIBS)
    set(OPENSSL_USE_STATIC_LIBS OFF)
endif()

# First try to find OpenSSL in the project directory
if(EXISTS "${CMAKE_SOURCE_DIR}/../openssl")
    set(OPENSSL_ROOT_DIR "${CMAKE_SOURCE_DIR}/../openssl")
    message(STATUS "Looking for OpenSSL in project directory: ${OPENSSL_ROOT_DIR}")
    find_package(OpenSSL QUIET)
endif()

# If not found, try the versioned directory
if(NOT OpenSSL_FOUND AND EXISTS "${CMAKE_SOURCE_DIR}/../openssl-3.6.0")
    set(OPENSSL_ROOT_DIR "${CMAKE_SOURCE_DIR}/../openssl-3.6.0")
    message(STATUS "Looking for OpenSSL in versioned directory: ${OPENSSL_ROOT_DIR}")
    find_package(OpenSSL QUIET)
endif()

# If still not found, try to find it in vcpkg installation
if(NOT OpenSSL_FOUND AND EXISTS "${CMAKE_SOURCE_DIR}/../vcpkg/installed/x64-windows")
    set(OPENSSL_ROOT_DIR "${CMAKE_SOURCE_DIR}/../vcpkg/installed/x64-windows")
    message(STATUS "Looking for OpenSSL in vcpkg directory: ${OPENSSL_ROOT_DIR}")
    find_package(OpenSSL QUIET)
endif()

# A wrong OPENSSL_ROOT_DIR makes FindOpenSSL search only that folder
# (NO_DEFAULT_PATH) and report both the headers and the libraries missing.
if(DEFINED ENV{OPENSSL_ROOT_DIR} AND NOT EXISTS "$ENV{OPENSSL_ROOT_DIR}/include/openssl/ssl.h")
    message(STATUS "Ignoring OPENSSL_ROOT_DIR that has no OpenSSL headers: $ENV{OPENSSL_ROOT_DIR}")
    unset(ENV{OPENSSL_ROOT_DIR})
endif()
if(DEFINED OPENSSL_ROOT_DIR AND NOT EXISTS "${OPENSSL_ROOT_DIR}/include/openssl/ssl.h")
    unset(OPENSSL_ROOT_DIR)
endif()

# GitHub windows-latest exposes VCPKG_INSTALLATION_ROOT, not VCPKG_ROOT.
if(NOT OpenSSL_FOUND AND WIN32)
    set(_vcpkg_roots "")
    if(DEFINED ENV{VCPKG_INSTALLATION_ROOT})
        list(APPEND _vcpkg_roots "$ENV{VCPKG_INSTALLATION_ROOT}")
    endif()
    if(DEFINED ENV{VCPKG_ROOT})
        list(APPEND _vcpkg_roots "$ENV{VCPKG_ROOT}")
    endif()
    list(APPEND _vcpkg_roots "C:/vcpkg")
    foreach(_root IN LISTS _vcpkg_roots)
        if(EXISTS "${_root}/installed/x64-windows/include/openssl/ssl.h")
            set(OPENSSL_ROOT_DIR "${_root}/installed/x64-windows")
            message(STATUS "Looking for OpenSSL in vcpkg: ${OPENSSL_ROOT_DIR}")
            find_package(OpenSSL 3 QUIET)
            break()
        endif()
    endforeach()
endif()

# If still not found, use OpenSSL 3 from the system or OPENSSL_ROOT_DIR.
if(NOT OpenSSL_FOUND)
    message(STATUS "Looking for OpenSSL 3 in system locations")
    find_package(OpenSSL 3 REQUIRED)
endif()

# Link OpenSSL libraries
if(OPENSSL_FOUND)
    # For modern CMake versions, use imported targets if available
    if(TARGET OpenSSL::Crypto AND TARGET OpenSSL::SSL)
        target_link_libraries(cton-sdk-core PRIVATE OpenSSL::Crypto OpenSSL::SSL)
    else()
        # Fallback to direct linking
        target_link_libraries(cton-sdk-core PRIVATE ${OPENSSL_CRYPTO_LIBRARY} ${OPENSSL_SSL_LIBRARY})
        target_include_directories(cton-sdk-core PRIVATE ${OPENSSL_INCLUDE_DIR})
    endif()
    
    target_compile_definitions(cton-sdk-core PRIVATE OPENSSL_AVAILABLE=1)
    
    # Also set for test executables that use OpenSSL
    target_compile_definitions(cton-sdk-test PRIVATE OPENSSL_AVAILABLE=1)
    target_compile_definitions(cton-sdk-demo PRIVATE OPENSSL_AVAILABLE=1)
    
    message(STATUS "Using OpenSSL libraries: ${OpenSSL_LIBRARIES}")
    message(STATUS "Using OpenSSL version: ${OPENSSL_VERSION}")
    message(STATUS "Using OpenSSL include dirs: ${OpenSSL_INCLUDE_DIRS}")

    # Static OpenSSL on Windows pulls in Winsock and the certificate store.
    if(WIN32)
        target_link_libraries(cton-sdk-core PRIVATE ws2_32 crypt32)
    endif()
else()
    message(FATAL_ERROR "OpenSSL libraries not found. OpenSSL is required for cryptographic operations.")
endif()

target_include_directories(cton-sdk-test PUBLIC 
    ${CMAKE_CURRENT_SOURCE_DIR}/test
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

target_include_directories(cton-sdk-demo PUBLIC 
    ${CMAKE_CURRENT_SOURCE_DIR}/test
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Compiler options
target_compile_options(cton-sdk-core PRIVATE 
    $<$<CXX_COMPILER_ID:MSVC>:/W4 /wd4100 /wd4996 /wd4267>  # Disable various warnings
    $<$<NOT:$<CXX_COMPILER_ID:MSVC>>:-Wall -Wextra -pedantic>
)