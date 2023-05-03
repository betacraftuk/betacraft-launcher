#include "FileSystem.h"

#include "StringUtils.h"
#include "Logger.h"

#include <stdio.h>
#include <sys/stat.h>
#include <json-c/json.h>
#include <zip.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <limits.h>

#ifdef _WIN32
#include <io.h>
#include <windows.h>
#include <direct.h>
#elif __APPLE__
#include <mach-o/dyld.h>
#include <TargetConditionals.h>
#endif

#if defined(__linux__) || defined(__APPLE__)
#include <unistd.h>
#include "../../lib/libtar/libtar.h"
#include <fcntl.h>
#include <zlib.h>
#endif

char* bc_file_make_absolute_path(const char* relative_path) {
    char* workdir = bc_file_directory_get_working();
    char* res = malloc(strlen(workdir) + strlen(relative_path) + 1);
    sprintf(res, "%s%s", workdir, relative_path);
    free(workdir);
#ifdef _WIN32
    char* reppath = repl_str(res, "\\", "/");
    free(res);
    return reppath;
#else
    return res;
#endif
}

char* bc_file_absolute_path(const char* relative_path) {
#ifdef _WIN32
    char* path = _fullpath(NULL, relative_path, _MAX_PATH);
    char* reppath = repl_str(path, "\\", "/");
    free(path);
    return reppath;
#elif defined(__linux__) || defined(__APPLE__)
    char* real = realpath(relative_path, NULL);
    int len = strlen(real);

    if (bc_file_directory_exists(real) && real[len-1] != '/') {
        real = realloc(real, len+2);
        real[len] = '/';
        real[len+1] = '\0';
    }
    return real;
#endif
}

off_t bc_file_size(const char* filepath) {
    struct stat st;
    stat(filepath, &st);

    return st.st_size;
}

int bc_file_exists(const char* filepath) {
    if (access(filepath, 0) == 0) {
        return 1;
    }

    return 0;
}

void bc_file_copy(const char* filepath, const char* destination) {
    FILE* source, * target;
    source = fopen(filepath, "rb");

    fseek(source, 0, SEEK_END);
    int length = ftell(source);

    fseek(source, 0, SEEK_SET);
    target = fopen(destination, "wb");

    if (target == NULL) fclose(source);

    for (int i = 0; i < length; i++) {
        fputc(fgetc(source), target);
    }

    fclose(source);
    fclose(target);
}

void bc_file_create(const char* filepath, const char* data) {
    if (!bc_file_exists(filepath)) {
        FILE* fp = fopen(filepath, "w+");
        if (fp == NULL) {
            bc_log("Error: bc_file_create failed %s\n", strerror(errno));
            return;
        }

        fprintf(fp, "%s", data);
        fclose(fp);
    }
}

bc_file_list_array* bc_file_list(const char* path) {
    bc_file_list_array* arr = malloc(sizeof(bc_file_list_array));
    arr->len = 0;

    DIR* dir;
    struct dirent* ent;

    if ((dir = opendir(path)) == NULL) {
        bc_log("%s\n", "Error: bc_file_list failed");
        return arr;
    }

    while ((ent = readdir(dir)) != NULL) {
        if (strcmp(ent->d_name, ".") == 0 || strcmp(ent->d_name, "..") == 0)
            continue;

        struct stat statbuf;
        char file_path[PATH_MAX];
        snprintf(file_path, sizeof(file_path), "%s/%s", path, ent->d_name);

#ifdef _WIN32
        if (stat(file_path, &statbuf) != 0) {
            bc_log("%s\n", "ERROR: bc_file_list failed - stat");
            exit(EXIT_FAILURE);
        }
#elif defined(__APPLE__) || defined(__linux__)
        if (lstat(file_path, &statbuf) != 0) {
            bc_log("%s\n", "ERROR: bc_file_list failed - lstat");
            exit(EXIT_FAILURE);
        }
#endif

        snprintf(arr->arr[arr->len].name, sizeof(arr->arr[arr->len].name), "%s", ent->d_name);

        arr->arr[arr->len].is_directory = S_ISDIR(statbuf.st_mode);
#if defined(__APPLE__) || defined(__linux__)
        arr->arr[arr->len].is_symlink = S_ISLNK(statbuf.st_mode);
#else
        arr->arr[arr->len].is_symlink = 0;
#endif
        arr->len++;
    }

    closedir(dir);

    return arr;
}

char* bc_file_directory_get_working() {
    char* real;
#ifdef _WIN32
    real = _getcwd(NULL, PATH_MAX);
#elif defined(__linux__) || defined(__APPLE__)
    real = malloc(PATH_MAX);
    getcwd(real, PATH_MAX);
#endif
    int len = strlen(real);

    if (bc_file_directory_exists(real) && real[len - 1] != '/') {
        real = realloc(real, len + 2);
        real[len] = '/';
        real[len+1] = '\0';
    }

    return real;
}

