#ifndef BC_INSTANCE_H
#define BC_INSTANCE_H

#include "Betacraft.h"
#include <limits.h>
#include <stdbool.h>

#define BC_INSTANCE_NAME_MAX_SIZE 64
#define BC_INSTANCE_JVM_ARGS_SIZE 1024
#define BC_INSTANCE_PROGRAM_ARGS_SIZE 1024
#define BC_INSTANCE_SERVER_IP_SIZE 128
#define BC_INSTANCE_SERVER_PORT_SIZE 16
#define BC_INSTANCE_VERSION_SIZE 32

#define BC_INSTANCE_ARRAY_MAX_SIZE 128

#define BC_INSTANCE_GROUP_ARRAY_MAX_SIZE 128
#define BC_INSTANCE_GROUP_NAME_MAX_SIZE 64
#define BC_INSTANCE_GROUP_INSTANCES_MAX_SIZE 128

#define BC_INSTANCE_GROUP_NAME_ARRAY_MAX_SIZE 128
#define BC_INSTANCE_GROUP_NAME_ARRAY_NAME_MAX_SIZE 32

typedef struct bc_instance_group_name_array {
    char arr[BC_INSTANCE_GROUP_NAME_ARRAY_MAX_SIZE][BC_INSTANCE_GROUP_NAME_ARRAY_NAME_MAX_SIZE];
    size_t len;
} bc_instance_group_name_array;

typedef struct bc_instance {
    char name[BC_INSTANCE_NAME_MAX_SIZE];
    char jvm_args[BC_INSTANCE_JVM_ARGS_SIZE];
    char program_args[BC_INSTANCE_PROGRAM_ARGS_SIZE];
    char server_ip[BC_INSTANCE_SERVER_IP_SIZE];
    char server_port[BC_INSTANCE_SERVER_PORT_SIZE];
    int join_server;
    char java_path[PATH_MAX];
    char path[PATH_MAX];
    char version[BC_INSTANCE_VERSION_SIZE];
    int width;
    int height;
    int maximized;
    int fullscreen;
    int show_log;
    int keep_open;
} bc_instance;

typedef struct bc_instance_group {
    char group_name[BC_INSTANCE_GROUP_NAME_MAX_SIZE];
    bc_instance instances[BC_INSTANCE_GROUP_INSTANCES_MAX_SIZE];
    size_t len;
} bc_instance_group;

typedef struct bc_instance_group_array {
    bc_instance_group arr[BC_INSTANCE_GROUP_ARRAY_MAX_SIZE];
    size_t len;
} bc_instance_group_array;

typedef struct bc_instance_array {
    bc_instance arr[BC_INSTANCE_ARRAY_MAX_SIZE];
    size_t len;
} bc_instance_array;

char* bc_instance_get_path(const char* instance_name);
void bc_instance_create(const char* name, const char* version, const char* group_name);
void bc_instance_group_create(const char* name);
void bc_instance_update(const bc_instance* instance);
void bc_instance_select(const char* path);
void bc_instance_move(bc_instance_array* standard, bc_instance_group_array* grouped, const char* instanceSelected);

void bc_instance_remove(const char* instance_path);
void bc_instance_remove_group(const char* name);

bc_instance* bc_instance_get(const char* instance_path);
bc_instance_array* bc_instance_get_all();
bc_instance* bc_instance_select_get();
bc_instance_group_array* bc_instance_group_get_all();
bc_instance_group_name_array* bc_instance_group_name_get_all();

void bc_instance_run(const char* server_ip, const char* server_port);
bc_progress bc_instance_run_progress();
bool bc_instance_validate_name(const char* name);

#endif
