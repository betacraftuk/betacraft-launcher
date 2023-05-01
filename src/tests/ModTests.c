#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include <string.h>

#include "../core/Mod.h"
#include "../core/Instance.h"
#include "../core/FileSystem.h"
#include "../core/Network.h"

void test_bc_mod_list(const char* version) {
    bc_mod_array* arr = bc_mod_list(version);
    assert(arr->len > 0);
    free(arr);
}

void test_bc_mod_list_installed(const char* instance_path) {
    bc_mod_version_array* arr = bc_mod_list_installed(instance_path);
    assert(arr->len == 0);
}

void test_bc_mod_add(const char* instance_path) {
    bc_mod_add("./mod.zip", instance_path, "b1.7.3");
    bc_mod_version_array* arr = bc_mod_list_installed(instance_path);

    assert(strcmp(arr->arr[0].name, "mod") == 0);
    assert(strcmp(arr->arr[0].path, "libraries/b1.7.3/mod/") == 0);
    
    free(arr);
}

void test_bc_mod_replace_jar(const char* instance_path) {
    bc_mod_replace_jar("./mod.zip", instance_path, "b1.7.3");
    bc_mod_version_array* arr = bc_mod_list_installed(instance_path);

    char* absPath = bc_file_make_absolute_path("instances/test/bc_replace/");

    assert(strcmp(arr->arr[0].name, "mod.zip") == 0);
    
    free(absPath);
    free(arr);
}

void test_bc_mod_list_remove(const char* instance_path) {
    bc_mod_list_remove(instance_path, "libraries/b1.7.3/mod/");

    bc_mod_version_array* arr = bc_mod_list_installed(instance_path);

    assert(arr->len == 0);
    
    free(arr);
}

int main() {
    int online = bc_network_status();

    if (!online) {
        return 0;
    }

    bc_file_clean();
    bc_file_init();

    bc_instance_create("test", "b1.7.3", "http://files.betacraft.uk/launcher/v2/assets/jsons/b1.7.3.json", NULL);
    char* instance_path = bc_instance_get_path("test");

    test_bc_mod_list("b1.7.3");
    test_bc_mod_list_installed(instance_path);

    bc_file_directory_create("testMod");
    bc_file_directory_create("testMod/mod");
    bc_file_zip("testMod", "mod.zip");

    test_bc_mod_add(instance_path);
    test_bc_mod_list_remove(instance_path);

    test_bc_mod_replace_jar(instance_path);

    free(instance_path);

    return 0;
}
