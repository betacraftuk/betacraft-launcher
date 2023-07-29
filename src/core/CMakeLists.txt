add_library(Core
    Betacraft.c
    Betacraft.h
    Instance.c
    Instance.h
    Game.c
    Game.h
    Network.c
    Network.h
    FileSystem.c
    FileSystem.h
    Version.c
    Version.h
    AssetIndex.c
    AssetIndex.h
    VersionList.c
    VersionList.h
    Settings.c
    Settings.h
    StringUtils.c
    StringUtils.h
    JsonExtension.c
    JsonExtension.h
    AuthMicrosoft.c
    AuthMicrosoft.h
    Account.c
    Account.h
    JavaInstallations.c
    JavaInstallations.h
    Logger.c
    Logger.h
    Discord.c
    Discord.h
    Mod.c
    Mod.h
    ProcessHandler.c
    ProcessHandler.h
    WindowsProcessHandler.c
    WindowsProcessHandler.h
    ../../lib/discord_game_sdk/discord_game_sdk.h
)

find_package(json-c CONFIG REQUIRED)
find_package(CURL REQUIRED)
find_package(LibArchive REQUIRED)

if (WIN32)
    target_link_libraries(Core PUBLIC ${CMAKE_SOURCE_DIR}/lib/discord_game_sdk/x86_64/discord_game_sdk.dll.lib)
elseif (APPLE)
    set(CMAKE_APPLE_SILICON_PROCESSOR "x86_64")
    #set(CMAKE_APPLE_SILICON_PROCESSOR "arm64")
    if(CMAKE_APPLE_SILICON_PROCESSOR STREQUAL "arm64")
        target_link_libraries(Core PUBLIC ${CMAKE_SOURCE_DIR}/lib/discord_game_sdk/aarch64/discord_game_sdk.bundle)
    elseif(CMAKE_APPLE_SILICON_PROCESSOR STREQUAL "x86_64")
        target_link_libraries(Core PUBLIC ${CMAKE_SOURCE_DIR}/lib/discord_game_sdk/x86_64/discord_game_sdk.dylib)
    endif()
elseif (UNIX AND NOT APPLE) # Linux
    add_library(discordgamesdk SHARED IMPORTED)
    set_target_properties(discordgamesdk PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/lib/discord_game_sdk/x86_64/discord_game_sdk.so)
    target_link_libraries(Core PUBLIC discordgamesdk)
endif()