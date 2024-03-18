#ifndef BC_MOD_H
#define BC_MOD_H

#include <limits.h>

typedef struct bc_mod_version {
    int id;
    int modid;
    char name[64];
    char version[32];
    char game_version[32];
    char download_url[256];
    char client_classes[128];
    char minecraft_dir[128];
    char resources_dir[128];
    char server_classes[128];
    char path[PATH_MAX];
    int requirements_len;
    struct bc_mod_version *requirements;
} bc_mod_version;

typedef struct bc_mod_version_array {
    int len;
    bc_mod_version arr[128];
} bc_mod_version_array;

typedef struct bc_mod {
    int id;
    char name[64];
    char description[512];
    bc_mod_version_array versions;
} bc_mod;

typedef struct bc_mod_array {
    bc_mod arr[512];
    int len;
} bc_mod_array;

bc_mod_array *bc_mod_list(const char *version);
void bc_mod_download(bc_mod_version *version, const char *instance_path,
                     const char *game_version);
bc_mod_version_array *bc_mod_list_installed(const char *instance_path);
void bc_mod_list_remove(const char *instance_path, const char *mod_path);
void bc_mod_list_installed_move(bc_mod_version_array *mods,
                                const char *instance_path);
void bc_mod_add(const char *mod_path, const char *instance_path,
                const char *game_version);
void bc_mod_replace_jar(const char *jar_path, const char *instance_path,
                        const char *game_version);
void bc_mod_install(bc_mod_version_array *installed, const char *instance_dir,
                    const char *game_version);

#endif
