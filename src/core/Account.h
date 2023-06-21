#ifndef BC_ACCOUNT_H
#define BC_ACCOUNT_H

extern char forbidden_accesstokens[128][2048];
extern int forbidden_accesstokens_size;
extern char forbidden_profileids[64][64];
extern int forbidden_profileids_size;

typedef enum bc_account_type {
    BC_ACCOUNT_UNAUTHENTICATED,
    BC_ACCOUNT_MICROSOFT,
    BC_ACCOUNT_MOJANG
} bc_account_type;

typedef struct bc_account {
    char uuid[64];
    char username[32];
    char access_token[2048];
    char refresh_token[2048];
    char minecraft_access_token[2048];
    bc_account_type account_type;
} bc_account;

typedef struct bc_account_array {
    bc_account arr[64];
    int len;
} bc_account_array;

void bc_account_create(const bc_account* account);
void bc_account_remove(const char* uuid);
void bc_account_update(const bc_account* account);
void bc_account_select(const char* uuid);
bc_account* bc_account_select_get();
bc_account* bc_account_get(const char* uuid);
bc_account_array* bc_account_list();
void bc_account_refresh();
void bc_account_register_forbidden_all();
void bc_account_register_forbidden(bc_account* account, int uuid);

#endif
