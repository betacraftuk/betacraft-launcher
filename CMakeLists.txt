cmake_minimum_required(VERSION 3.12)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_BUILD_TYPE "Release")

if(APPLE)
    set(CMAKE_APPLE_SILICON_PROCESSOR "arm64")

    message(STATUS "${CMAKE_APPLE_SILICON_PROCESSOR}")

    if(CMAKE_APPLE_SILICON_PROCESSOR STREQUAL "arm64")
        set(CMAKE_OSX_ARCHITECTURES "arm64")

    elseif(CMAKE_APPLE_SILICON_PROCESSOR STREQUAL "x86_64")
        set(CMAKE_OSX_ARCHITECTURES "x86_64")

    endif()
elseif (UNIX AND NOT APPLE) # Linux
    if (CMAKE_BUILD_TYPE STREQUAL "Release")
        set(CMAKE_INSTALL_PREFIX ${CMAKE_CURRENT_SOURCE_DIR}/build/release)
    endif()
endif()

project(Betacraft)

if (NOT DEFINED ENV{BETACRAFT_API_MICROSOFT_CLIENT_ID})
    set(API_MICROSOFT_CLIENT_ID "")
else()
    set(API_MICROSOFT_CLIENT_ID $ENV{BETACRAFT_API_MICROSOFT_CLIENT_ID})
endif()


add_compile_definitions(API_MICROSOFT_CLIENT_ID="${API_MICROSOFT_CLIENT_ID}")

include(CTest)
include(FetchContent)

FetchContent_Declare(json URL https://github.com/nlohmann/json/releases/download/v3.11.3/json.tar.xz)
FetchContent_MakeAvailable(json)

add_subdirectory(lib/json-c)
add_subdirectory(lib/cpr)
add_subdirectory(lib/libarchive)
add_subdirectory(lib/gtest)

add_subdirectory(src)
