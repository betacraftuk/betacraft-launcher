set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTORCC ON)

if (CMAKE_BUILD_TYPE STREQUAL "Release")
    set(CMAKE_MACOSX_BUNDLE ON)
endif()

find_package(Qt6 REQUIRED COMPONENTS Widgets Concurrent)

add_executable(Betacraft
    MainWindow.cpp MainWindow.h
    main.cpp
    assets.qrc
    AboutWidget.cpp AboutWidget.h
    instances/InstanceListWidget.cpp instances/InstanceListWidget.h
    instances/AddInstanceWidget.cpp instances/AddInstanceWidget.h
    instances/InstanceEditWidget.cpp instances/InstanceEditWidget.h
    instances/InstanceEditVersionWidget.cpp instances/InstanceEditVersionWidget.h
    instances/InstanceEditAppearanceWidget.cpp instances/InstanceEditAppearanceWidget.h
    instances/InstanceEditArgumentsWidget.cpp instances/InstanceEditArgumentsWidget.h
    instances/mods/InstanceEditModsWidget.cpp instances/mods/InstanceEditModsWidget.h
    instances/mods/InstanceEditModRepoWidget.cpp instances/mods/InstanceEditModRepoWidget.h
    instances/mods/InstanceEditModVersionsWidget.cpp instances/mods/InstanceEditModVersionsWidget.h
    servers/ServerListWidget.cpp servers/ServerListWidget.h
    servers/ServerListItemWidget.h servers/ServerListItemWidget.cpp
    accounts/AccountListWidget.cpp accounts/AccountListWidget.h
    accounts/AccountListItemWidget.cpp accounts/AccountListItemWidget.h
    accounts/AddAccountWidget.cpp accounts/AddAccountWidget.h
    accounts/AddAccountMicrosoftWidget.cpp accounts/AddAccountMicrosoftWidget.h
    accounts/AddAccountMojangWidget.cpp accounts/AddAccountMojangWidget.h
    settings/SettingsWidget.cpp settings/SettingsWidget.h
    settings/SettingsGeneralWidget.cpp settings/SettingsGeneralWidget.h
    settings/SettingsJavaWidget.cpp settings/SettingsJavaWidget.h
    ConsoleLogWidget.cpp ConsoleLogWidget.h)

target_link_libraries(Betacraft PRIVATE
    Core
    Qt6::Widgets
    Qt6::Concurrent
)

if(WIN32)
    set_property(TARGET Betacraft APPEND PROPERTY SOURCES ${CMAKE_SOURCE_DIR}/assets/betacraft.o)

    if(CMAKE_BUILD_TYPE STREQUAL "Release")
        set_property(TARGET Betacraft PROPERTY WIN32_EXECUTABLE true)
    elseif (CMAKE_BUILD_TYPE STREQUAL "Debug")
        find_program(TOOL_WINDEPLOYQT NAMES windeployqt)
    endif()

    add_custom_command(TARGET Betacraft POST_BUILD
        COMMAND ${TOOL_WINDEPLOYQT}
                $<TARGET_FILE:Betacraft>
        COMMENT "Running windeployqt..."
    )
endif()
