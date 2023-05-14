#include "Version.h"
#include "JsonExtension.h"
#include "StringUtils.h"
#include "Logger.h"
#include "Network.h"
#include "VersionList.h"
#include "FileSystem.h"
#include "Betacraft.h"

#include <assert.h>
#include <stdio.h>
#include <string.h>
#include <json-c/json.h>

bc_version_actionRule* bc_version_read_rule(json_object* obj) {
    bc_version_actionRule* rule = malloc(sizeof(bc_version_actionRule));
    json_object* tmp;

    if (json_object_object_get_ex(obj, "action", &tmp) == 1) {
        snprintf(rule->action, sizeof(rule->action), "%s", json_object_get_string(tmp));
    } else {
        strcpy(rule->action, "allow");
    }

    if (json_object_object_get_ex(obj, "features", &tmp) == 1) {
        rule->features.is_empty = 0;
        rule->features.has_custom_resolution = jext_get_boolean(tmp, "has_custom_resolution");
        rule->features.is_demo_user = jext_get_boolean(tmp, "is_demo_user");
        rule->features.has_server = jext_get_boolean(tmp, "has_server");
    } else {
        rule->features.is_empty = 1;
    }

    if (json_object_object_get_ex(obj, "os", &tmp) == 1) {
        rule->os.is_empty = 0;
        snprintf(rule->os.arch, sizeof(rule->os.arch), "%s", jext_get_string_dummy(tmp, "arch"));
        snprintf(rule->os.name, sizeof(rule->os.name), "%s", jext_get_string_dummy(tmp, "name"));
        snprintf(rule->os.version, sizeof(rule->os.version), "%s", jext_get_string_dummy(tmp, "version"));
    } else {
        rule->os.is_empty = 1;
    }

    return rule;
}

void bc_version_read_rule_all(bc_version_actionRule* rules, json_object* obj) {
    array_list* rulesJson = json_object_get_array(obj);

    for (int j = 0; j < rulesJson->size; j++) {
        rules[j] = *(bc_version_read_rule(rulesJson->array[j]));
    }
}

void bc_version_read_arg_rule(bc_version_argRule* rule, json_object* arg_rule) {
    if (json_object_is_type(arg_rule, json_type_string) == 1) {
        snprintf(rule->value[0], sizeof(rule->value[0]), "%s", json_object_get_string(arg_rule));
        rule->value_len = 0;
        rule->rules_len = 0;
    } else {
        json_object* valueJson, * rulesJson;

        if (json_object_object_get_ex(arg_rule, "value", &valueJson) == 1) {
            if (json_object_is_type(valueJson, json_type_string) == 1) {
                snprintf(rule->value[0], sizeof(rule->value[0]), "%s", json_object_get_string(valueJson));
                rule->value_len = 0;
            } else if (json_object_is_type(valueJson, json_type_array) == 1) {
                rule->value_len = json_object_array_length(valueJson);

                int len = json_object_array_length(valueJson);

                json_object* tmp;
                for (int i = 0; i < len; i++) {
                    tmp = json_object_array_get_idx(valueJson, i);

                    snprintf(rule->value[i], sizeof(rule->value[i]), "%s", json_object_get_string(tmp));
                }
            }
        }

        if (json_object_object_get_ex(arg_rule, "rules", &rulesJson) == 1) {
            rule->rules_len = json_object_array_length(rulesJson);
            bc_version_read_rule_all(rule->rules, rulesJson);
        }
    }
}

void bc_version_read_json_asset_index(json_object* obj, bc_version* v) {
    json_object* tmp;

    if (json_object_object_get_ex(obj, "assetIndex", &tmp) == 1) {
        snprintf(v->assetIndex.id, sizeof(v->assetIndex.id), "%s", jext_get_string_dummy(tmp, "id"));
        snprintf(v->assetIndex.sha1, sizeof(v->assetIndex.sha1), "%s", jext_get_string_dummy(tmp, "sha1"));
        snprintf(v->assetIndex.url, sizeof(v->assetIndex.url), "%s", jext_get_string_dummy(tmp, "url"));
        v->assetIndex.size = jext_get_int(tmp, "size");
        v->assetIndex.totalSize = jext_get_int(tmp, "totalSize");
    }
}

void bc_version_read_json_downloads(json_object* obj, const char* value, bc_version_downloadable* dw) {
    json_object* tmp;

    if (json_object_object_get_ex(obj, value, &tmp) == 1) {
        snprintf(dw->sha1, sizeof(dw->sha1), "%s", jext_get_string_dummy(tmp, "sha1"));
        snprintf(dw->url, sizeof(dw->url), "%s", jext_get_string_dummy(tmp, "url"));
        dw->size = jext_get_int(tmp, "size");
    }
}

