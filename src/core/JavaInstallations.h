#ifndef BC_JAVAINSTALLATIONS_H
#define BC_JAVAINSTALLATIONS_H

#include <limits.h>

typedef struct bc_jinst {
    char arch[16];
    char version[16];
    char path[PATH_MAX];
} bc_jinst;

typedef struct bc_jinst_array {
    bc_jinst arr[16];
    int len;
} bc_jinst_array;

typedef struct bc_jrepo_platform {
    char name[32];
    char arch[16];
    char hash[512];
    char url[256];
} bc_jrepo_platform;

typedef struct bc_jrepo {
    int version;
    char full_version[64];
    int platforms_len;
    bc_jrepo_platform platforms[8];
} bc_jrepo;

typedef struct bc_jrepo_array {
    bc_jrepo arr[32];
    int len;
} bc_jrepo_array;

typedef struct bc_jrepo_download {
    int version;
    char full_version[64];
    char url[256];
} bc_jrepo_download;

typedef struct bc_jrepo_download_array {
    bc_jrepo_download arr[32];
    int len;
} bc_jrepo_download_array;

char* bc_java_version(const char* path);
void bc_java_download(const char* url);

void bc_jinst_system_check();
void bc_jinst_add(const char* path);
void bc_jinst_remove(const char* uuid);
void bc_jinst_select(const char* uuid);

bc_jrepo_download_array* bc_jrepo_get_all_system();
bc_jinst_array* bc_jinst_get_all();
char* bc_jinst_select_get();
bc_jinst* bc_jinst_get(const char* uuid);
char* bc_jrepo_get_recommended(const char* gameVersion);
char* bc_jrepo_parse_version(const char* version);

#endif
