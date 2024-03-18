#include <assert.h>

#include "../core/Betacraft.h"
#include "../core/Network.h"

#define INVALIDAVATARSIZE 12

void test_bc_server_list() {
    bc_server_array servers;
    bc_server_list(&servers);
    assert(servers.len > 0);
}

int main() {
    // int online = bc_network_status();

    // if (!online) {
        // return 0;
    // }

    // test_bc_server_list();

    return 0;
}
