#if !defined(BC_DISCORD_H) && !defined(BC_RETRO)
#define BC_DISCORD_H

struct Application {
    struct IDiscordCore* core;
    struct IDiscordActivityManager* activity;
};

int bc_discord_init();
void bc_discord_loop();
void bc_discord_activity_update(const char* details, const char* state);
void bc_discord_stop();

#endif
