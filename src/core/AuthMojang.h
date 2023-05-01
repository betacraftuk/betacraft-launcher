#ifndef BC_AUTHMOJANG_H
#define BC_AUTHMOJANG_H

typedef struct AuthMojangResponse {
    char access_token[1024];
    char client_token[1024];
    char uuid[512];
    char username[32];
} AuthMojangResponse;

void bc_auth_mojang(const char* username, const char* password);
void bc_auth_mojang_refresh(const char* uuid);
void bc_auth_mojang_invalidate(const char* uuid);

#endif
