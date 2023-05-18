#ifndef BC_BETACRAFT_H
#define BC_BETACRAFT_H

#include "Network.h"

#define BETACRAFT_VERSION "2.0.0-alpha.20230501"

extern int betacraft_online;

typedef struct bc_server {
    char name[64];
    char description[1024];
    char* icon;
    int is_public;
    char software_name[128];
    char software_version[64];
    int max_players;
    int online_players;
    char player_names[4096];
    int last_ping_time;
    char version_category[128];
    char connect_version[64];
    char connect_protocol[64];
    char connect_socket[64];
    int connect_online_mode;
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
