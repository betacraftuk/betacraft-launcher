#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include <string.h>

#include "../core/FileSystem.h"

#define TESTFILESIZE 4

void test_bc_file_create() {
    bc_file_create("fileSystemTest.txt", "TEST");
    assert(bc_file_exists("fileSystemTest.txt") == 1);
}

void test_bc_file_size() {
    off_t size = bc_file_size("fileSystemTest.txt");
    assert(size == TESTFILESIZE);
}

void test_bc_file_copy() {
    bc_file_copy("fileSystemTest.txt", "fileSystemTestCopy.txt");

    assert(bc_file_exists("fileSystemTestCopy.txt") == 1);
    assert(bc_file_size("fileSystemTestCopy.txt") == 4);
}

void test_bc_file_directory_create() {
    bc_file_directory_create("test/");
    assert(bc_file_directory_exists("test/") == 1);
}

void test_bc_file_directory_remove() {
    bc_file_directory_remove("test/");
    assert(bc_file_directory_exists("test/") == 0);
}

void test_make_path() {
    make_path("test/innerDir/", 0);
    assert(bc_file_directory_exists("test/innerDir/") == 1);
}

int test_bc_file_list(const char* dest) {
    bc_file_list_array* files = bc_file_list(dest);
    int ret = files->len;
    free(files);

    return ret;
}

void test_bc_file_make_absolute_path(const char* path) {
    char* absolutePath = bc_file_make_absolute_path(path);

    assert(absolutePath != NULL);
    free(absolutePath);
}

void test_bc_file_directory_copy() {
    bc_file_directory_copy("test/", "copyTest/");
    assert(bc_file_directory_exists("copyTest/innerDir/") == 1);
}

void test_bc_file_zip() {
    bc_file_zip("test/", "test.zip");
    assert(bc_file_exists("test.zip") == 1);
}

void test_bc_file_unzip() {
    bc_file_unzip("test.zip", "test/innerDir/instances/");
    assert(bc_file_directory_exists("test/innerDir/instances/innerDir/") == 1);
}

int main() {
    bc_file_clean();
    assert(bc_file_exists("settings.json") == 0);
    bc_file_init();

    assert(bc_file_exists("settings.json") == 1);
    test_bc_file_create();
    test_bc_file_size();
    test_bc_file_copy();
    assert(bc_file_directory_exists("instances/") == 1);
    test_bc_file_directory_create();
    assert(test_bc_file_list("./") > 0);
    assert(test_bc_file_list("versions/") == 0);
    test_bc_file_directory_remove();
    test_make_path();
    test_bc_file_make_absolute_path("instances/");
    test_bc_file_directory_copy();
    test_bc_file_zip();
    test_bc_file_unzip();

    bc_file_directory_remove("test/");
    bc_file_directory_remove("copyTest/");
    remove("fileSystemTest.txt");
    remove("fileSystemTestCopy.txt");
    remove("test.zip");

    return 0;
}