void bc_version_read_json_java_version(json_object* obj, bc_version* v) {
    json_object* tmp;

    if (json_object_object_get_ex(obj, "javaVersion", &tmp) == 1) {
        snprintf(v->javaVersion.component, sizeof(v->javaVersion.component), "%s", jext_get_string_dummy(tmp, "component"));
        v->javaVersion.majorVersion = jext_get_int(tmp, "majorVersion");

        // Betacraft exclusive
        v->javaVersion.advisedMaxVersion = jext_get_int(tmp, "advisedMaxVersion");
        v->javaVersion.minVersion = jext_get_int(tmp, "minVersion");
    }
}

void bc_version_read_json_betacraft(json_object* obj, bc_version* v) {
    json_object* tmp;

    // Betacraft exclusive
    if (json_object_object_get_ex(obj, "modern_server_parameters", &tmp) == 1) {
        v->modern_server_parameters = jext_get_boolean(obj, "modern_server_parameters");
    } else {
        v->modern_server_parameters = 1;
    }

    // Betacraft exclusive
    if (json_object_object_get_ex(obj, "uuid", &tmp) == 1) {
        snprintf(v->uuid, sizeof(v->uuid), "%s", json_object_get_string(tmp));
    } else {
        snprintf(v->uuid, sizeof(v->uuid), "%s", v->id);
    }
}

void bc_game_version_json_read_logging(json_object* obj, bc_version* v) {
    json_object* tmp;

    if (json_object_object_get_ex(obj, "logging", &tmp) == 1) {
        if (json_object_object_get_ex(tmp, "client", &tmp) == 1) {
            snprintf(v->logging.client.type, sizeof(v->logging.client.type), "%s", jext_get_string_dummy(tmp, "type"));
            snprintf(v->logging.client.argument, sizeof(v->logging.client.argument), "%s", jext_get_string_dummy(tmp, "argument"));

            if (json_object_object_get_ex(tmp, "file", &tmp) == 1) {
                snprintf(v->logging.client.file.id, sizeof(v->logging.client.file.id), "%s", jext_get_string_dummy(tmp, "id"));
                snprintf(v->logging.client.file.sha1, sizeof(v->logging.client.file.sha1), "%s", jext_get_string_dummy(tmp, "sha1"));
                snprintf(v->logging.client.file.url, sizeof(v->logging.client.file.url), "%s", jext_get_string_dummy(tmp, "url"));
                v->logging.client.file.size = jext_get_int(tmp, "size");
            }
        }
    }
}

void bc_game_version_json_read_minecraft_arguments(json_object* tmp, bc_version* v) {
    char* mcargs = jext_alloc_string(tmp);
    char** split = split_str(mcargs, " ");

    char* alterable = strdup(mcargs);
    int size = count_substring(alterable, ' ') + 1 + /* width, height */ 4;
    free(alterable);

    v->arguments.game_len = 1;
    v->arguments.game->rules_len = 0;
    v->arguments.game->value_len = size;

    // minecraftArguments *never* contains width and height.
    strcpy(v->arguments.game->value[0], "--width");
    strcpy(v->arguments.game->value[1], "${resolution_width}");
    strcpy(v->arguments.game->value[2], "--height");
    strcpy(v->arguments.game->value[3], "${resolution_height}");

    for (int i = 4; i < size; i++) {
        snprintf(v->arguments.game->value[i], sizeof(v->arguments.game->value[i]), "%s", split[i - 4]);
    }

    free(split);

    v->arguments.jvm_len = 1;
    v->arguments.jvm->rules_len = 0;

    strcpy(v->arguments.jvm->value[0], "-Djava.library.path=${natives_directory}");
    strcpy(v->arguments.jvm->value[1], "-Dminecraft.launcher.brand=${launcher_name}");
    strcpy(v->arguments.jvm->value[2], "-Dminecraft.launcher.version=${launcher_version}");
    strcpy(v->arguments.jvm->value[3], "-cp");
    strcpy(v->arguments.jvm->value[4], "${classpath}");

    v->arguments.jvm->value_len = 5;
}

void bc_game_version_read_arguments(json_object* tmp, bc_version* v) {
    json_object* innerObj;

    if (json_object_object_get_ex(tmp, "game", &innerObj) == 1) {
        array_list* gameArr = json_object_get_array(innerObj);
        int arrlen = json_object_array_length(innerObj);

        int base = v->arguments.game_len;
        v->arguments.game_len += arrlen;

        for (int i = 0; i < arrlen; i++) {
            json_object* gameobj = gameArr->array[i];

            bc_version_read_arg_rule(&v->arguments.game[base + i], gameobj);
        }
    }

    if (json_object_object_get_ex(tmp, "jvm", &innerObj) == 1) {
        array_list* jvmArr = json_object_get_array(innerObj);
        int arrlen = json_object_array_length(innerObj);

        int base = v->arguments.jvm_len;
        v->arguments.jvm_len += arrlen;

        for (int i = 0; i < arrlen; i++) {
            json_object* jvmobj = jvmArr->array[i];

            bc_version_read_arg_rule(&v->arguments.jvm[base + i], jvmobj);
        }
    }
}

