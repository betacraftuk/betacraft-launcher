# Betacraft

[Website](https://betacraft.uk/) | [Discord](https://discord.gg/d4WvXeQ)

Betacraft aims to provide easy access to old Minecraft versions and improve the overall game experience.

## System Requirements

* OS
    * Windows (10 or higher).
    * Linux.
    * macOS (10.14 or higher).

## Building

Betacraft requires [CMake](https://cmake.org/). Many libraries are bundled with Betacraft and used if they're not installed on your system. CMake will inform you if a bundled library is used or if you need to install any missing packages yourself.

1. `mkdir build`
2. `cd build`
3. `cmake ..`
4. `make`

If you're building for Linux or macOS, you'll have to build [libtar](https://github.com/tklauser/libtar) which can be pulled as a submodule:
```sh
git submodule update --init --recursive
```
Alternatively, you can download it as a package:

* Linux (Ubuntu)
```sh
sudo apt-get install libtar
```

* macOS
```sh
brew install libtar
```
