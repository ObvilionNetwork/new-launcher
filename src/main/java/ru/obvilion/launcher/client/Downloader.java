package ru.obvilion.launcher.client;

import javafx.application.Platform;
import ru.obvilion.json.JSONArray;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.config.Config;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.api.Request;
import ru.obvilion.launcher.utils.FileUtil;
import ru.obvilion.launcher.utils.Log;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class Downloader {
    public static String id;
    public static File CLIENT_DIR;
    static final String api = Global.API_LINK + "files/";

    public static void setClient(String newId) {
        id = newId;
    }

    public static Client load() {
        Log.info("Checking client {0}...", id);

        if (Vars.richPresence != null) {
            Vars.richPresence.updateDescription("Сервер: " + id);
            Vars.richPresence.updateState("Скачивает файлы игры");
        }

        Request r = new Request(Global.API_LINK + "servers/info");
        JSONObject serv = r.connectAndGetJSON();

        if (serv == null) {
            Log.err("Error getting info servers files");
            return null;
        }

        JSONArray servers = serv.getJSONArray("servers");
        JSONObject server = null;
        for (Object obj : servers) {
            JSONObject temp = (JSONObject) obj;
            if (temp.getString("name").equals(id)) server = temp;
        }

        if (server == null) {
            Log.err("Error getting info server files");
            return null;
        }

        CLIENT_DIR = new File(Global.LAUNCHER_CLIENTS, server.getString("id"));
        CLIENT_DIR.mkdir();

        long size = 0;
        size += server.getJSONObject("core").getLong("size");
        size += getModulesSize(server.getJSONArray("libraries"));
        size += getModulesSize(server.getJSONArray("natives"));
        size += getModulesSize(server.getJSONArray("mods"));
        size += getModulesSize(server.getJSONArray("assets"));
        size += getModulesSize(server.getJSONArray("other"));

        AtomicReference<Long> s = new AtomicReference<>();
        s.set(size);

        JSONObject finalServer = server;
        new Thread(() -> {
            // TODO: показывать скорость загрузки
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // TODO: фильтр лишних файлов
                long se = FileUtil.getSize(CLIENT_DIR);
                se += FileUtil.getSize(new File(CLIENT_DIR, "../assets/" + finalServer.getString("version")));

                if (!System.getProperty("java.version").startsWith("1.8"))
                se += FileUtil.getSize(new File(CLIENT_DIR, "../../common/java/" + finalServer.getString("javaVersion")));

                float persent = (float) ((double) se / (double) s.get());
                if (persent > 1) {
                    persent = 1;
                }

                float finalPersent = persent;
                Platform.runLater(() -> {
                    Vars.frameController.PERSENT.setText((int) (finalPersent * 100) + "%");
                    Vars.frameController.STATUS_L.setPrefWidth((1040 * finalPersent) * Vars.frameController.root.getWidth() / 1165);
                });

                if (persent == 1) {
                    break;
                }
            }

            Thread.currentThread().interrupt();
        }).start();

        downloadModule(server.getJSONObject("core"));
        downloadAllModules(server.getJSONArray("libraries"));
        downloadAllModules(server.getJSONArray("natives"));
        downloadAllModules(server.getJSONArray("mods"));
        downloadAllModules(server.getJSONArray("assets"));
        downloadAllModules(server.getJSONArray("other"));

        if (!System.getProperty("java.version").startsWith("1.8")) {
            JSONObject java = server.getJSONObject("java");

            String os = "";
            String real = Global.OS.toLowerCase();
            if (real.contains("win")) {
                os = "windows";
            } else if (real.contains("lin")) {
                os = "linux";
            } else if (real.contains("nix")) {
                os = "linux";
            } else if (real.contains("aix")) {
                os = "linux";
            } else if (real.contains("mac")) {
                os = "macos";
            }

            os += System.getProperty("sun.arch.data.model");

            if (java.has(os)) {
                Log.info("Downloading Java version {0}...", server.getString("javaVersion"));

                Vars.useCustomJRE = true;
                downloadAllModules(java.getJSONArray(os));

                size += getModulesSize(java.getJSONArray(os));
                s.set(size);
            } else {
                Log.err("Error downloading Java version {0} for OS {1}({2})!", server.getString("javaVersion"), os, Global.OS);
            }
        }

        if (Vars.richPresence != null) {
            Vars.richPresence.updateDescription("На сервере: " + id);
            Vars.richPresence.updateState("Игрок: " + Config.getValue("login"));
        }

        Log.info("Download ended. Starting client...");
        return new Client(server);
    }

    private static long getModulesSize(JSONArray modules) {
        long s = 0;
        for (Object m : modules) {
            JSONObject module = (JSONObject) m;
            s += module.getLong("size");
        }

        return s;
    }

    private static void downloadModule(JSONObject module) {
        String path = module.getString("path").replace("../../common/java", "../java");
        File target = new File(CLIENT_DIR, path);

        File temp = CLIENT_DIR;
        String[] sll = path.split("/");
        for (String e : sll) {
            if (sll[sll.length - 1].equals(e)) break;

            temp = new File(temp, e);
            temp.mkdir();
        }

        if (FileUtil.getSize(target) != module.getLong("size")) {
            try {
                Log.debug("Downloading file {0} ({1}KB)", module.getString("link"), module.getLong("size") / 1024 + "");

                Platform.runLater(() -> Vars.frameController.STATUS.setText("Загрузка файла " + module.getString("link")));

                int threads;
                if (module.getLong("size") / 1024 > 4096) {
                    threads = 8;
                } else if (module.getLong("size") / 1024 > 800) {
                    threads = 4;
                } else if (module.getLong("size") / 1024 > 128) {
                    threads = 2;
                } else {
                    threads = 1;
                }

                FileUtil.threadedDownload(api + module.getString("link"), target, module.getLong("size"), threads);
            } catch (Exception e) {
                Log.err("Error downloading file " + module.getString("link"));
            }
        }
    }

    private static void downloadAllModules(JSONArray modules) {
        for (Object m : modules) {
            JSONObject module = (JSONObject) m;
            downloadModule(module);
        }
    }
}
