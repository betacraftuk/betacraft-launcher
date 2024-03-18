#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "../core/FileSystem.h"
#include "../core/Settings.h"

void test_bc_settings_update() {
    bc_settings *settings = malloc(sizeof(bc_settings));
    settings->discord = 1;
    strcpy(settings->language, "French");

    bc_settings_update(settings);

    bc_settings *cur = bc_settings_get();
    assert(settings->discord == cur->discord);
    assert(strcmp(settings->language, cur->language) == 0);

    free(settings);
    free(cur);
}

int main() {
    bc_file_clean();
    bc_file_init();

    test_bc_settings_update();

    return 0;
}