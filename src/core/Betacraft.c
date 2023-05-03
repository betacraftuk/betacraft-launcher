#include "Betacraft.h"
#include "JsonExtension.h"
#include "Settings.h"

#include <string.h>
#include <stdio.h>

int betacraft_online = 0;
const char CRAFATAR_ENDPOINT[] = "https://crafatar.com/avatars/";

bc_server_array* bc_server_list() {
    bc_server_array* server_list = malloc(sizeof(bc_server_array));

    char* response = bc_network_get("https://servers.api.legacyminecraft.com/api/v1/getServers?type=all&icons=true", NULL);
    json_object* json = json_tokener_parse(response);
    json_object* arr, * tmp;

    free(response);

    json_object_object_get_ex(json, "servers", &arr);

    server_list->len = json_object_array_length(arr);

    for (int i = 0; i < server_list->len; i++) {
        tmp = json_object_array_get_idx(arr, i);

        snprintf(server_list->arr[i].name, sizeof(server_list->arr[i].name), "%s", jext_get_string_dummy(tmp, "serverName"));
        snprintf(server_list->arr[i].server_ip, sizeof(server_list->arr[i].server_ip), "%s", jext_get_string_dummy(tmp, "serverIP"));
        snprintf(server_list->arr[i].description, sizeof(server_list->arr[i].description), "%s", jext_get_string_dummy(tmp, "serverDescription"));
        snprintf(server_list->arr[i].icon, sizeof(server_list->arr[i].icon), "%s", jext_get_string_dummy(tmp, "serverIcon"));
        snprintf(server_list->arr[i].version, sizeof(server_list->arr[i].version), "%s", jext_get_string_dummy(tmp, "serverVersion"));

        server_list->arr[i].online_players = jext_get_int(tmp, "onlinePlayers");
        server_list->arr[i].max_players = jext_get_int(tmp, "maxPlayers");
    }

    json_object_put(json);

    return server_list;
}

bc_memory bc_avatar_get(const char* uuid) {
    char endpoint[256];
    snprintf(endpoint, sizeof(endpoint), "%s%s?overlay", CRAFATAR_ENDPOINT, uuid);

    return bc_network_get_chunk(endpoint);
}

void bc_translate(const char* key, char* out) {
    bc_settings* settings = bc_settings_get();

    char fileName[64];
    snprintf(fileName, sizeof(fileName), "lang/%s.json", settings->language);

    json_object* json = json_object_from_file(fileName);
    snprintf(out, 256, "%s", jext_get_string_dummy(json, key));

    json_object_put(json);
    free(settings);
}
