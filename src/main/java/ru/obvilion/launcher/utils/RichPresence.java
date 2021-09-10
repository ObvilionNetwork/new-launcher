package ru.obvilion.launcher.utils;

import club.minnced.discord.rpc.*;
import ru.obvilion.launcher.config.Global;

public class RichPresence {
    private final DiscordRPC lib = DiscordRPC.INSTANCE;

    public DiscordEventHandlers handlers;
    public DiscordRichPresence presence;

    public RichPresence() {
        presence = new DiscordRichPresence();

        presence.details = "Вспоминает пароль";
        presence.largeImageKey = "logo";

        handlers = new DiscordEventHandlers();

        lib.Discord_Initialize(Global.DISCORD_APP_ID, handlers, true, null);

        updateTimestamp();
        render();
    }

    public void updateTimestamp() {
        try {
            presence.startTimestamp = System.currentTimeMillis();
            render();
        } catch (Exception e) {
            Log.err("Error on updating timestamp in rich presence: " + e.getLocalizedMessage());
        }
    }

    public void updateDescription(String description) {
        try {
            presence.details = description;
            render();
        } catch (Exception e) {
            Log.err("Error on updating description in rich presence: " + e.getLocalizedMessage());
        }
    }

    public void updateState(String state) {
        try {
            presence.state = state;
            render();
        } catch (Exception e) {
            Log.err("Error on updating state in rich presence: " + e.getLocalizedMessage());
        }
    }

    public void render() {
        lib.Discord_UpdatePresence(presence);
    }

    public void dispose() {
        lib.Discord_Shutdown();
    }
}