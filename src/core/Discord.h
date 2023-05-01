#ifndef BC_DISCORD_H
#define BC_DISCORD_H

struct Application {
    struct IDiscordCore* core;
    struct IDiscordActivityManager* activity;
};

int bc_discord_init();
void bc_discord_loop();
void bc_discord_activity_update(const char* details, const char* state);

#endif
