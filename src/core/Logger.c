#include "Logger.h"

#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>
#include <time.h>
#include <string.h>

void bc_log(const char* format, ...) {
    FILE* fp;
    fp = fopen("betacraft.log", "a");

    va_list args, args2;
    va_start(args, format);
    va_copy(args2, args);

    char timeString[9];
    struct tm* tm_info;
    time_t now;

    time(&now);
    tm_info = localtime(&now);

    strftime(timeString, sizeof(timeString), "%H:%M:%S", tm_info);

    char outFormat[256];
    snprintf(outFormat, sizeof(outFormat), "[%s] %s", timeString, format);

    char printbuffer[8192];
    vsprintf(printbuffer, outFormat, args);
    // TODO: make a list of forbidden strings at startup and exclude them from logging

    printf(printbuffer);
    fprintf(fp, printbuffer);

    va_end(args);
    va_end(args2);

    fclose(fp);
}
