#ifndef BC_BETACRAFT_H
#define BC_BETACRAFT_H

#include "Network.h"

#define BETACRAFT_VERSION "2.0.0-alpha.20230501"

extern int betacraft_online;

typedef struct bc_server {
    char* name;
    char* server_ip;
    char* description;
    char* icon;
    char* version;
    int online_players;
    int max_players;
} bc_server;

typedef struct bc_server_array {
    bc_server* arr;
    int len;
} bc_server_array;

bc_server_array* bc_server_list();
bc_memory bc_avatar_get(const char* uuid);
void bc_translate(const char* key, char* out);

#endif
