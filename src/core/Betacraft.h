#ifndef BC_BETACRAFT_H
#define BC_BETACRAFT_H

#include "Network.h"

#define BETACRAFT_VERSION "2.0.0-alpha.20230501"

extern int betacraft_online;

typedef struct bc_server {
    char name[64];
    char server_ip[64];
    char description[1024];
    char icon[256];
    char version[32];
    int online_players;
    int max_players;
} bc_server;

typedef struct bc_server_array {
    bc_server arr[256];
    int len;
} bc_server_array;

bc_server_array* bc_server_list();
bc_memory bc_avatar_get(const char* uuid);
void bc_translate(const char* key, char* out);
char* bc_update_check();

#endif
