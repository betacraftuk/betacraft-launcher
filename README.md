# Betacraft Retro

## Betacraft Launcher for legacy Windows and Mac OS X.
Updates for Betacraft Retro are not going to be consistent with the [regular v2 branch](https://github.com/betacraftuk/betacraft-launcher/tree/v2).

### Building on Mac OS X 10.4.11 (i386)

_Targeting i386_

1. Install [Xcode 2.5](https://developer.apple.com/services-account/download?path=/Developer_Tools/xcode_2.5_developer_tools/xcode25_8m2558_developerdvd.dmg) (you need an Apple Developer account for this) & latest [MacPorts for Tiger](https://www.macports.org/install.php#installing)
2. Setup MacPorts, install: `apple-gcc42`, `libarchive`, `curl`
3. [Download](https://github.com/Kitware/CMake/archive/refs/tags/v3.9.6.zip), [build & install](https://github.com/Kitware/CMake/tree/v3.9.6?tab=readme-ov-file#building-cmake-from-scratch) CMake 3.9.6
4. [Download](https://github.com/json-c/json-c), [build & install](https://github.com/json-c/json-c?tab=readme-ov-file#build-instructions--) json-c
5. Download & install: [Qt Creator 1.2.1](https://download.qt.io/archive/qtcreator/1/qt-creator-mac-opensource-1.2.1.dmg), [Qt 4.7.3](https://download.qt.io/archive/qt/4.7/qt-mac-carbon-opensource-4.7.3.dmg)
6. Open up Qt Creator, make sure it's using Qt 4.7.3
7. Load up the project in Qt Creator, you're ready to build the launcher.

_Targeting ppc_\
TODO

### Building on Mac OS X 10.5.8 (x86_64)

_Targeting x86_64_\
TODO

_Targeting ppc64_\
TODO

### Building on Windows XP SP3

_Targeting i586_\
TODO
