#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include <string.h>

#include "../core/Account.h"
#include "../core/FileSystem.h"

void test_bc_account_create(const char* uuid) {
    bc_account* acc = malloc(sizeof(bc_account));
    strcpy(acc->username, "Test");
    strcpy(acc->uuid, uuid);
    strcpy(acc->refresh_token, uuid);
    strcpy(acc->access_token, uuid);
    acc->account_type = BC_ACCOUNT_MICROSOFT;

    bc_account_create(acc);

    bc_account* newAcc = bc_account_get(uuid);

    assert(strcmp(newAcc->username, acc->username) == 0);
    assert(strcmp(newAcc->refresh_token, acc->refresh_token) == 0);
    assert(strcmp(newAcc->uuid, acc->uuid) == 0);
    assert(strcmp(newAcc->access_token, acc->access_token) == 0);
    assert(newAcc->account_type == acc->account_type);

    free(newAcc);
    free(acc);
}

void test_bc_account_remove(const char* uuid) {
    bc_account_remove(uuid);

    bc_account* acc = bc_account_get(uuid);
    bc_account* accSelected = bc_account_select_get();

    assert(acc == NULL);
    assert(accSelected == NULL);

    free(accSelected);
    free(acc);
}

void test_bc_account_select(const char* uuid) {
    bc_account_select(uuid);

    bc_account* acc = bc_account_select_get();
    assert(strcmp(acc->uuid, uuid) == 0);
    free(acc);
}

int test_bc_account_select_get() {
    bc_account* acc = bc_account_select_get();
    int result = acc != NULL;
    free(acc);

    return result;
}

void test_bc_account_update(const char* uuid) {
    bc_account* curAcc = bc_account_get(uuid);
    bc_account* acc = malloc(sizeof(bc_account));

    strcpy(acc->uuid, uuid);
    strcpy(acc->access_token, "TEST");
    strcpy(acc->refresh_token, "TEST");
    strcpy(acc->username, "TestNew");
    acc->account_type = BC_ACCOUNT_MOJANG;
    
    bc_account_update(acc);

    bc_account* getAcc = bc_account_get(uuid);

    assert(strcmp(curAcc->uuid, getAcc->uuid) == 0);
    assert(strcmp(curAcc->username, getAcc->username) != 0);
    assert(strcmp(curAcc->refresh_token, getAcc->refresh_token) != 0);
    assert(strcmp(curAcc->access_token, getAcc->access_token) != 0);
    assert(curAcc->account_type != getAcc->account_type);

    free(curAcc);
    free(getAcc);
    free(acc);
}

int test_bc_account_list() {
    bc_account_array* accounts = bc_account_list();
    int result = accounts->len;

    if (result > 0) {
        bc_account* acc = bc_account_select_get();

        for (int i = 0; i < accounts->len; i++) {
            assert(strcmp(accounts->arr[i].username, acc->username) == 0);
            assert(strcmp(accounts->arr[i].refresh_token, acc->refresh_token) == 0);
            assert(strcmp(accounts->arr[i].uuid, acc->uuid) == 0);
            assert(strcmp(accounts->arr[i].access_token, acc->access_token) == 0);
            assert(accounts->arr[i].account_type == acc->account_type);
        }

        free(acc);
    }

    free(accounts);

    return result;
}

void test_bc_account_get(const char* uuid) {
    bc_account* acc = bc_account_get(uuid);
    assert(acc == NULL);
}

int main() {
    bc_file_clean();
    bc_file_init();

    char* uuid = bc_file_uuid();

    test_bc_account_get(uuid);
    assert(test_bc_account_select_get() == 0);
    assert(test_bc_account_list() == 0);
    test_bc_account_create(uuid);
    test_bc_account_select(uuid);
    assert(test_bc_account_select_get() == 1);
    test_bc_account_update(uuid);
    assert(test_bc_account_list() == 1);
    test_bc_account_remove(uuid);

    free(uuid);

    return 0;
}