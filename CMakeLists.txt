cmake_minimum_required(VERSION 3.8)
set(CMAKE_C_STANDARD_REQUIRED 99)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

if(APPLE)
    set(CMAKE_OSX_ARCHITECTURES "x86_64")
    set(CMAKE_APPLE_SILICON_PROCESSOR "x86_64")
    #set(CMAKE_OSX_ARCHITECTURES "x86_64")
    #set(CMAKE_APPLE_SILICON_PROCESSOR "x86_64")
    message(STATUS "${CMAKE_APPLE_SILICON_PROCESSOR}")
    if(CMAKE_APPLE_SILICON_PROCESSOR STREQUAL "arm64")
        file(COPY ${CMAKE_SOURCE_DIR}/lib/discord_game_sdk/aarch64/discord_game_sdk.dylib
            DESTINATION ${CMAKE_CURRENT_BINARY_DIR}/src/ui/)

		set(VCPKG_TARGET_ARCHITECTURE "arm64")
		set(VCPKG_TARGET_TRIPLET arm64-osx)
    elseif(CMAKE_APPLE_SILICON_PROCESSOR STREQUAL "x86_64")
        file(COPY ${CMAKE_SOURCE_DIR}/lib/discord_game_sdk/x86_64/discord_game_sdk.dylib
            DESTINATION ${CMAKE_CURRENT_BINARY_DIR}/src/ui/)

		set(VCPKG_TARGET_ARCHITECTURE "x64")
		set(VCPKG_TARGET_TRIPLET x64-osx)
    endif()

    set(VCPKG_LIBRARY_LINKAGE "static")
elseif (WIN32)
    file(COPY ${CMAKE_SOURCE_DIR}/lib/discord_game_sdk/x86_64/discord_game_sdk.dll
        DESTINATION ${CMAKE_CURRENT_BINARY_DIR}/src/ui/)
elseif (UNIX AND NOT APPLE) # Linux
    file(COPY ${CMAKE_SOURCE_DIR}/lib/discord_game_sdk/x86_64/discord_game_sdk.so
        DESTINATION ${CMAKE_CURRENT_BINARY_DIR}/src/ui/)

	if (CMAKE_BUILD_TYPE STREQUAL "Release")
        set(CMAKE_INSTALL_PREFIX "/home/betacraft-build")
    endif()
endif()

project(Betacraft)

file(COPY ${CMAKE_CURRENT_SOURCE_DIR}/java_repo.json
    DESTINATION ${CMAKE_CURRENT_BINARY_DIR}/src/ui/)

file(COPY ${CMAKE_CURRENT_SOURCE_DIR}/lang
    DESTINATION ${CMAKE_CURRENT_BINARY_DIR}/src/ui/)

enable_testing()

add_subdirectory(src)
