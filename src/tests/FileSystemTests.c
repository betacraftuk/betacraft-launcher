#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "../core/FileSystem.h"

#define TESTFILESIZE 4

void test_bc_file_create() {
    bc_file_create("fileSystemTest.txt", "TEST");
    assert(bc_file_exists("fileSystemTest.txt"));
}

void test_bc_file_size() {
    off_t size = bc_file_size("fileSystemTest.txt");
    assert(size == TESTFILESIZE);
}

void test_bc_file_copy() {
    bc_file_copy("fileSystemTest.txt", "fileSystemTestCopy.txt");

    assert(bc_file_exists("fileSystemTestCopy.txt"));
    assert(bc_file_size("fileSystemTestCopy.txt") == 4);
}

void test_bc_file_directory_create() {
    bc_file_directory_create("test/");
    assert(bc_file_directory_exists("test/"));
}

void test_bc_file_directory_remove() {
    bc_file_directory_remove("test/");
    assert(!bc_file_directory_exists("test/"));
}

void test_make_path() {
    make_path("test/innerDir/", 0);
    assert(bc_file_directory_exists("test/innerDir/"));
}

int test_bc_file_list(const char *dest) {
    bc_file_list_array *files = bc_file_list(dest);
    int ret = files->len;
    free(files);

    return ret;
}

void test_bc_file_make_absolute_path(const char *path) {
    char *absolutePath = bc_file_make_absolute_path(path);

    assert(absolutePath != NULL);
    free(absolutePath);
}

void test_bc_file_directory_copy() {
    bc_file_directory_copy("test/", "copyTest/");
    assert(bc_file_directory_exists("copyTest/innerDir/"));
}

void test_bc_file_archive() {
    bc_file_archive("test", "test.zip");
    assert(bc_file_exists("test.zip"));
}

void test_bc_file_extract() {
    bc_file_extract("test.zip", "test/innerDir/instances/");
    assert(bc_file_directory_exists("test/innerDir/instances/innerDir/"));
}

int main() {
    bc_file_clean();
    assert(!bc_file_exists("settings.json"));
    bc_file_init();

    assert(bc_file_exists("settings.json"));
    test_bc_file_create();
    test_bc_file_size();
    test_bc_file_copy();
    assert(bc_file_directory_exists("instances/"));
    test_bc_file_directory_create();
    assert(test_bc_file_list("./") > 0);
    assert(test_bc_file_list("versions/") == 0);
    test_bc_file_directory_remove();
    test_make_path();
    test_bc_file_make_absolute_path("instances/");
    test_bc_file_directory_copy();
    test_bc_file_archive();
    test_bc_file_extract();

    bc_file_directory_remove("test/");
    bc_file_directory_remove("copyTest/");
    remove("fileSystemTest.txt");
    remove("fileSystemTestCopy.txt");
    remove("test.zip");

    return 0;
}
