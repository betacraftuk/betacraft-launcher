#ifndef BC_FILESYSTEM_H
#define BC_FILESYSTEM_H

#include <sys/types.h>
#include <limits.h>

typedef struct bc_file_list_array_dirent {
    char name[PATH_MAX];
    int is_directory;
    int is_symlink;
    int size;
} bc_file_list_array_dirent;

typedef struct bc_file_list_array {
    bc_file_list_array_dirent arr[2048];
    int len;
} bc_file_list_array;

void bc_file_init();
void bc_file_clean();

void bc_file_create(const char* filepath, const char* data);
void bc_file_copy(const char* filepath, const char* destination);
off_t bc_file_size(const char* filepath);
int bc_file_exists(const char* filepath);
bc_file_list_array* bc_file_list(const char* path);

char* bc_file_make_absolute_path(const char* relative_path);
char* bc_file_absolute_path(const char* relative_path);

void bc_file_directory_create(const char* name);
void bc_file_directory_remove(const char* path);
char* bc_file_directory_get_working();
void bc_file_directory_remove(const char* path);
void bc_file_directory_copy(const char* source, const char* destination);
int bc_file_directory_exists(const char* folder);

int make_path(const char* path, int onlyParents);

char* bc_file_uuid();
char* bc_file_os();

void bc_file_extract(const char* filepath, const char* dest);
void bc_file_archive(const char* directory, const char* file_name);

#if defined(__linux__) || defined(__APPLE__)
void bc_file_untar(const char* filepath, char* dest);
#endif

#endif