void bc_game_version_read_lib_rules(json_object* obj, bc_version* v, int i) {
    json_object* tmp;

    if (json_object_object_get_ex(obj, "rules", &tmp) == 1) {
        bc_version_read_rule_all(v->libraries[i].rules, tmp);

        array_list* rulesJson = json_object_get_array(tmp);
        v->libraries[i].rules_len = rulesJson->size;
    } else {
        v->libraries[i].rules_len = 0;
    }
}

void bc_game_version_read_lib_natives(json_object* obj, bc_version* v, int i) {
    json_object* tmp;

    if (json_object_object_get_ex(obj, "natives", &tmp) == 1) {
        struct json_object_iterator itBegin = json_object_iter_begin(tmp);
        struct json_object_iterator itEnd = json_object_iter_end(tmp);

        int size = json_object_object_length(tmp);
        v->libraries[i].natives_len = size;

        int pos = 0;
        while (!json_object_iter_equal(&itBegin, &itEnd)) {
            bc_version_nativeMap* nativeMap = &v->libraries[i].natives[pos];

            snprintf(nativeMap->os, sizeof(nativeMap->os), "%s", json_object_iter_peek_name(&itBegin));

            json_object* val = json_object_iter_peek_value(&itBegin);

            snprintf(nativeMap->classifierId, sizeof(nativeMap->classifierId), "%s", json_object_get_string(val));

            pos++;
            json_object_iter_next(&itBegin);
        }
    } else {
        v->libraries[i].natives_len = 0;
    }
}

void bc_game_version_read_lib_dw_artifact(json_object* obj, bc_version* v, int i) {
    json_object* artifactJson;

    if (json_object_object_get_ex(obj, "artifact", &artifactJson) == 1) {
        bc_version_downloadable* downloadable = &v->libraries[i].downloads.artifact;

        snprintf(downloadable->sha1, sizeof(downloadable->sha1), "%s", jext_get_string_dummy(artifactJson, "sha1"));
        snprintf(downloadable->url, sizeof(downloadable->url), "%s", jext_get_string_dummy(artifactJson, "url"));
        downloadable->size = jext_get_int(artifactJson, "size");
    } else {
        v->libraries[i].downloads.artifact.size = 0;
    }
}

void bc_game_version_read_lib_dw_classifiers(json_object* obj, bc_version* v, int i) {
    json_object* classifiersJson;
    if (json_object_object_get_ex(obj, "classifiers", &classifiersJson) == 1) {
        struct json_object_iterator itBegin = json_object_iter_begin(classifiersJson);
        struct json_object_iterator itEnd = json_object_iter_end(classifiersJson);

        int size = json_object_object_length(classifiersJson);
        v->libraries[i].downloads.classifiers_len = size;

        int pos = 0;
        while (!json_object_iter_equal(&itBegin, &itEnd)) {
            const char* key = json_object_iter_peek_name(&itBegin);
            bc_version_classifiersMap* classifiersMap = &v->libraries[i].downloads.classifiers[pos];

            snprintf(classifiersMap->id, sizeof(classifiersMap->id), "%s", key);

            json_object* dlobj = json_object_iter_peek_value(&itBegin);

            snprintf(classifiersMap->object.sha1, sizeof(classifiersMap->object.sha1), "%s", jext_get_string_dummy(dlobj, "sha1"));
            snprintf(classifiersMap->object.url, sizeof(classifiersMap->object.url), "%s", jext_get_string_dummy(dlobj, "url"));
            classifiersMap->object.size = jext_get_int(dlobj, "size");

            pos++;

            json_object_iter_next(&itBegin);
        }
    } else {
        v->libraries[i].downloads.classifiers_len = 0;
    }
}

void bc_game_version_read_lib_list(json_object* obj, bc_version* v) {
    json_object* tmp;

    if (json_object_object_get_ex(obj, "libraries", &tmp) == 1) {
        array_list* libArr = json_object_get_array(tmp);

        int base = v->lib_len;
        v->lib_len += libArr->size;

        for (int i = 0; i < libArr->size; i++) {
            json_object* libraryobj = libArr->array[i];

            snprintf(v->libraries[base + i].name, sizeof(v->libraries[base + i].name), "%s", jext_get_string_dummy(libraryobj, "name"));
            snprintf(v->libraries[base + i].url, sizeof(v->libraries[base + i].url), "%s", jext_get_string_dummy(libraryobj, "url"));

            json_object* temp;
            bc_game_version_read_lib_rules(libraryobj, v, base + i);
            bc_game_version_read_lib_natives(libraryobj, v, base + i);

            if (json_object_object_get_ex(libraryobj, "downloads", &temp) == 1) {
                bc_game_version_read_lib_dw_artifact(temp, v, base + i);
                bc_game_version_read_lib_dw_classifiers(temp, v, base + i);
            }
        }
    }
}

