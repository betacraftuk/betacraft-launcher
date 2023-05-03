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
    bc_github_release* release = NULL;

    char* response = bc_network_get("https://api.github.com/repos/betacraftuk/betacraft-launcher/releases?per_page=1", "User-Agent: Betacraft");
    json_object* json = json_tokener_parse(response);
    json_object* tmp, * tmp_array;
    free(response);

    json_object* latestRelease = json_object_array_get_idx(json, 0);

    if (strcmp(jext_get_string_dummy(latestRelease, "target_commitish"), "v2") != 0) {
        json_object_put(json);
        return release;
    }

    json_object_object_get_ex(latestRelease, "assets", &tmp);

    release = malloc(sizeof(bc_github_release));
    release->assets_length = json_object_array_length(tmp);

    for (int i = 0; i < release->assets_length; i++) {
        tmp_array = json_object_array_get_idx(tmp, i);
        snprintf(release->assets[i].name, sizeof(release->assets[i].name), "%s", jext_get_string_dummy(tmp_array, "name"));
        snprintf(release->assets[i].browser_download_url, sizeof(release->assets[i].browser_download_url), "%s", jext_get_string_dummy(tmp_array, "browser_download_url"));
    }

    snprintf(release->tag_name, sizeof(release->tag_name), "%s", jext_get_string_dummy(latestRelease, "tag_name"));

    json_object_put(json);

    return release;
}

void bc_update_perform() {
    bc_github_release* release = bc_update_release_get();

    if (release == NULL)
        return;

    if (strcmp(release->tag_name, BETACRAFT_VERSION) == 0) {
        free(release);
        return;
    }

    char* os = bc_file_os();

    if (strcmp(os, "win64") != 0) {
        free(release);
        free(os);
        return;
    }

    for (int i = 0; i < release->assets_length; i++) {
        char* release_os = strtok(release->assets[i].name, "-");

        for (int j = 0; j < 3; j++) {
            release_os = strtok(NULL, "-");
        }

        int size = strlen(release_os) - 4; // get os
        release_os[size] = '\0';

        if (strcmp(release_os, os) == 0) {
#ifdef _WIN32
            bc_network_download(release->assets[i].browser_download_url, "./betacraft-update.zip", true);
            make_path("temp-update", 0);
            bc_file_unzip("betacraft-update.zip", "temp-update/");
            remove("betacraft-update.zip");

            free(release);
            free(os);

            chdir("temp-update/Betacraft");
            execl("Betacraft.exe", "Betacraft.exe", "-update", NULL);
#endif
        }
    }
}
