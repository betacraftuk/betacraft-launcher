#include "Instance.h"
#include "FileSystem.h"
#include "JsonExtension.h"
#include "VersionList.h"
#include "Game.h"
#include "JavaInstallations.h"
#include "Network.h"
#include "Settings.h"
#include "Version.h"
#include "Logger.h"
#include "Mod.h"
#include "Betacraft.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char* bc_instance_get_path(const char* instance_name) {
    char path[PATH_MAX];
    snprintf(path, sizeof(path), "instances/%s/bc_instance.json", instance_name);

    return bc_file_make_absolute_path(path);
}

void bc_instance_fill_object_from_json(bc_instance* instance, const char* instance_path, json_object* json) {
    snprintf(instance->name, sizeof(instance->name), "%s", jext_get_string_dummy(json, "name"));
    snprintf(instance->version, sizeof(instance->version), "%s", jext_get_string_dummy(json, "version"));
    snprintf(instance->jvm_args, sizeof(instance->jvm_args), "%s", jext_get_string_dummy(json, "jvm_args"));
    snprintf(instance->program_args, sizeof(instance->program_args), "%s", jext_get_string_dummy(json, "program_args"));
    snprintf(instance->server_ip, sizeof(instance->server_ip), "%s", jext_get_string_dummy(json, "server_ip"));
    snprintf(instance->server_port, sizeof(instance->server_port), "%s", jext_get_string_dummy(json, "server_port"));

    instance->height = jext_get_int(json, "height");
    instance->width = jext_get_int(json, "width");
    instance->fullscreen = jext_get_int(json, "fullscreen");
    instance->show_log = jext_get_int(json, "show_log");

    char* absPath = bc_file_absolute_path(instance_path);
    snprintf(instance->path, sizeof(instance->path), "%s", absPath);
    free(absPath);
}

json_object* bc_instance_create_default_config(const char* name, const char* version) {
    json_object* config = json_object_new_object();

    json_object_object_add(config, "name", json_object_new_string(name));
    json_object_object_add(config, "version", json_object_new_string(version));
    json_object_object_add(config, "jvm_args", json_object_new_string("-Xmx1G\n-XX:HeapDumpPath=java.exe_minecraft.exe.heapdump"));
    json_object_object_add(config, "program_args", json_object_new_string(""));
    json_object_object_add(config, "height", json_object_new_int(480));
    json_object_object_add(config, "width", json_object_new_int(854));
    json_object_object_add(config, "fullscreen", json_object_new_boolean(0));
    json_object_object_add(config, "show_log", json_object_new_boolean(0));
    json_object_object_add(config, "server_ip", json_object_new_string(""));
    json_object_object_add(config, "server_port", json_object_new_string(""));
    json_object_object_add(config, "mods", json_object_new_array());

    return config;
}

json_object* bc_instance_create_config(const bc_instance* instance) {
    json_object* json = json_object_from_file(instance->path);
    json_object* tmp;

    json_object_object_get_ex(json, "name", &tmp);
    json_object_set_string(tmp, instance->name);

    json_object_object_get_ex(json, "version", &tmp);
    json_object_set_string(tmp, instance->version);

    json_object_object_get_ex(json, "jvm_args", &tmp);
    json_object_set_string(tmp, instance->jvm_args);

    json_object_object_get_ex(json, "program_args", &tmp);
    json_object_set_string(tmp, instance->program_args);

    json_object_object_get_ex(json, "height", &tmp);
    json_object_set_int(tmp, instance->height);

    json_object_object_get_ex(json, "width", &tmp);
    json_object_set_int(tmp, instance->width);

    json_object_object_get_ex(json, "fullscreen", &tmp);
    json_object_set_boolean(tmp, instance->fullscreen);

    json_object_object_get_ex(json, "show_log", &tmp);
    json_object_set_boolean(tmp, instance->show_log);

    return json;
}

void bc_instance_group_create(const char* name) {
    json_object* settings = json_object_from_file("settings.json");
    json_object* tmp, * instance_tmp;
    json_object* group_object = json_object_new_object();

    json_object_object_get_ex(settings, "instance", &instance_tmp);
    json_object_object_get_ex(instance_tmp, "grouped", &tmp);

    json_object_object_add(group_object, "group_name", json_object_new_string(name));
    json_object_object_add(group_object, "instances", json_object_new_array());
    json_object_array_add(tmp, group_object);

    jext_file_write("settings.json", settings);
    json_object_put(settings);
}

