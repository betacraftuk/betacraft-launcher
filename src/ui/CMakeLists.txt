set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTORCC ON)

if (CMAKE_BUILD_TYPE STREQUAL "Release")
    set(CMAKE_MACOSX_BUNDLE ON)
endif()

find_package(Qt6 REQUIRED COMPONENTS Widgets Concurrent)
qt_standard_project_setup()

qt_add_executable(Betacraft
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
                --no-translations
                $<TARGET_FILE:Betacraft>
        COMMENT "Running windeployqt..."
    )
elseif(UNIX AND NOT APPLE)
    # cmake --install .
    # cpack -G DEB
    # sudo dpkg -i <package-name>

    install(TARGETS Betacraft
        BUNDLE  DESTINATION .
        RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
    )

    qt_generate_deploy_app_script(
        TARGET Betacraft
        FILENAME_VARIABLE deploy_script
        NO_TRANSLATIONS
        NO_UNSUPPORTED_PLATFORM_ERROR
    )

    install(SCRIPT ${deploy_script})

    install(FILES ${CMAKE_SOURCE_DIR}/assets/betacraft.desktop DESTINATION /usr/share/applications)
    install(FILES ${CMAKE_SOURCE_DIR}/assets/betacraft.png DESTINATION /usr/share/icons)

    set(CPACK_PACKAGE_NAME Betacraft)
    set(CPACK_PACKAGE_VERSION "2.0.0-alpha.20230501")
    set(CPACK_PACKAGE_DESCRIPTION_SUMMARY "Betacraft aims to provide easy access to old Minecraft versions and improve the overall game experience.")
    set(CPACK_PACKAGE_VENDOR "Betacraft")
    set(CPACK_PACKAGE_INSTALL_DIRECTORY ${CPACK_PACKAGE_NAME})
    set(CPACK_VERBATIM_VARIABLES ON)
    set(CPACK_PACKAGING_INSTALL_PREFIX "/opt/betacraft")
    set(CPACK_DEBIAN_PACKAGE_MAINTAINER "Kazu <kazuofficial.contact@gmail.com>")
    set(CPACK_DEBIAN_PACKAGE_DEPENDS "libc6, libstdc++6, libgcc-s1, libxcb-cursor0, libtar")

    include(CPack)
elseif(APPLE)
    set(MACOSX_BUNDLE_ICON_FILE betacraft.icns)

    set(app_icon_macos ${CMAKE_SOURCE_DIR}/assets/betacraft.icns)
    set_source_files_properties(${app_icon_macos} PROPERTIES
           MACOSX_PACKAGE_LOCATION "Resources")

    set_property(TARGET Betacraft APPEND PROPERTY SOURCES ${app_icon_macos})
endif()
