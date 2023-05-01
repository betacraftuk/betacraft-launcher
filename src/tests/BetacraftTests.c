#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include <string.h>

#include "../core/Betacraft.h"
#include "../core/Network.h"

#define INVALIDAVATARSIZE 12

void test_bc_server_list() {
    bc_server_array* servers = bc_server_list();
    assert(servers->len > 0);
    free(servers);
}

int test_bc_avatar_get(const char* uuid) {
    bc_memory avatar = bc_avatar_get(uuid);
    return avatar.size != INVALIDAVATARSIZE;
    free(avatar.response);
}

int main() {
    int online = bc_network_status();

    if (!online) {
        return 0;
    }

    test_bc_server_list();
    assert(test_bc_avatar_get("bd346dd5-ac1c-427d-87e8-73bdd4bf3e13") == 1);
    assert(test_bc_avatar_get("TEST") == 0);

    return 0;
}
