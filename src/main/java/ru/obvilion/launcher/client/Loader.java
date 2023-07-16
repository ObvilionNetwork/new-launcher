package ru.obvilion.launcher.client;

import javafx.application.Platform;
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
import ru.obvilion.launcher.fx.CachingImageLoader;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.utils.*;

import javax.net.ssl.SSLHandshakeException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
            e.printStackTrace();
            Log.warn("Discord rich presence is not initialized.");
        }

        new Thread(() -> {
            int old = 0;
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int tec = SystemStats.getUsedMemoryMB();

                if (Math.max(old, tec) - 10 < Math.min(old, tec)) continue;

                System.gc();
                int afterGC = SystemStats.getUsedMemoryMB();

                if (Global.PRINT_GC_STATUS) {
                    Log.debug("GC: {0}mb -> {1}mb", tec + "", afterGC + "");
                }
                Platform.runLater(() -> {
                    DateFormat df = new SimpleDateFormat("hh:mm:ss");

                    Date today = Calendar.getInstance().getTime();
                    String todayAsString = df.format(today);


                    Vars.frameController.DEBUG_LASTGC.setText("Посл. очистка " + todayAsString);
                    Vars.frameController.DEBUG_MEMORY.setText("Потр. " + afterGC + " MB, очищено " + (afterGC - tec)  + " MB");
                });


                old = afterGC;
            }
        }, "GarbageClearing").start();
    }

    public static void autoLogin(boolean first) {
        final FrameController c = Vars.frameController;

        Request r1 = new Request(RequestType.POST, Global.API_LINK + "auth/login");
        r1.setBody(new JSONObject().put("name", Config.getValue("login")).put("password", Config.getPasswordValue("password")));
        JSONObject result = null;

        String customError    = "НЕТ ПОДКЛЮЧЕНИЯ К СЕРВЕРАМ OBVILION NETWORK",
               customErrorSub = "Проверьте подключение к сети или обратитесь к техподдержке при помощи Discord: https://discord.gg/cg82mjh";

        boolean try_load = true;

        try {
            result = r1.connectAndGetJSON_THROWS();
        } catch (Exception ex) {
            if (ex.getClass() == SSLHandshakeException.class) {
                Log.err("Auto login error: broken version of Java detected!");

                try_load = false;

                customError = "ОБНАРУЖЕНА ПОЛОМАННАЯ ВЕРСИЯ JAVA";
                customErrorSub = "Скачайте Java тут https://java.com/ru/download или обратитесь к техподдержке при помощи Discord: https://discord.gg/cg82mjh";
            }

            ex.printStackTrace();
        }

        if (result != null) {
            if (!result.has("token")) {
                Log.info("Automatic login to account is failed. Stopping cycle.");
                Platform.runLater(() -> Gui.openPane(c.AUTHORIZATION_PANE));
                return;
            }

            Log.info("Automatic login to account is successful");

            Vars.userData = result;

            Config.setValue("token", result.getString("token"));
            Config.setValue("uuid", result.getString("uuid"));
            Config.setValue("login", result.getString("name"));

            JSONObject finalResult = result;
            Platform.runLater(() -> {
                c.BALANCE.setText("Баланс: " + finalResult.getInt("money") + "p.");
            });

            Gui.openPane(c.MAIN_PANE);
            StyleUtil.createFadeAnimation(c.BG_TOP, 600, 0);

            if (lastChangedPosition + 1400 < System.currentTimeMillis()) {
                StyleUtil.changePosition(c.NO_INTERNET, 0, -150, 2400);
            }
            lastChangedPosition = System.currentTimeMillis();

            new CachingImageLoader()
                    .load(Global.API_LINK + "users/" + DesktopUtil.encodeValue(Config.getValue("login")) + "/avatar")
                    .useLoadingGif(true)
                    .setLifetime(1000 * 60 * 60)
                    .setCallback(img -> {
                        if (img.isError()) {
                            c.AVATAR.setFill(Color.valueOf("#192331"));
                            return;
                        }

                        c.AVATAR.setFill(new ImagePattern(img));
                    })
                    .setRequestedSize(40, 40)
                    .runRequest();

            if (Vars.richPresence != null) {
                Vars.richPresence.updateDescription("Игрок " + Config.getValue("login"));
                Vars.richPresence.updateState("Выбирает сервер");
                Vars.richPresence.disableInvite();
            }

            Thread.currentThread().interrupt();
        } else {
//            String finalCustomError = customError;
//            String finalCustomErrorSub = customErrorSub;
//
//            Platform.runLater(() -> {
//                c.NO_INTERNET_BG.setOpacity(0.35);
//                c.NO_INTERNET_TITLE.setText(finalCustomError);
//                c.NO_INTERNET_SUBTITLE.setText(finalCustomErrorSub);
//
//                c.NO_INTERNET_TITLE.getText();
//            });

            if (lastChangedPosition + 1400 < System.currentTimeMillis()) {
                StyleUtil.changePosition(c.NO_INTERNET, 0, 0, 1400);
            }
            lastChangedPosition = System.currentTimeMillis();

            if (first) {
                Platform.runLater(() -> Gui.openPane(c.AUTHORIZATION_PANE));
            }

            if (!try_load) {
                Log.err("Critical error during automatic authorization.");

                //Thread.currentThread().interrupt();
                return;
            }

            Log.err("Error during automatic authorization. Attempt to load the servers after 5 seconds...");

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
        JSONObject servers = null;

        String customError = "НЕТ ПОДКЛЮЧЕНИЯ К СЕРВЕРАМ OBVILION NETWORK",
               customErrorSub = "Проверьте подключение к сети или обратитесь к техподдержке при помощи Discord: https://discord.gg/cg82mjh";

        boolean try_load = true;

        try {
            servers = r.connectAndGetJSON_THROWS();
        } catch (Exception ex) {
            if (ex.getClass() == SSLHandshakeException.class) {
                Log.err("List servers error: broken version of Java detected!");

                try_load = false;

                customError = "ОБНАРУЖЕНА ПОЛОМАННАЯ ВЕРСИЯ JAVA";
                customErrorSub = "Скачайте Java тут https://java.com/ru/download или обратитесь к техподдержке при помощи Discord: https://discord.gg/cg82mjh";
            }

            ex.printStackTrace();
        }

        if (servers != null) {
            Vars.servers = servers.getJSONArray("servers");
            JSONArray temp = new JSONArray();

            Log.debug("Sorting servers... Count: " + Vars.servers.length());

            int index = 0;
            Map<Integer, String> _do = new HashMap<>();
            for (Object obj : Vars.servers) {
                JSONObject tec = (JSONObject) obj;

                if (tec.getString("type").equalsIgnoreCase("minecraft")) {
                    Log.debug("  + Server " + tec.getString("name"));
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
            String finalCustomError = customError;
            String finalCustomErrorSub = customErrorSub;

            Platform.runLater(() -> {
                c.NO_INTERNET_BG.setOpacity(0.35);
                c.NO_INTERNET_TITLE.setText(finalCustomError);
                c.NO_INTERNET_SUBTITLE.setText(finalCustomErrorSub);

                c.NO_INTERNET_TITLE.getText();
            });

            if (c.NO_INTERNET.getLayoutY() != 0) {

                if (lastChangedPosition + 1400 < System.currentTimeMillis()) {
                    StyleUtil.changePosition(c.NO_INTERNET, 0, 0, 1400);
                }

                lastChangedPosition = System.currentTimeMillis();
            }

            Vars.servers = new JSONArray();

            if (!try_load) {
                Log.err("Critical error on attempt to get list of servers!");

                Thread.currentThread().interrupt();
                return;
            }

            Log.err("Unable to get the list of servers. Attempt to load the servers after 5 seconds...");

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                /* Ignored */
            }

            loadServers();
        }
    }
}
