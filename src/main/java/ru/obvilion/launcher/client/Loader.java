package ru.obvilion.launcher.client;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import ru.obvilion.json.JSONArray;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.api.Request;
import ru.obvilion.launcher.api.RequestType;
import ru.obvilion.launcher.config.Config;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.controllers.FrameController;
import ru.obvilion.launcher.controllers.ServersController;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.utils.Log;
import ru.obvilion.launcher.utils.RichPresence;
import ru.obvilion.launcher.utils.StyleUtil;
import ru.obvilion.launcher.utils.SystemStats;

import java.util.*;

public class Loader {
    public static long lastChangedPosition = 0;

    public static void load() {
        final FrameController c = Vars.frameController;

        new Thread(Loader::loadServers, "ServersUpdater").start();

        if (!Config.getBooleanValue("savePass")) {
            Config.setValue("password", "");
        }

        Vars.selectedPane = c.AUTHORIZATION_PANE;

        /* Auto login */
        if (!Config.getValue("login").equals("") && !Config.getValue("password").equals("")) {
            c.AUTHORIZATION_PANE.setVisible(false);
            c.LOADING_PANE.setVisible(true);
            Vars.selectedPane = c.LOADING_PANE;
            new Thread(() -> {
                autoLogin(true);
            }, "AutoLoginUpdater").start();
        }

        try {
            Vars.richPresence = new RichPresence();
            Log.info("Initialized Discord rich presence.");
        } catch (Exception e) {
            Log.warn("Discord rich presence is not initialized.");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (Vars.minecraft != null) {
                Vars.minecraft.destroy();
            }
        }));

        new Thread(() -> {
            int old = 0;
            while (true) {
                try {
                    Thread.sleep(1100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int tec = SystemStats.getUsedMemoryMB();

                if (Math.max(old, tec) - 10 < Math.min(old, tec)) continue;

                System.gc();
                int afterGC = SystemStats.getUsedMemoryMB();

                Log.debug("GC: {0}mb -> {1}mb", tec + "", afterGC + "");

                old = afterGC;
            }
        }, "GarbageClearing").start();
    }

    public static void autoLogin(boolean first) {
        final FrameController c = Vars.frameController;

        Request r1 = new Request(RequestType.POST, Global.API_LINK + "auth/login");
        r1.setBody(new JSONObject().put("name", Config.getValue("login")).put("password", Config.getPasswordValue("password")));
        JSONObject result = r1.connectAndGetJSON();

        if (result != null) {
            if (!result.has("token")) {
                Log.info("Automatic login to account is failed. Stopping cycle.");
                Platform.runLater(() -> Gui.openPane(c.AUTHORIZATION_PANE));
                return;
            }

            Log.info("Automatic login to account is successful");

            Config.setValue("token", result.getString("token"));
            Config.setValue("uuid", result.getString("uuid"));
            Config.setValue("login", result.getString("name"));

            Platform.runLater(() -> {
                c.BALANCE.setText("Баланс: " + result.getInt("money") + "p.");
            });

            Gui.openPane(c.MAIN_PANE);
            StyleUtil.createFadeAnimation(c.BG_TOP, 600, 0);

            if (lastChangedPosition + 1400 < System.currentTimeMillis()) {
                StyleUtil.changePosition(c.NO_INTERNET, 0, -150, 2400);
            }
            lastChangedPosition = System.currentTimeMillis();

            Image avatar = new Image(Global.API_LINK + "users/" + Config.getValue("login") + "/avatar", 512, 512, true, false);
            if (!avatar.isError()) {
                c.AVATAR.setFill(new ImagePattern(avatar));
            } else {
                Log.err("Error loading user avatar");
                c.AVATAR.setFill(Color.valueOf("#192331"));
            }

            if (Vars.richPresence != null) {
                Vars.richPresence.updateDescription("Игрок " + Config.getValue("login"));
                Vars.richPresence.updateState("Выбирает сервер");
            }

            Thread.currentThread().interrupt();
        } else {
            if (result == null) {
                Log.err("Error during automatic authorization. Attempt to load the servers after 5 seconds...");

                c.NO_INTERNET_BG.setOpacity(0.35);
                c.NO_INTERNET_TITLE.setText("НЕТ ПОДКЛЮЧЕНИЯ К СЕРВЕРАМ OBVILION NETWORK");
                c.NO_INTERNET_SUBTITLE.setText("Проверьте подключение к сети или обратитесь к техподдержке при помощи Discord: https://discord.gg/cg82mjh");

                if (lastChangedPosition + 1400 < System.currentTimeMillis()) {
                    StyleUtil.changePosition(c.NO_INTERNET, 0, 0, 1400);
                }
                lastChangedPosition = System.currentTimeMillis();
            }

            if (first) {
                Platform.runLater(() -> Gui.openPane(c.AUTHORIZATION_PANE));
            }

            try {
                Thread.sleep(5000);
            } catch (Exception e) { /* Ignored */ }

            autoLogin(false);
        }
    }

    public static void loadServers() {
        final FrameController c = Vars.frameController;

        /* Get servers list */
        Request r = new Request(Global.API_LINK + "servers");
        JSONObject servers = r.connectAndGetJSON();

        if (servers != null) {
            Vars.servers = servers.getJSONArray("servers");
            JSONArray temp = new JSONArray();

            Log.debug("Sorting servers... Count: " + Vars.servers.length());

            int index = 0;
            Map<Integer, String> _do = new HashMap<>();
            for (Object obj : Vars.servers) {
                JSONObject tec = (JSONObject) obj;

                if (tec.getString("type").equalsIgnoreCase("minecraft")) {
                    Log.debug("+ Server " + tec.getString("name"));
                    _do.put(index, tec.getString("name"));
                }

                index++;
            }

            List<Map.Entry<Integer, String>> entries = new ArrayList<>(_do.entrySet());
            entries.sort(Map.Entry.comparingByValue());

            for (Map.Entry<Integer, String> entry : entries) {
                temp.put(Vars.servers.getJSONObject(entry.getKey()));
            }

            Vars.servers = temp;

            Log.info("Loaded servers: " + Vars.servers.length());

            if (Vars.serversController == null) {
                new ServersController().init();
            }

            if (c.NO_INTERNET.getLayoutY() != -150) {
                if (lastChangedPosition + 1400 < System.currentTimeMillis()) {
                    StyleUtil.changePosition(c.NO_INTERNET, 0, -150, 2400);
                }
                lastChangedPosition = System.currentTimeMillis();
            }

            Thread.currentThread().interrupt();
        } else {
            if (c.NO_INTERNET.getLayoutY() != 0) {
                c.NO_INTERNET_BG.setOpacity(0.35);
                c.NO_INTERNET_TITLE.setText("НЕТ ПОДКЛЮЧЕНИЯ К СЕРВЕРАМ OBVILION NETWORK");
                c.NO_INTERNET_SUBTITLE.setText("Проверьте подключение к сети или обратитесь к техподдержке при помощи Discord: https://discord.gg/cg82mjh");

                if (lastChangedPosition + 1400 < System.currentTimeMillis()) {
                    StyleUtil.changePosition(c.NO_INTERNET, 0, 0, 1400);
                }
                lastChangedPosition = System.currentTimeMillis();
            }

            Vars.servers = new JSONArray();
            Log.err("Unable to get the list of servers. Attempt to load the servers after 5 seconds...");

            try {
                Thread.sleep(5000);
            } catch (Exception e) { /* Ignored */ }

            loadServers();
        }
    }
}