void bc_instance_move(bc_instance_array* standard, bc_instance_group_array* grouped, const char* instanceSelected) {
    json_object* json = json_object_from_file("settings.json");
    json_object* instance = json_object_new_object();
    json_object* tmp, * tmpArr, * tmpArrGrouped;

    json_object_object_del(json, "instance");

    json_object_object_add(instance, "selected", json_object_new_string(instanceSelected));
    json_object_object_add(instance, "standalone", json_object_new_array());
    json_object_object_add(instance, "grouped", json_object_new_array());

    json_object_object_add(json, "instance", instance);

    json_object_object_get_ex(json, "instance", &tmp);
    json_object_object_get_ex(tmp, "standalone", &tmpArr);

    for (int i = 0; i < standard->len; i++) {
        json_object_array_add(tmpArr, json_object_new_string(standard->arr[i].path));
    }

    json_object_object_get_ex(tmp, "grouped", &tmpArr);

    for (int i = 0; i < grouped->len; i++) {
        json_object* group = json_object_new_object();
        json_object_object_add(group, "group_name", json_object_new_string(grouped->arr[i].group_name));
        json_object_object_add(group, "instances", json_object_new_array());

        json_object_object_get_ex(group, "instances", &tmpArrGrouped);

        for (int j = 0; j < grouped->arr[i].len; j++) {
            json_object_array_add(tmpArrGrouped, json_object_new_string(grouped->arr[i].instances[j].path));
        }

        json_object_array_add(tmpArr, group);
    }

    jext_file_write("settings.json", json);
    json_object_put(json);
}

bc_instance_group_name_array* bc_instance_group_name_get_all() {
    bc_instance_group_name_array* group_array = malloc(sizeof(bc_instance_group_name_array));

    json_object* settings = json_object_from_file("settings.json");

    if (settings == NULL) {
        group_array->len = 0;
        json_object_put(settings);
        return group_array;
    }

    json_object* tmp, * instance_tmp, * group_tmp;
    json_object_object_get_ex(settings, "instance", &instance_tmp);
    json_object_object_get_ex(instance_tmp, "grouped", &tmp);

    group_array->len = json_object_array_length(tmp);

    for (int i = 0; i < group_array->len; i++) {
        group_tmp = json_object_array_get_idx(tmp, i);
        snprintf(group_array->arr[i], sizeof(group_array->arr[i]), "%s", jext_get_string_dummy(group_tmp, "group_name"));
    }

    json_object_put(settings);

    return group_array;
}

void bc_instance_update_settings(const char* instance_path, const char* group_name) {
    json_object* settings = json_object_from_file("settings.json");
    json_object* instance_tmp, * tmp, * val_tmp, * val_arr_tmp;

    json_object_object_get_ex(settings, "instance", &instance_tmp);

    if (group_name == NULL) {
        json_object_object_get_ex(instance_tmp, "standalone", &tmp);
        json_object_array_add(tmp, json_object_new_string(instance_path));
    } else {
        json_object_object_get_ex(instance_tmp, "grouped", &tmp);

        for (int i = 0; i < json_object_array_length(tmp); i++) {
            val_tmp = json_object_array_get_idx(tmp, i);

            if (strcmp(jext_get_string_dummy(val_tmp, "group_name"), group_name) == 0) {
                json_object_object_get_ex(val_tmp, "instances", &val_arr_tmp);
                json_object_array_add(val_arr_tmp, json_object_new_string(instance_path));

                break;
            }
        }
    }

    jext_file_write("settings.json", settings);
    json_object_put(settings);
}

bc_instance* bc_instance_select_get() {
    bc_instance* instance_selected = NULL;

    json_object* json = json_object_from_file("settings.json");
    json_object* tmp;

    json_object_object_get_ex(json, "instance", &tmp);

    const char* selected = jext_get_string_dummy(tmp, "selected");

    if (selected[0] != '\0') {
        instance_selected = bc_instance_get(selected);
    }

    json_object_put(json);

    return instance_selected;
}

