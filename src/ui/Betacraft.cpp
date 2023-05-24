#include "Betacraft.h"

extern "C" {
    #include "../core/Settings.h"
    #include "../core/JsonExtension.h"
}

char _languagePath[PATH_MAX];

QString bc_translate(const char* key) {
    json_object* json = json_object_from_file(_languagePath);

    QString ret = QString(jext_get_string_dummy(json, key));

    json_object_put(json);

    return ret;
}

void bc_translate_init() {
    bc_settings* settings = bc_settings_get();
    snprintf(_languagePath, sizeof(_languagePath), "lang/%s.json", settings->language);

    free(settings);
}