void bc_file_directory_create(const char* name) {
#if defined(__linux__) || defined(__APPLE__)
    mode_t mode = 0755;
    mkdir(name, mode);
#elif _WIN32
    _mkdir(name);
#endif
}

void bc_file_directory_remove(const char* path) {
    int err = rmdir(path);

    if (err == -1 && errno == ENOTEMPTY) {
        bc_file_list_array* arr = bc_file_list(path);

        for (int i = 0; i < arr->len; i++) {
            char filepath[PATH_MAX];
            snprintf(filepath, sizeof(filepath), "%s/%s", path, arr->arr[i].name);

            if (arr->arr[i].is_directory != 0) {
                bc_file_directory_remove(filepath);
            } else {
                remove(filepath);
            }
        }

        rmdir(path);
    } else {
        bc_log("%s %d\n", "Error: bc_file_directory_remove failed", err);
    }
}

void bc_file_directory_copy(const char* source, const char* destination) {
    bc_file_list_array* files = bc_file_list(source);

    for (int i = 0; i < files->len; i++) {
        char sPath[PATH_MAX];
        snprintf(sPath, sizeof(sPath), "%s/%s", source, files->arr[i].name);

        char dPath[PATH_MAX];
        snprintf(dPath, sizeof(dPath), "%s/%s", destination, files->arr[i].name);

        if (files->arr[i].is_directory == 1) {
            make_path(dPath, 0);
            bc_file_directory_copy(sPath, dPath);
            continue;
        } else if (files->arr[i].is_symlink == 1) {
#if defined(__linux__) || defined(__APPLE__)
            char linkTarget[PATH_MAX];
            int len = readlink(sPath, linkTarget, sizeof(linkTarget));
            linkTarget[len] = '\0';
            symlink(linkTarget, dPath);
#endif
        } else {
            bc_file_copy(sPath, dPath);
        }
    }

    free(files);
}

int bc_file_directory_exists(const char* folder) {
    int result = access(folder, 0);

    if (result == 0) {
        struct stat status;
        stat(folder, &status);

        return (status.st_mode & S_IFDIR) != 0;
    }

    return 0;
}

static int do_mkdir(const char* path) {
    struct stat st;
    int	status = 0;

    if (stat(path, &st) != 0) {
        int res;
#ifdef _WIN32
        res = mkdir(path);
#else
        mode_t mode = 0755;
        res = mkdir(path, mode);
#endif
        /* Directory does not exist. EEXIST for race condition */
        if (res != 0 && errno != EEXIST)
            status = -1;
    } else if (st.st_mode & (S_IFDIR == 0)) {
        errno = ENOTDIR;
        status = -1;
    }

    return(status);
}

/**
** mkpath - ensure all directories in path exist
** Algorithm takes the pessimistic view and works top-down to ensure
** each directory in path exists, rather than optimistically creating
** the last element and working backwards.
*/
int make_path(const char* path, int onlyParents) {
    char* pp;
    char* sp;
    int	status;
    char* copypath = strdup(path);

    status = 0;
    pp = copypath;
    while (status == 0 && (sp = strchr(pp, '/')) != 0) {
        if (sp != pp) {
            /* Neither root nor double slash in path */
            *sp = '\0';
            status = do_mkdir(copypath);
            *sp = '/';
        }
        pp = sp + 1;
    }
    if (status == 0 && onlyParents == 0)
        status = do_mkdir(path);

    free(copypath);
    return (status);
}

void bc_file_clean() {
    bc_file_directory_remove("instances/");
    bc_file_directory_remove("versions/");
    bc_file_directory_remove("libraries/");

    remove("settings.json");
    remove("accounts.json");
}

void bc_file_init() {
    make_path("instances/", 0);
    make_path("versions/", 0);
    make_path("libraries/", 0);

    json_object* settings = json_object_new_object();
    json_object* settings_java = json_object_new_object();
    json_object* accounts = json_object_new_object();
    json_object* instance = json_object_new_object();

    json_object_object_add(instance, "selected", json_object_new_string(""));
    json_object_object_add(instance, "standalone", json_object_new_array());
    json_object_object_add(instance, "grouped", json_object_new_array());

    json_object_object_add(settings_java, "selected", json_object_new_string(""));
    json_object_object_add(settings_java, "installations", json_object_new_array());

    json_object_object_add(settings, "language", json_object_new_string("English"));
    json_object_object_add(settings, "discord", json_object_new_boolean(1));
    json_object_object_add(settings, "instance", instance);
    json_object_object_add(settings, "java", settings_java);

    json_object_object_add(accounts, "selected", json_object_new_string(""));
    json_object_object_add(accounts, "accounts", json_object_new_array());

    bc_file_create("settings.json", json_object_to_json_string(settings));
    bc_file_create("accounts.json", json_object_to_json_string(accounts));

    json_object_put(settings);
    json_object_put(accounts);

    bc_log("%s\n", "Files initialized");
}

