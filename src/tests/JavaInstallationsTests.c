#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "../core/FileSystem.h"
#include "../core/JavaInstallations.h"

void test_bc_jinst_select_get() {
    char *path = bc_jinst_select_get();
    assert(path == NULL);
    free(path);
}

void test_bc_jinst_select() {
    bc_jinst_select("test/path/java");
    char *path = bc_jinst_select_get();

    assert(path != NULL);
    free(path);
}

int main() {
    bc_file_clean();
    bc_file_init();

    test_bc_jinst_select_get();
    test_bc_jinst_select();

    return 0;
}