void bc_instance_select(const char* path) {
    json_object* json = json_object_from_file("settings.json");
    json_object* tmp, *tmp_selected;

    json_object_object_get_ex(json, "instance", &tmp);
    json_object_object_get_ex(tmp, "selected", &tmp_selected);

    json_object_set_string(tmp_selected, path);

    jext_file_write("settings.json", json);
    json_object_put(json);
}

void bc_instance_remove_group(const char* name) {
    json_object* settings = json_object_from_file("settings.json");
    json_object* tmp, * instance_tmp, * arr_tmp;

    json_object_object_get_ex(settings, "instance", &instance_tmp);
    json_object_object_get_ex(instance_tmp, "grouped", &tmp);

    for (int i = 0; i < json_object_array_length(tmp); i++) {
        arr_tmp = json_object_array_get_idx(tmp, i);

        if (strcmp(jext_get_string(arr_tmp, "group_name"), name) == 0) {
            json_object_array_del_idx(tmp, i, 1);
            break;
        }
    }

    jext_file_write("settings.json", settings);
    json_object_put(settings);
}

void bc_instance_create(const char* name, const char* version, const char* version_url, const char* group_name) {
    char n[64];
    snprintf(n, sizeof(n), "%s", name);

    char* path = bc_instance_get_path(n);
    int counter = 1;

    while (bc_file_exists(path) == 1) {
        for (int i = 0; i < counter; i++)
            snprintf(n, sizeof(n), "%s-", n);

        free(path);
        path = bc_instance_get_path(n);
        counter++;
    }

    json_object* config = bc_instance_create_default_config(n, version);
    bc_instance_update_settings(path, group_name);

    make_path(path, 1);
    bc_file_create(path, json_object_to_json_string(config));
    free(path);

    json_object_put(config);
}

void bc_instance_update(const bc_instance* instance) {
    json_object* config = bc_instance_create_config(instance);

    jext_file_write(instance->path, config);
    json_object_put(config);
}

void bc_instance_remove(const char* instance_path) {
    json_object* settings = json_object_from_file("settings.json");
    json_object* instance_tmp, * tmp, * val_tmp, * val_arr_tmp, * val_grouped_arr_tmp, * tmp_selected;

    json_object_object_get_ex(settings, "instance", &instance_tmp);
    json_object_object_get_ex(instance_tmp, "standalone", &tmp);

    for (int i = 0; i < json_object_array_length(tmp); i++) {
        val_tmp = json_object_array_get_idx(tmp, i);

        if (strcmp(json_object_get_string(val_tmp), instance_path) == 0) {
            json_object_array_del_idx(tmp, i, 1);
            break;
        }
    }

    json_object_object_get_ex(instance_tmp, "grouped", &tmp);

    for (int i = 0; i < json_object_array_length(tmp); i++) {
        val_tmp = json_object_array_get_idx(tmp, i);
        json_object_object_get_ex(val_tmp, "instances", &val_arr_tmp);

        for (int y = 0; y < json_object_array_length(val_arr_tmp); y++) {
            val_grouped_arr_tmp = json_object_array_get_idx(val_arr_tmp, i);

            if (strcmp(json_object_get_string(val_grouped_arr_tmp), instance_path) == 0) {
                json_object_array_del_idx(val_arr_tmp, i, 1);
                break;
            }
        }
    }

    json_object_object_get_ex(instance_tmp, "selected", &tmp_selected);

    if (strcmp(json_object_get_string(tmp_selected), instance_path) == 0) {
        json_object_set_string(tmp_selected, "");
    }

    jext_file_write("settings.json", settings);
    json_object_put(settings);
}

bc_instance* bc_instance_get(const char* instance_path) {
    bc_instance* instance = NULL;
    json_object* json = json_object_from_file(instance_path);

    if (json != NULL) {
        instance = malloc(sizeof(bc_instance));

        bc_instance_fill_object_from_json(instance, instance_path, json);
        json_object_put(json);
    }

    return instance;
}

