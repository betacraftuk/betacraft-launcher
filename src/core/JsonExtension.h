#ifndef BC_JSONEXTENSION_H
#define BC_JSONEXTENSION_H

#include <json-c/json.h>

char* jext_alloc_string(json_object* json);
char** jext_alloc_string_array(json_object* json);
char* jext_get_string(json_object* json, const char* section);
const char* jext_get_string_dummy(json_object* json, const char* section);
int jext_get_int(json_object* json, const char* section);
int jext_get_boolean(json_object* json, const char* section);
char** jext_get_string_array(json_object* json, const char* section);
int jext_get_string_array_index(json_object* arr, const char* section, const char* value);
int jext_get_double(json_object* json, const char* section);
void jext_file_write(const char* path, json_object* obj);

#endif