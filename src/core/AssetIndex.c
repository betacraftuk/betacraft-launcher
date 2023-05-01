#include "AssetIndex.h"

#include "FileSystem.h"
#include "JsonExtension.h"
#include "Network.h"
#include "Betacraft.h"

#include <stdio.h>
#include <string.h>
#include <limits.h>

#ifdef __linux__
#include <pwd.h>
#include <unistd.h>
#endif

bc_assetindex* bc_assetindex_read_objects(const char* responseData) {
    json_object* responseJson = json_tokener_parse(responseData);
    bc_assetindex* index = malloc(sizeof(bc_assetindex));

    json_object* assetArray;
    if (json_object_object_get_ex(responseJson, "objects", &assetArray) == 1) {
        struct json_object_iterator itBegin = json_object_iter_begin(assetArray);
        struct json_object_iterator itEnd = json_object_iter_end(assetArray);

        index->len = json_object_object_length(assetArray);
        index->objects = malloc(index->len * sizeof(bc_assetindex_asset));

        int pos = 0;
        while (!json_object_iter_equal(&itBegin, &itEnd)) {
            snprintf(index->objects[pos].objectid, sizeof(index->objects[pos].objectid), "%s", json_object_iter_peek_name(&itBegin));

            json_object* assetObject = json_object_iter_peek_value(&itBegin);

            snprintf(index->objects[pos].hash, sizeof(index->objects[pos].hash), "%s", jext_get_string_dummy(assetObject, "hash"));
            index->objects[pos].size = jext_get_int(assetObject, "size");
            // betacraft exclusive
            snprintf(index->objects[pos].baseUrl, sizeof(index->objects[pos].baseUrl), "%s", jext_get_string_dummy(assetObject, "custom_url"));

            pos++;
            json_object_iter_next(&itBegin);
        }
    }

    index->virtual = jext_get_boolean(responseJson, "virtual");
    index->mapToResources = jext_get_boolean(responseJson, "mapToResources");

    return index;
}

bc_assetindex* bc_assetindex_load(bc_version_assetIndexData* data) {
    char* assetsData;
    char* location;
    char jsonLoc[PATH_MAX];

    json_object* json = NULL;

#ifdef __APPLE__
    location = bc_file_directory_get_working();

    snprintf(jsonLoc, sizeof(jsonLoc), "%s/minecraft/assets/indexes/", location);
#else
#ifdef _WIN32
    location = getenv("APPDATA");
#elif __linux__
    struct passwd* pw = getpwuid(getuid());
    location = pw->pw_dir;
#endif
    snprintf(jsonLoc, sizeof(jsonLoc), "%s/.minecraft/assets/indexes/", location);
#endif

    make_path(jsonLoc, 0);

    snprintf(jsonLoc, sizeof(jsonLoc), "%s%s.json", jsonLoc, data->id);

    if (betacraft_online == 1
        && (!bc_file_exists(jsonLoc)
            || bc_file_size(jsonLoc) != data->size)) {

        assetsData = bc_network_get(data->url, NULL);
        bc_file_create(jsonLoc, assetsData);
    } else {
        json = json_object_from_file(jsonLoc);
        assetsData = json_object_to_json_string(json);
    }

    bc_assetindex* assetIndex = bc_assetindex_read_objects(assetsData);

    if (json != NULL) {
        json_object_put(json);
    } else {
        free(assetsData);
    }

    return assetIndex;
}
