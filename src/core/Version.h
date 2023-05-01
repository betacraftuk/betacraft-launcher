#ifndef BC_VERSION_H
#define BC_VERSION_H

#include <json-c/json_object.h>

typedef struct bc_version_javaExec {
    char component[256];
    int majorVersion;
    int advisedMaxVersion; // Betacraft only
    int minVersion; // Betacraft only
} bc_version_javaExec;

typedef struct bc_version_downloadable {
    char sha1[128];
    char url[256];
    int size;
} bc_version_downloadable;

typedef struct bc_version_classifiersMap {
    char id[256];
    bc_version_downloadable object;
} bc_version_classifiersMap;

typedef struct bc_version_nativeMap {
    char os[256];
    char classifierId[256];
} bc_version_nativeMap;

typedef struct bc_version_assetIndexData {
    char id[16];
    char sha1[128];
    char url[256];
    int size;
    int totalSize;
} bc_version_assetIndexData;

typedef struct bc_version_featureStruct {
    int is_demo_user;
    int has_custom_resolution;
    int has_server;
    int is_empty;
} bc_version_featureStruct;

typedef struct bc_version_OSStruct {
    char name[64];
    char arch[64];
    char version[64];
    int is_empty;
} bc_version_OSStruct;

typedef struct bc_version_actionRule {
    char action[16];
    bc_version_featureStruct features;
    bc_version_OSStruct os;
} bc_version_actionRule;

typedef struct bc_version_argRule {
    bc_version_actionRule rules[128];
    char value[128][128];
    int rules_len;
    int value_len;
} bc_version_argRule;

typedef struct bc_version_library {
    struct {
        bc_version_downloadable artifact;
        bc_version_classifiersMap classifiers[128];
        int classifiers_len;
    } downloads;
    char name[128];
    char url[256];
    bc_version_nativeMap natives[128];
    bc_version_actionRule rules[128];
    int rules_len;
    int natives_len;
} bc_version_library;

typedef struct bc_version_arguments {
    bc_version_argRule jvm[128];
    bc_version_argRule game[128];
    int jvm_len;
    int game_len;
} bc_version_arguments;

typedef struct bc_version {
    char id[32];
    bc_version_arguments arguments;
    bc_version_assetIndexData assetIndex;
    bc_version_library libraries[128];
    int lib_len;
    char assets[32];
    struct {
        bc_version_downloadable client;
        bc_version_downloadable server;
        bc_version_downloadable windows_server;
        bc_version_downloadable client_mappings;
        bc_version_downloadable server_mappings;
    } downloads;
    bc_version_javaExec javaVersion;
    struct {
        struct {
            char type[128];
            char argument[128];
            struct {
                char id[128];
                // We build for C99, can't extend Downloadable
                char sha1[256];
                char url[256];
                int size;
            } file;
        } client;
    } logging;
    char mainClass[48];
    int minimumLauncherVersion;
    char releaseTime[32];
    char time[32];
    char type[16];
    char minecraftArguments[320];
    char inheritsFrom[32];
    char url[256];

    // Betacraft only
    char uuid[48]; // Defaults to 'id' if not specified
    int modern_server_parameters; // Whether to use --server and --port or not
} bc_version;

bc_version* bc_version_read_json(json_object* obj);

#endif
