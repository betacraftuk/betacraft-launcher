#include "StringUtils.h"
#include "Betacraft.h"

#include <string.h>
#include <stdlib.h>
#include <stddef.h>
#include <stdio.h>
#include <assert.h>

void repl_str(char* target, const char* input, const char* from, const char* to) {
    char* str = strdup(input);

    size_t cache_sz_inc = 16;
    const size_t cache_sz_inc_factor = 3;
    const size_t cache_sz_inc_max = 1048576;

    char* pret;
    const char* pstr2, * pstr = str;
    size_t i, count = 0;
    ptrdiff_t* pos_cache_tmp, * pos_cache = NULL;
    size_t cache_sz = 0;
    size_t cpylen, orglen, retlen, tolen, fromlen = strlen(from);

    while ((pstr2 = strstr(pstr, from)) != NULL) {
        count++;

        if (cache_sz < count) {
            cache_sz += cache_sz_inc;
            pos_cache_tmp = realloc(pos_cache, sizeof(*pos_cache) * cache_sz);
            if (pos_cache_tmp == NULL) {
                goto end_repl_str;
            }
            else pos_cache = pos_cache_tmp;
            cache_sz_inc *= cache_sz_inc_factor;
            if (cache_sz_inc > cache_sz_inc_max) {
                cache_sz_inc = cache_sz_inc_max;
            }
        }

        pos_cache[count - 1] = pstr2 - str;
        pstr = pstr2 + fromlen;
    }

    orglen = pstr - str + strlen(pstr);

    if (count > 0) {
        tolen = strlen(to);
        retlen = orglen + (tolen - fromlen) * count;
    }
    else retlen = orglen;

    if (count == 0) {
        strcpy(target, str);
    } else {
        pret = target;
        memcpy(pret, str, pos_cache[0]);
        pret += pos_cache[0];
        for (i = 0; i < count; i++) {
            memcpy(pret, to, tolen);
            pret += tolen;
            pstr = str + pos_cache[i] + fromlen;
            cpylen = (i == count - 1 ? orglen : pos_cache[i + 1]) - pos_cache[i] - fromlen;
            memcpy(pret, pstr, cpylen);
            pret += cpylen;
        }
        target[retlen] = '\0';
    }

end_repl_str:

    free(pos_cache);
    free(str);
}

int count_substring(char* s, char c) {
    char buffer[1024];
    assert(strlen(s) < sizeof(buffer));

    snprintf(buffer, sizeof(buffer), "%s", s);

    return *buffer == '\0'
               ? 0
               : count_substring(buffer + 1, c) + (*buffer == c);
}

int str_starts_with(char* string, char* prefix) {
    return strncmp(prefix, string, strlen(prefix)) == 0;
}

int str_ends_with(char* string, char* suffix) {
    char* dot = strrchr(string, '.');

    if (dot == NULL)
        return 0;

    return strcmp(dot, suffix) == 0;
}
