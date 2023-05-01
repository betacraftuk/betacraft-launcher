#include "AuthMojang.h"
#include "Network.h"
#include "JsonExtension.h"
#include "Account.h"

#include <json-c/json.h>
#include <string.h>
#include <stdio.h>

const char API_MOJANG_AUTH[] = "https://authserver.mojang.com/authenticate";
const char API_MOJANG_REFRESH[] = "https://authserver.mojang.com/refresh";
const char API_MOJANG_INVALIDATE[] = "https://authserver.mojang.com/invalidate";

void bc_auth_mojang(const char* username, const char* password)
{
    json_object* data = json_object_new_object();
    json_object* agent = json_object_new_object();
    json_object* tmp;

    json_object_object_add(agent, "name", json_object_new_string("Minecraft"));
    json_object_object_add(agent, "version", json_object_new_int(1));
    json_object_object_add(data, "agent", agent);
    json_object_object_add(data, "username", json_object_new_string(username));
    json_object_object_add(data, "password", json_object_new_string(password));

    char* response = bc_network_post(API_MOJANG_AUTH, json_object_to_json_string(data), "Content-Type: application/json");
    json_object* json = json_tokener_parse(response);
    free(response);

    json_object_object_get_ex(json, "selectedProfile", &tmp);

    if (tmp == NULL) return;

    AuthMojangResponse* res = malloc(sizeof(struct AuthMojangResponse));

    snprintf(res->access_token, sizeof(res->access_token), "%s", jext_get_string_dummy(json, "accessToken"));
    snprintf(res->client_token, sizeof(res->client_token), "%s", jext_get_string_dummy(json, "clientToken"));
    snprintf(res->username, sizeof(res->username), "%s", jext_get_string_dummy(tmp, "name"));
    snprintf(res->uuid, sizeof(res->uuid), "%s", jext_get_string_dummy(tmp, "id"));

    bc_account* account = bc_account_get(res->uuid);

    if (account == NULL) {
        bc_account* new_account = malloc(sizeof(bc_account));

        snprintf(new_account->username, sizeof(new_account->username), "%s", res->username);
        snprintf(new_account->uuid, sizeof(new_account->uuid), "%s", res->uuid);
        snprintf(new_account->access_token, sizeof(new_account->access_token), "%s", res->access_token);
        snprintf(new_account->refresh_token, sizeof(new_account->refresh_token), "%s", res->client_token);
        new_account->account_type = BC_ACCOUNT_MOJANG;

        bc_account_create(new_account);
        free(new_account);
    } else {
        snprintf(account->username, sizeof(account->username), "%s", res->username);
        snprintf(account->access_token, sizeof(account->access_token), "%s", res->access_token);
        snprintf(account->refresh_token, sizeof(account->refresh_token), "%s", res->client_token);

        bc_account_update(account);
        free(account);
    }

    bc_account_select(res->uuid);

    free(res);

    json_object_put(data);
    json_object_put(json);
}

void bc_auth_mojang_refresh(const char* uuid) {
    bc_account* account = bc_account_get(uuid);

    if (account != NULL) {
        json_object* data = json_object_new_object();
        json_object* tmp;

        json_object_object_add(data, "accessToken", json_object_new_string(account->access_token));
        json_object_object_add(data, "clientToken", json_object_new_string(account->refresh_token));

        char* response = bc_network_post(API_MOJANG_REFRESH, json_object_to_json_string(data), "Content-Type: application/json");
        json_object* json = json_tokener_parse(response);
        free(response);

        json_object_object_get_ex(json, "selectedProfile", &tmp);

        snprintf(account->username, sizeof(account->username), "%s", jext_get_string_dummy(tmp, "name"));
        snprintf(account->access_token, sizeof(account->access_token), "%s", jext_get_string_dummy(json, "accessToken"));
        snprintf(account->refresh_token, sizeof(account->refresh_token), "%s", jext_get_string_dummy(json, "clientToken"));
        bc_account_update(account);

        json_object_put(data);
        json_object_put(json);
        free(account);
    }
}

void bc_auth_mojang_invalidate(const char* uuid) {
    bc_account* account = bc_account_get(uuid);

    if (account != NULL)
    {
        json_object* data = json_object_new_object();

        json_object_object_add(data, "accessToken", json_object_new_string(account->access_token));
        json_object_object_add(data, "clientToken", json_object_new_string(account->refresh_token));

        char* res = bc_network_post(API_MOJANG_INVALIDATE, json_object_to_json_string(data), "Content-Type: application/json");

        if (strlen(res) == 0) { // if success
            bc_account_remove(uuid);
        }

        free(res);
        free(account);
        json_object_put(data);
    }
}
