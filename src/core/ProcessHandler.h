#ifndef BC_PROCESSHANDLER_H
#define BC_PROCESSHANDLER_H

#include "Account.h"

extern char* bc_process_log;

struct bc_process_args {
    char* arr[128];
    int size;
    int len;
} typedef bc_process_args;

void bc_process_create(bc_process_args* args);
void bc_process_create_log(bc_process_args* args, bc_account* account);

#endif
