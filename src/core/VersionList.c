#include "VersionList.h"

#include "JsonExtension.h"
#include "Network.h"
#include "FileSystem.h"
#include "StringUtils.h"

#include <stdio.h>
#include <string.h>

void bc_versionlist_read_version_json(bc_versionlist_version* version, json_object* obj, int local) {
    snprintf(version->id, sizeof(version->id), "%s", jext_get_string_dummy(obj, "id"));
    snprintf(version->type, sizeof(version->type), "%s", jext_get_string_dummy(obj, "type"));
    snprintf(version->url, sizeof(version->url), "%s", jext_get_string_dummy(obj, "url"));
    snprintf(version->releaseTime, sizeof(version->releaseTime), "%s", jext_get_string_dummy(obj, "releaseTime"));
    version->local = local;
}

bc_versionlist* bc_versionlist_read_json(json_object* obj, json_object* bcList) {
    bc_versionlist* vl = malloc(sizeof(bc_versionlist));
    json_object* tmp;

    vl->versions_len = 0;

    json_object_object_get_ex(obj, "latest", &tmp);

    snprintf(vl->latest.release, sizeof(vl->latest.release), "%s", jext_get_string_dummy(tmp, "release"));
    snprintf(vl->latest.snapshot, sizeof(vl->latest.snapshot), "%s", jext_get_string_dummy(tmp, "snapshot"));

    json_object_object_get_ex(obj, "versions", &tmp);
    array_list* verArr = json_object_get_array(tmp);

    if (verArr == NULL) {
        return vl;
    }

    json_object* verObj;

    const char* trimAt = jext_get_string_dummy(bcList, "trim_at");
    int max = jext_get_string_array_index(tmp, "id", trimAt);

    json_object* bctmp;
    json_object_object_get_ex(bcList, "versions", &bctmp);

    array_list* bcarr = json_object_get_array(bctmp);
    int bc_len = bcarr->size;

    vl->versions_len = max + bc_len;

    // Official version list
    for (int i = 0; i < max; i++) {
        verObj = verArr->array[i];

        bc_versionlist_read_version_json(&vl->versions[i], verObj, 0);
    }

    // Betacraft version list
    for (int i = 0; i < bc_len; i++) {
        verObj = bcarr->array[i];

        bc_versionlist_read_version_json(&vl->versions[i + max], verObj, 0);
    }

    bc_file_list_array* filelist = bc_file_list("versions");
    int totalfound = 0;
    for (int i = 0; i < filelist->len; i++) {
        bc_file_list_array_dirent element = filelist->arr[i];
        if (!element.is_directory && str_ends_with(element.name, ".json")) {
            char* vername = strdup(element.name);
            int indexof = strstr(vername, ".json") - vername;
            vername[indexof] = '\0';

            int size = strlen(vername) + strlen("versions/.json") + 1;
            char* abspath = malloc(size);
            snprintf(abspath, size, "versions/%s.json", vername);
            json_object* verobj = json_object_from_file(abspath);

            bc_versionlist_read_version_json(&vl->versions[vl->versions_len + totalfound], verobj, 1);

            json_object_put(verobj);
            totalfound++;
        }
    }

    vl->versions_len += totalfound;

    json_object_put(verObj);

    return vl;
}

bc_versionlist* bc_versionlist_fetch() {
    char* response = bc_network_get("https://launchermeta.mojang.com/mc/game/version_manifest.json", NULL);
    json_object* obj = json_tokener_parse(response);
    free(response);

    if (obj == NULL) return NULL;

    response = bc_network_get("http://files.betacraft.uk/launcher/v2/assets/version_list.json", NULL);
    json_object* bcList = json_tokener_parse(response);
    free(response);

    return bc_versionlist_read_json(obj, bcList);
}

bc_versionlist_version* bc_versionlist_find(const char* id) {
    bc_versionlist* vl = bc_versionlist_fetch();

    for (int i = 0; i < vl->versions_len; i++) {
        if (strcmp(vl->versions[i].id, id) == 0) {
            bc_versionlist_version* version = malloc(sizeof(bc_versionlist_version));
            snprintf(version->id, sizeof(version->id), "%s", vl->versions[i].id);
            snprintf(version->url, sizeof(version->url), "%s", vl->versions[i].url);
            snprintf(version->releaseTime, sizeof(version->releaseTime), "%s", vl->versions[i].releaseTime);
            snprintf(version->type, sizeof(version->type), "%s", vl->versions[i].type);

            free(vl);
            return version;
        }
    }

    free(vl);
    return NULL;
}
