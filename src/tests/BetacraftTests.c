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

int main() {
    int online = bc_network_status();

    if (!online) {
        return 0;
    }

    test_bc_server_list();

    return 0;
}
