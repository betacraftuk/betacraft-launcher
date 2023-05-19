#ifndef BC_VERSIONLIST_H
#define BC_VERSIONLIST_H

#include <json-c/json_object.h>

typedef struct bc_versionlist_version {
    char id[32];
    char type[16];
    char url[256];
    char releaseTime[32];
} bc_versionlist_version;

typedef struct bc_versionlist {
    bc_versionlist_version versions[2048];
    int versions_len;
    struct {
        char release[16];
        char snapshot[16];
    } latest;
} bc_versionlist;

bc_versionlist* bc_versionlist_read_json(json_object* obj, json_object* bcList);
bc_versionlist* bc_versionlist_fetch();
bc_versionlist_version* bc_versionlist_find(const char* id);
int bc_versionlist_download(const char* gameVersion);

#endif
