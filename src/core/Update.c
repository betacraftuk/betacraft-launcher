#include "Update.h"
#include "FileSystem.h"
#include "Network.h"
#include "JsonExtension.h"
#include "Betacraft.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdbool.h>

#ifdef _WIN32
#include <direct.h>
#include <process.h>
#elif defined(__linux__) || defined(__APPLE__)
#include <unistd.h>
#endif

bc_github_release* bc_update_release_get() {
    bc_github_release* release = malloc(sizeof(bc_github_release));

    char* response = bc_network_get("https://api.github.com/repos/KazuOfficial/updatertest/releases/latest", "User-Agent: Betacraft");
    json_object* json = json_tokener_parse(response);
    json_object* tmp, * tmp_array;
    free(response);

    json_object_object_get_ex(json, "assets", &tmp);

    release->assets_length = json_object_array_length(tmp);

    for (int i = 0; i < release->assets_length; i++) {
        tmp_array = json_object_array_get_idx(tmp, i);
        snprintf(release->assets[i].name, sizeof(release->assets[i].name), "%s", jext_get_string_dummy(tmp_array, "name"));
        snprintf(release->assets[i].browser_download_url, sizeof(release->assets[i].browser_download_url), "%s", jext_get_string_dummy(tmp_array, "browser_download_url"));
    }

    snprintf(release->tag_name, sizeof(release->tag_name), "%s", jext_get_string_dummy(json, "tag_name"));

    json_object_put(json);

    return release;
}

void bc_update_perform() {
    bc_github_release* release = bc_update_release_get();

    if (strcmp(release->tag_name, BETACRAFT_VERSION) == 0) return;

    char* os = bc_file_os();

    for (int i = 0; i < release->assets_length; i++) {
        char* release_os = strtok(release->assets[i].name, "-");
        for (int y = 0; y < 2; y++) {
            release_os = strtok(NULL, "-");
        }

        int size = strlen(release_os) - strlen(".zip");
        release_os[size] = '\0';

        if (strcmp(release_os, os) == 0) {
            bc_network_download(release->assets[i].browser_download_url, "./betacraft-update.zip", true);
            make_path("temp", 0);
            bc_file_unzip("betacraft-update.zip", "temp/");

            free(release);
            free(os);

            chdir("temp");
            execl("Betacraft.exe", "Betacraft.exe", "-update", NULL);
        }
    }
}
