#ifndef BC_NETWORK_H
#define BC_NETWORK_H

#include <stddef.h>

typedef struct bc_memory {
    char* response;
    size_t size;
} bc_memory;

typedef struct bc_progress {
    char filename[256];
    double totalToDownload;
    double nowDownloaded;
    double totalToDownloadMb;
    double nowDownloadedMb;
} bc_progress;

extern bc_progress bc_network_progress;
extern int bc_network_cancel;

bc_memory bc_network_get_chunk(const char* url);
char* bc_network_get(const char* url, const char* header);
char* bc_network_post(const char* url, const char* data, const char* header);
int bc_network_download(const char* url, const char* dest, int isFile);
int bc_network_status();

#endif