bc_instance_array* bc_instance_get_all() {
    bc_instance_array* instance_array = malloc(sizeof(bc_instance_array));

    json_object* settings = json_object_from_file("settings.json");

    if (settings == NULL) {
        instance_array->len = 0;
        return instance_array;
    }

    json_object* tmp, * instance_tmp, * arr_tmp, * instance_file_tmp;

    json_object_object_get_ex(settings, "instance", &instance_tmp);
    json_object_object_get_ex(instance_tmp, "standalone", &tmp);

    instance_array->len = json_object_array_length(tmp);

    for (int i = 0; i < instance_array->len; i++) {
        arr_tmp = json_object_array_get_idx(tmp, i);

        const char* instance_path = json_object_get_string(arr_tmp);
        instance_file_tmp = json_object_from_file(instance_path);

        bc_instance_fill_object_from_json(&instance_array->arr[i], instance_path, instance_file_tmp);
    }

    json_object_put(settings);

    return instance_array;
}

bc_instance_group_array* bc_instance_group_get_all() {
    bc_instance_group_array* instance_array = malloc(sizeof(bc_instance_group_array));

    json_object* settings = json_object_from_file("settings.json");

    if (settings == NULL) {
        instance_array->len = 0;
        json_object_put(settings);
        return instance_array;
    }

    json_object* tmp, * instance_tmp, * arr_tmp, * group_instances_tmp, * instance_file_tmp;

    json_object_object_get_ex(settings, "instance", &instance_tmp);
    json_object_object_get_ex(instance_tmp, "grouped", &tmp);

    instance_array->len = json_object_array_length(tmp);

    for (int i = 0; i < instance_array->len; i++) {
        arr_tmp = json_object_array_get_idx(tmp, i);

        snprintf(instance_array->arr[i].group_name, sizeof(instance_array->arr[i].group_name), "%s", jext_get_string_dummy(arr_tmp, "group_name"));

        json_object_object_get_ex(arr_tmp, "instances", &group_instances_tmp);
        instance_array->arr[i].len = json_object_array_length(group_instances_tmp);

        for (int y = 0; y < instance_array->arr[i].len; y++) {
            arr_tmp = json_object_array_get_idx(group_instances_tmp, y);
            const char* instance_path = json_object_get_string(arr_tmp);
            instance_file_tmp = json_object_from_file(instance_path);

            bc_instance_fill_object_from_json(&instance_array->arr[i].instances[y], instance_path, instance_file_tmp);
        }
    }

    json_object_put(settings);

    return instance_array;
}

int bc_instance_run_progress() { return bc_game_run_progress; }

void bc_instance_run(const char* server_ip, const char* server_port) {
    bc_instance* in = bc_instance_select_get();
    bc_mod_version_array* mods = bc_mod_list_installed(in->path);

    // makes the path be not of the json, but of the instance directory
    int pathSize = strlen(in->path) - strlen("bc_instance.json");
    in->path[pathSize] = '\0';

    char* selectedJinst = bc_jinst_select_get();
    snprintf(in->java_path, sizeof(in->java_path), "%s", selectedJinst);
    free(selectedJinst);

    char jsonLoc[PATH_MAX];
    snprintf(jsonLoc, sizeof(jsonLoc), "versions/%s.json", in->version);

    json_object* jsonObj = json_object_from_file(jsonLoc);
    bc_version* ver = bc_version_read_json(jsonObj);
    json_object_put(jsonObj);

    bc_account* acc = bc_account_select_get();

    if (acc == NULL) {
        acc = malloc(sizeof(bc_account));

        acc->account_type = BC_ACCOUNT_UNAUTHENTICATED;
        strcpy(acc->username, "Player");
        strcpy(acc->uuid, "bd346dd5-ac1c-427d-87e8-73bdd4bf3e13");
        strcpy(acc->access_token, "-");
    }

    bc_game_data* data = malloc(sizeof(bc_game_data));
    data->instance = in;
    data->version = ver;
    data->account = acc;
    data->mods = mods;
    strcpy(data->server_ip, server_ip);
    strcpy(data->server_port, server_port);

    char* random_uuid = bc_file_uuid();
    snprintf(data->natives_folder, sizeof(data->natives_folder), "%snatives-%s/", in->path, random_uuid);
    free(random_uuid);

    bc_game_run(data);

    free(data->instance);
    free(data->version);
    free(data->account);
    free(data->mods);
    free(data);
}
