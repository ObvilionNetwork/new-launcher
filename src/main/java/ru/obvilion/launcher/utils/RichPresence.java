package ru.obvilion.launcher.utils;

import club.minnced.discord.rpc.*;
import javafx.application.Platform;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.client.Client;
import ru.obvilion.launcher.client.Downloader;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.gui.Gui;

public class RichPresence {
    private final DiscordRPC lib = DiscordRPC.INSTANCE;

    public DiscordEventHandlers handlers;
    public DiscordRichPresence presence;

    public RichPresence() {
        presence = new DiscordRichPresence();

        presence.details = "Вспоминает пароль";
        presence.largeImageKey = "logo";

        handlers = new DiscordEventHandlers();

        handlers.joinGame = secret -> {
            Log.info("Discord RPC join: {0}", secret);
            if (Vars.selectedPane != Vars.frameController.MAIN_PANE && Vars.selectedPane != Vars.frameController.SETTINGS_PANE) {
                Vars.frameController.NO_INTERNET_BG.setOpacity(0.35);
                Vars.frameController.NO_INTERNET_TITLE.setText("ОШИБКА АВТОМАТИЧЕСКОГО ПОДКЛЮЧЕНИЯ С DISCORD");
                Vars.frameController.NO_INTERNET_SUBTITLE.setText("Вы должны находиться в меню выбора серверов.");
                StyleUtil.changePosition(Vars.frameController.NO_INTERNET, 0, 0, 1400);
                return;
            }

            Platform.runLater(() -> {
                JSONObject server = null;
                for (Object j : Vars.servers) {
                    JSONObject serv = (JSONObject) j;
                    if (secret.replace("server", "").equals(serv.getInt("id") + "")) {
                        server = serv;
                    }
                }

                if (server == null) {
                    Vars.frameController.NO_INTERNET_BG.setOpacity(0.35);
                    Vars.frameController.NO_INTERNET_TITLE.setText("ОШИБКА АВТОМАТИЧЕСКОГО ПОДКЛЮЧЕНИЯ С DISCORD");
                    Vars.frameController.NO_INTERNET_SUBTITLE.setText("Сервер не найден");
                    StyleUtil.changePosition(Vars.frameController.NO_INTERNET, 0, 0, 1400);
                    return;
                }

                Vars.frameController.NO_INTERNET_BG.setOpacity(0.35);
                Vars.frameController.NO_INTERNET_TITLE.setText("АВТОМАТИЧЕСКОЕ ПОДКЛЮЧЕНИЕ ПРИ ПОМОЩИ DISCORD");
                Vars.frameController.NO_INTERNET_SUBTITLE.setText("Загрузка клиента... Ваш id: " + secret);
                StyleUtil.changePosition(Vars.frameController.NO_INTERNET, 0, 0, 1400);

                Vars.selectedServer = server;
                Downloader.setClient(server.getString("name"));
                new Thread(() -> {
                    Client client = Downloader.load();
                    client.run();
                }).start();

                Gui.openPane(Vars.frameController.DOWNLOADING_PANE);
            });
        };
        handlers.joinRequest = request -> {

        };
        handlers.errored = (errorCode, message) ->
                Log.err("Discord RPC error: {0} - {1}", errorCode + "", message);

        lib.Discord_Initialize(Global.DISCORD_APP_ID, handlers, false, null);
        lib.Discord_Register(Global.DISCORD_APP_ID, null);

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                lib.Discord_RunCallbacks();
            }
        }, "RPC").start();

        updateTimestamp();
        render();
    }

    public void disableInvite() {
        if (presence.joinSecret == null) return;

        presence.partySize = 0;
        presence.partyMax = 0;
        presence.joinSecret = null;
        presence.partyId = null;

        render();
    }

    public void updateInvite() {
        if (Vars.selectedServer == null) return;

        presence.partySize = Vars.selectedServer.getInt("online") + 1;
        presence.partyMax = Vars.selectedServer.getInt("maxOnline");
        presence.joinSecret = "server" + Vars.selectedServer.getInt("id");
        presence.partyId = "party" + Vars.selectedServer.getInt("id");

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