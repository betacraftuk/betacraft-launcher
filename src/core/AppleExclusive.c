#include "AppleExclusive.h"

#ifdef __APPLE__
#include <CoreServices/CoreServices.h>

char* bc_file_get_application_support() {
    FSRef ref;
    OSType folderType = kApplicationSupportFolderType;
    char path[PATH_MAX];

    FSFindFolder(kUserDomain, folderType, kCreateFolder, &ref);

    FSRefMakePath(&ref, (UInt8*)&path, PATH_MAX);

    char* out = malloc(strlen(path) + 1);
    strcpy(out, path);

    return out;
}
#endif
