#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include <string.h>

#include "../core/Instance.h"
#include "../core/FileSystem.h"

void test_bc_instance_group_name_get_all() {
    bc_instance_group_name_array* groups = bc_instance_group_name_get_all();
    assert(groups->len == 0);
    free(groups);
}

void test_bc_instance_group_create() {
    bc_instance_group_create("testgroup");

    bc_instance_group_name_array* groups = bc_instance_group_name_get_all();
    assert(groups->len > 0);

    free(groups);
}

int test_bc_instance_get_all() {
    bc_instance_array* arr = bc_instance_get_all();
    int result = arr->len;
    free(arr);

    return result;
}

int test_bc_instance_group_get_all() {
    bc_instance_group_array* arr = bc_instance_group_get_all();
    int result = arr->len;

    for (int i = 0; i < arr->len; i++) {
        if (arr->arr[i].len > 0) {
            result = arr->arr[i].len;
        }
    }

    free(arr);

    return result;
}

void test_bc_instance_get(const char* path) {
    bc_instance* i = bc_instance_get(path);
    assert(i == NULL);
}

void test_bc_instance_create(const char* name, const char* group) {
    bc_instance_create(name, "b1.7.3", group);

    char* path = bc_instance_get_path(name);
    bc_instance* i = bc_instance_get(path);

    assert(strcmp(i->name, name) == 0);
    assert(strcmp(i->version, "b1.7.3") == 0);
    assert(strcmp(i->path, path) == 0);

    free(path);
    free(i);
}

void test_bc_instance_remove(const char* name) {
    char* path = bc_instance_get_path(name);
    bc_instance_remove(path);

    bc_instance_array* standalone = bc_instance_get_all();
    bc_instance_group_array* grouped = bc_instance_group_get_all();

    int found = 0;
    for (int i = 0; i < standalone->len; i++) {
        if (strcmp(path, standalone->arr[i].path) == 0) {
            found = 1;
        }
    }

    for (int i = 0; i < grouped->len; i++) {
        for (int j = 0; j < grouped->arr[i].len; j++) {
            if (strcmp(path, grouped->arr[i].instances[j].path) == 0) {
                found = 1;
            }
        }
    }

    assert(found == 0);

    free(path);
}

void test_bc_instance_select(const char* name) {
    char* path = bc_instance_get_path(name);
    bc_instance_select(path);

    bc_instance* i = bc_instance_select_get();
    assert(strcmp(i->name, name) == 0);
    assert(strcmp(i->path, path) == 0);

    free(path);
}

void test_bc_instance_select_get() {
    bc_instance* i = bc_instance_select_get();
    assert(i == NULL);
}

int main() {
    bc_file_clean();
    bc_file_init();

    test_bc_instance_group_name_get_all();
    assert(test_bc_instance_get_all() == 0);
    assert(test_bc_instance_group_get_all() == 0);
    test_bc_instance_select_get();
    test_bc_instance_group_create();
    test_bc_instance_get("instances/test/bc_instance.json");
    test_bc_instance_create("test", NULL);
    assert(test_bc_instance_get_all() > 0);
    test_bc_instance_create("testGroup", "testgroup");
    assert(test_bc_instance_group_get_all() > 0);
    test_bc_instance_select("test");
    test_bc_instance_remove("test");
    test_bc_instance_select_get();
    test_bc_instance_remove("testGroup");

    return 0;
}