#ifndef BC_INSTANCE_H
#define BC_INSTANCE_H

#include <limits.h>
#include "Betacraft.h"

typedef struct bc_instance_group_name_array {
    char arr[128][32];
    int len;
} bc_instance_group_name_array;

typedef struct bc_instance {
    char name[64];
    char jvm_args[1024];
    char program_args[1024];
    char server_ip[128];
    char server_port[16];
    int join_server;
    char java_path[PATH_MAX];
    char path[PATH_MAX];
    char version[32];
    int width;
    int height;
    int maximized;
    int fullscreen;
    int show_log;
    int keep_open;
} bc_instance;

typedef struct bc_instance_group {
    char group_name[64];
    bc_instance instances[128];
    int len;
} bc_instance_group;

typedef struct bc_instance_group_array {
    bc_instance_group arr[128];
    int len;
} bc_instance_group_array;

typedef struct bc_instance_array {
    bc_instance arr[128];
    int len;
} bc_instance_array;

char* bc_instance_get_path(const char* instance_name);
void bc_instance_create(const char* name, const char* version, const char* group_name);
void bc_instance_group_create(const char* name);
void bc_instance_update(const bc_instance* instance);
void bc_instance_select(const char* path);
void bc_instance_move(bc_instance_array* standard, bc_instance_group_array* grouped, const char* instanceSelected);

void bc_instance_remove(const char* instance_name);
void bc_instance_remove_group(const char* name);

bc_instance* bc_instance_get(const char* instance_path);
bc_instance_array* bc_instance_get_all();
bc_instance* bc_instance_select_get();
bc_instance_group_array* bc_instance_group_get_all();
bc_instance_group_name_array* bc_instance_group_name_get_all();

void bc_instance_run(const char* server_ip, const char* server_port);
bc_progress bc_instance_run_progress();
int bc_instance_validate_name(const char* name);

#endif
