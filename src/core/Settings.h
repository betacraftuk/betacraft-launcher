#ifndef BC_SETTINGS_H
#define BC_SETTINGS_H

#include <limits.h>

typedef struct bc_settings {
    char language[32];
    char instance[PATH_MAX];
    char java_install[PATH_MAX];
    int discord;
} bc_settings;

void bc_settings_update(bc_settings *settings);
bc_settings *bc_settings_get();

#endif