void bc_version_read_int_if_exists(int* ipointer, json_object* json, char* key) {
    json_object* tmp;
    if (json_object_object_get_ex(json, key, &tmp)) {
        *ipointer = json_object_get_int(tmp);
    }
}

void bc_version_read_string_if_exists(char* spointer, json_object* json, char* key) {
    json_object* tmp;
    if (json_object_object_get_ex(json, key, &tmp)) {
        snprintf(spointer, json_object_get_string_len(tmp) + 1, "%s", json_object_get_string(tmp));
    }
}

void bc_version_read_arguments_partial(bc_version* v, json_object* obj) {
    json_object* tmp;

    if (json_object_object_get_ex(obj, "arguments", &tmp) == 1) {
        bc_game_version_read_arguments(tmp, v);
        v->usesMinecraftArguments = 0;
    } else if (json_object_object_get_ex(obj, "minecraftArguments", &tmp) == 1) {

        // in case of inheritsFrom, `minecraftArguments` overwrites values of the inheritee,
        // so we can't let the base version's arguments take priority
        if (!v->usesMinecraftArguments) {
            v->arguments.game_len = 0;
            v->arguments.jvm_len = 0;

            bc_game_version_json_read_minecraft_arguments(tmp, v);
        }

        v->usesMinecraftArguments = 1;
    }
}

void bc_version_read_inherit_partial(bc_version* v, json_object* obj) {
    json_object* tmp, *downloads;
    json_object_object_get_ex(obj, "downloads", &downloads);

    bc_version_read_json_betacraft(obj, v);
    bc_version_read_json_asset_index(obj, v);
    bc_version_read_json_downloads(downloads, "client", &v->downloads.client);
    bc_version_read_json_downloads(downloads, "server", &v->downloads.server);
    bc_version_read_json_downloads(downloads, "windows_server", &v->downloads.windows_server);
    bc_version_read_json_downloads(downloads, "client_mappings", &v->downloads.client_mappings);
    bc_version_read_json_downloads(downloads, "server_mappings", &v->downloads.server_mappings);

    bc_version_read_json_java_version(obj, v);
    bc_version_read_string_if_exists(v->assets, obj, "assets");

    bc_version_read_string_if_exists(v->id, obj, "id");
    bc_version_read_string_if_exists(v->type, obj, "type");
    bc_version_read_string_if_exists(v->mainClass, obj, "mainClass");
    bc_version_read_string_if_exists(v->releaseTime, obj, "releaseTime");
    bc_version_read_string_if_exists(v->time, obj, "time");
    bc_version_read_int_if_exists(&v->minimumLauncherVersion, obj, "minimumLauncherVersion");
    bc_game_version_read_lib_list(obj, v);

    bc_game_version_json_read_logging(obj, v);
}

json_object* bc_version_get_json(char* ver_name) {
    int size = strlen(ver_name) + strlen("versions/.json") + 1;
    char* jsonLoc = malloc(size);
    snprintf(jsonLoc, size, "versions/%s.json", ver_name);

    if (betacraft_online == 1 && !bc_file_exists(jsonLoc)) {
        bc_versionlist_version* version = bc_versionlist_find(ver_name);

        if (version == NULL) {
            return NULL;
        }

        bc_network_download(version->url, jsonLoc, 1);
        free(version);
    }

    return json_object_from_file(jsonLoc);
}

bc_version* bc_version_read_json(char* ver_name) {

    bc_version* v = malloc(sizeof(bc_version));
    v->lib_len = 0;
    v->arguments.game_len = 0;
    v->arguments.jvm_len = 0;

    json_object* obj = bc_version_get_json(ver_name);

    v->usesMinecraftArguments = 0;
    bc_version_read_arguments_partial(v, obj);

    json_object* tmp;
    if (json_object_object_get_ex(obj, "inheritsFrom", &tmp)) {
        snprintf(v->inheritsFrom, sizeof(v->inheritsFrom), "%s", jext_alloc_string(tmp));

        // if the version inherits from another version, read that one first
        if (strcmp(v->inheritsFrom, "") != 0) {
            json_object* basever = bc_version_get_json(v->inheritsFrom);
            assert(basever != NULL);

            bc_version_read_inherit_partial(v, basever);
            bc_version_read_arguments_partial(v, basever);
            json_object_put(basever);
        }
    }

    bc_version_read_inherit_partial(v, obj);

    json_object_put(obj);

    return v;
}
