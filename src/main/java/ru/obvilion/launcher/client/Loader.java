package ru.obvilion.launcher.client;

import javafx.application.Platform;
import javafx.scene.text.Font;
import ru.obvilion.json.JSONArray;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.api.Request;
import ru.obvilion.launcher.api.RequestType;
import ru.obvilion.launcher.config.Config;
import ru.obvilion.launcher.controllers.FrameController;
import ru.obvilion.launcher.controllers.ServersController;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.gui.ResizeListener;
import ru.obvilion.launcher.utils.Log;
import ru.obvilion.launcher.utils.StyleUtil;

import java.lang.management.ManagementFactory;

public class Loader {
    public static void load() {
        final FrameController c = Vars.frameController;

        /* Get servers list */
        Request r = new Request("https://obvilionnetwork.ru/api/servers");
        JSONObject servers = r.connectAndGetJSON();

        if (servers != null) {
            Vars.servers = servers.getJSONArray("servers");
            JSONArray temp = new JSONArray();

            Log.debug("Sorting servers... Count: " + Vars.servers.length());
            for (Object obj : Vars.servers) {
                JSONObject tec = (JSONObject) obj;

                if (tec.get("type").equals("minecraft")) {
                    Log.debug("+ Server " + tec.getString("name"));
                    temp.put(tec);
                }
            }
            Vars.servers = temp;

            Log.info("Loaded servers: " + Vars.servers.length());
        } else {
            Vars.servers = new JSONArray();
            Log.err("Unable to get the list of servers");
        }

        /* Add controller */
        new ServersController().init();

        /* Add servers to list */
        boolean first = true;
        for (Object obj : Vars.servers) {
            JSONObject tec = (JSONObject) obj;

            boolean finalFirst = first;
            Platform.runLater(() -> {
                c.SERVERS.getChildren().add(Vars.serversController.getServer(tec));
                if (finalFirst) {
                    Vars.serversController.setSelectedServer(tec);
                }
            });

            first = false;
        }

        /* Auto login */
        if (!Config.getValue("login").equals("") && !Config.getValue("password").equals("")) {
            c.AUTHORIZATION_PANE.setVisible(false);
            c.LOADING_PANE.setVisible(true);

            Request r1 = new Request(RequestType.POST, "https://obvilionnetwork.ru/api/auth/login");
            r1.setBody(new JSONObject().put("name", Config.getValue("login")).put("password", Config.getPasswordValue("password")));
            JSONObject result = r1.connectAndGetJSON();

            if (result != null && result.has("token")) {
                Log.info("Automatic login to account is successful");
                Platform.runLater(() -> {
                    StyleUtil.createFadeAnimation(c.LOADING_PANE, 600, 0);

                    new Thread(() -> {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Platform.runLater(() -> {
                            ResizeListener.textMarginUpdate();

                            c.MAIN_PANE.setOpacity(0);
                            c.LOADING_PANE.setVisible(false);
                            c.MAIN_PANE.setVisible(true);
                            StyleUtil.createFadeAnimation(c.MAIN_PANE, 600, 1);
                        });
                    }).start();

                    c.BG.setStyle("-fx-background-image: url(\"" + c.selectedServerImage + "\");");
                });
            } else {
                Platform.runLater(() -> {
                    StyleUtil.createFadeAnimation(c.LOADING_PANE, 600, 0);
                });

                new Thread(() -> {
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Platform.runLater(() -> {
                        c.AUTHORIZATION_PANE.setOpacity(0);
                        c.LOADING_PANE.setVisible(false);
                        c.AUTHORIZATION_PANE.setVisible(true);
                        StyleUtil.createFadeAnimation(c.AUTHORIZATION_PANE, 600, 1);
                    });
                }).start();
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (Vars.minecraft != null) {
                Vars.minecraft.destroy();
            }
        }));


        new Thread(() -> {
            int old = 0;
            while (true) {
                int tec = (int) (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 1024 / 1024);
                if (old != tec) {
                    Log.debug(tec + "mb - heap");
                }

                old = tec;

                if (old > 40) System.gc();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
