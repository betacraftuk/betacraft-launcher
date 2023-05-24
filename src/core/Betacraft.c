#include "Betacraft.h"
#include "JsonExtension.h"
#include "Settings.h"

#include <string.h>
#include <stdio.h>

int betacraft_online = 0;
char application_support_path[PATH_MAX] = "";
const char CRAFATAR_ENDPOINT[] = "https://crafatar.com/avatars/";
const char API_SERVER_LIST[] = "https://api.betacraft.uk/v2/server_list";

bc_server_array* bc_server_list() {
    bc_server_array* server_list = malloc(sizeof(bc_server_array));

    char* response = bc_network_get(API_SERVER_LIST, NULL);
    json_object* json = json_tokener_parse(response);
    json_object* tmp;

    free(response);

    server_list->len = json_object_array_length(json);

    for (int i = 0; i < server_list->len; i++) {
        tmp = json_object_array_get_idx(json, i);

        server_list->arr[i].icon = jext_get_string(tmp, "icon");
        snprintf(server_list->arr[i].name, sizeof(server_list->arr[i].name), "%s", jext_get_string_dummy(tmp, "name"));
        snprintf(server_list->arr[i].description, sizeof(server_list->arr[i].description), "%s", jext_get_string_dummy(tmp, "description"));
        snprintf(server_list->arr[i].software_name, sizeof(server_list->arr[i].software_name), "%s", jext_get_string_dummy(tmp, "software_name"));
        snprintf(server_list->arr[i].software_version, sizeof(server_list->arr[i].software_version), "%s", jext_get_string_dummy(tmp, "software_version"));
        snprintf(server_list->arr[i].player_names, sizeof(server_list->arr[i].player_names), "%s", jext_get_string_dummy(tmp, "players_names"));
        snprintf(server_list->arr[i].version_category, sizeof(server_list->arr[i].version_category), "%s", jext_get_string_dummy(tmp, "version_category"));
        snprintf(server_list->arr[i].connect_version, sizeof(server_list->arr[i].connect_version), "%s", jext_get_string_dummy(tmp, "connect_version"));
        snprintf(server_list->arr[i].connect_protocol, sizeof(server_list->arr[i].connect_protocol), "%s", jext_get_string_dummy(tmp, "connect_protocol"));
        snprintf(server_list->arr[i].connect_socket, sizeof(server_list->arr[i].connect_socket), "%s", jext_get_string_dummy(tmp, "connect_socket"));
        server_list->arr[i].is_public = jext_get_boolean(tmp, "is_public");
        server_list->arr[i].max_players = jext_get_int(tmp, "max_players");
        server_list->arr[i].online_players = jext_get_int(tmp, "online_players");
        server_list->arr[i].last_ping_time = jext_get_int(tmp, "last_ping_time");
        server_list->arr[i].connect_online_mode = jext_get_boolean(tmp, "connect_online_mode");
    }

    json_object_put(json);

    return server_list;
}

bc_memory bc_avatar_get(const char* uuid) {
    char endpoint[256];
    snprintf(endpoint, sizeof(endpoint), "%s%s?overlay", CRAFATAR_ENDPOINT, uuid);

    return bc_network_get_chunk(endpoint);
}

char* bc_update_check() {
    char* response = bc_network_get("https://api.github.com/repos/betacraftuk/betacraft-launcher/releases?per_page=1", "User-Agent: Betacraft");
    json_object* json = json_tokener_parse(response);
    free(response);

    json_object* latestRelease = json_object_array_get_idx(json, 0);
    char* out = NULL;

    if (strcmp(jext_get_string_dummy(latestRelease, "target_commitish"), "v2") != 0) {
        json_object_put(json);
        return out;
    }

    const char* tagName = jext_get_string_dummy(latestRelease, "tag_name");

    if (strcmp(tagName, BETACRAFT_VERSION) != 0) {
        out = malloc(strlen(tagName) + 1);
        strcpy(out, tagName);
    }

    json_object_put(json);

    return out;
}