#if defined(__linux__) || defined(__APPLE__)
void bc_file_gzip_decompress(const char* filepath, const char* out) {
    int LENGTH = 0x1000;

    gzFile file = gzopen(filepath, "rb");
    FILE* fileDecomp = fopen(out, "ab");

    while (1) {
        int err = 0;
        unsigned char buffer[LENGTH];

        int bytes_read = gzread(file, buffer, LENGTH - 1);
        buffer[bytes_read] = '\0';

        fwrite(buffer, 1, bytes_read, fileDecomp);

        if (bytes_read < LENGTH - 1) {
            if (gzeof(file)) {
                break;
            } else {
                const char* error_string = gzerror(file, &err);
                if (err) {
                    bc_log("Error: bc_file_gzip_decompress failed:  %s\n", error_string);
                    exit(EXIT_FAILURE);
                }
            }
        }
    }

    gzclose(file);
    fclose(fileDecomp);

    remove(filepath);
}

void bc_file_untar(const char* filepath, char* dest) {
    char decompPath[PATH_MAX];
    snprintf(decompPath, sizeof(decompPath), "%s", filepath);
    decompPath[strlen(decompPath) - 3] = '\0';

    bc_file_gzip_decompress(filepath, decompPath);

    TAR* tar;

    if (tar_open(&tar, decompPath, 0, O_RDONLY, 0, TAR_GNU) == -1) {
        bc_log("%s\n", "ERROR: bc_file_untar - tar_open failed");
        exit(1);
    }

    if (tar_extract_all(tar, dest) == -1) {
        bc_log("%s\n", "ERROR: bc_file_untar - tar_extract_all failed");
    }

    if (tar_close(tar) == -1) {
        bc_log("%s\n", "ERROR: bc_file_untar - tar_close failed");
        exit(1);
    }

    remove(decompPath);
}
#endif

void bc_file_unzip(const char* filepath, const char* dest) {
    int err;
    char* p_strchr;
    struct zip_stat sb;
    struct zip_file* zf;

    zip_t* zip = zip_open(filepath, 0, &err);

    for (int i = 0; i < zip_get_num_entries(zip, 0); i++) {
        if (zip_stat_index(zip, i, 0, &sb) == 0) {
            int len = strlen(sb.name);

            char path[PATH_MAX];
            snprintf(path, sizeof(path), "%s%s", dest, sb.name);

            if (sb.name[len - 1] == '/') {
                make_path(path, 0);
            } else {
                zf = zip_fopen_index(zip, i, 0);
                p_strchr = strrchr(path, '/');

                char pathdir[PATH_MAX];
                snprintf(pathdir, sizeof(pathdir), "%s", path);
                pathdir[strlen(pathdir) - strlen(p_strchr)] = '\0';

                make_path(pathdir, 0);

                FILE* fp = fopen(path, "wb+");

                char* buffer = malloc(sb.size);
                zip_fread(zf, buffer, sb.size);
                fwrite(buffer, sb.size, 1, fp);

                free(buffer);
                fclose(fp);
                zip_fclose(zf);
            }
        }
    }

    zip_close(zip);
}

void bc_file_zip_directory(const char* p, int n, zip_t* zip) {
    bc_file_list_array* files = bc_file_list(p);

    for (int i = 0; i < files->len; i++) {
        char dirPath[PATH_MAX];
        snprintf(dirPath, sizeof(dirPath), "%s", p);

        if (p[strlen(p) - 1] != '/')
            strcat(dirPath, "/");

        strcat(dirPath, files->arr[i].name);
        char* path = dirPath + n;

        if (files->arr[i].is_directory) {
            zip_dir_add(zip, path, ZIP_FL_ENC_UTF_8);
            bc_file_zip_directory(dirPath, n, zip);
        } else {
            zip_source_t* source = zip_source_file(zip, dirPath, 0, -1);
            int s = zip_file_add(zip, path, source, ZIP_FL_ENC_UTF_8);

            if (source == NULL || s < 0)
                zip_source_free(source);
        }
    }

    free(files);
}

void bc_file_zip(const char* directory, const char* file_name) {
    int err = 0;
    struct zip_stat sb;
    zip_t* zip = zip_open(file_name, ZIP_CREATE, &err);

    bc_file_zip_directory(directory, strlen(directory), zip);

    zip_close(zip);
}

char* bc_file_uuid() {
    char v[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    char* buf = malloc(37);

    srand(time(NULL));
    for (int i = 0; i < 36; ++i) {
        buf[i] = v[rand() % 16];
    }

    buf[8] = '-';
    buf[13] = '-';
    buf[18] = '-';
    buf[23] = '-';
    buf[36] = '\0';

    return buf;
}

char* bc_file_os() {
    char* os = malloc(32);
#ifdef _WIN64
    strcpy(os, "win64");
#elif _WIN32
    strcpy(os, "win32");
#elif __linux__
    strcpy(os, "linux");
#elif __APPLE__
#ifdef __aarch64__
    strcpy(os, "osxarm64");
#elif TARGET_OS_MAC
    strcpy(os, "osx64");
#endif
#endif

    return os;
}
