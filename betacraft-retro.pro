# -------------------------------------------------
# Project created by QtCreator 2023-12-10T11:37:16
# -------------------------------------------------
DESTDIR = build
OBJECTS_DIR = build/.obj
MOC_DIR = build/.moc
RCC_DIR = build/.rcc
UI_DIR = build/.ui
DEFINES += BC_RETRO
DEFINES += API_MICROSOFT_CLIENT_ID=\\\"$$(BETACRAFT_API_MICROSOFT_CLIENT_ID)\\\"
QMAKE_CFLAGS += -std=c99
INCLUDEPATH += "/usr/local/include/json-c/" # json-c (manual install)
INCLUDEPATH += "/opt/local/include/curl/" # curl (macports install)
INCLUDEPATH += "/opt/local/include/" # libarchive (macports install)
LIBS += -L"/usr/local/lib" -ljson-c -L"/opt/local/lib/" -lcurl -larchive
LIBS += -F/System/Library/Frameworks -framework CoreServices
TARGET = BetacraftRetro
TEMPLATE = app
RESOURCES = src/ui/assets.qrc
SOURCES += src/core/*.c \
    src/ui/accounts/*.cpp \
    src/ui/settings/*.cpp \
    src/ui/servers/*.cpp \
    src/ui/instances/*.cpp \
    src/ui/instances/mods/*.cpp \
    src/ui/*.cpp
HEADERS += src/core/*.h \
    src/ui/accounts/*.h \
    src/ui/settings/*.h \
    src/ui/servers/*.h \
    src/ui/instances/*.h \
    src/ui/instances/mods/*.h \
    src/ui/*.h

ICON = "assets/betacraft-osx.icns"
