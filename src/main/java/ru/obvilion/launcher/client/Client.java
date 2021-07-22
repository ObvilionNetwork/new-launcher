package ru.obvilion.launcher.client;

import javafx.application.Platform;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.config.Config;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.utils.Log;
import ru.obvilion.launcher.utils.StreamGobbler;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Client {
    public String name;
    public String version;
    public String javaVersion;
    public String core;
    public File clientDir;
    public File javaFile;

    public Client(JSONObject info) {
        this.name = info.getString("name");
        this.version = info.getString("version");
        this.javaVersion = info.getString("javaVersion");
        this.core = info.getJSONObject("core").getString("type");

        clientDir = new File(Global.LAUNCHER_CLIENTS, info.getString("id"));
        javaFile = new File(clientDir, "../java/" + javaVersion + "/bin");
        javaFile = new File(javaFile, Global.OS.toLowerCase().contains("win") ? "java.exe" : "java");
    }

    public String getCmd() {
        String cmd;

        if (!Vars.useCustomJRE) {
            cmd = "java ";
        } else {
            cmd = javaFile + " ";
        }

        cmd += "-Xms" + Config.getValue("minRam") + "m "; // Минимальное кол-во озу
        cmd += "-Xmx" + Config.getValue("maxRam") + "m "; // Максимальное кол-во озу
        cmd += "-Djava.library.path=" + new File(clientDir, "natives").getPath() + " ";
        cmd += "-cp " + new Classpath(new File(clientDir, "libraries")).getCmd() + new File(clientDir, "forge.jar").getPath() +  " ";
        cmd += "-Duser.language=ru ";

        cmd += "net.minecraft.launchwrapper.Launch ";

        cmd += "--username " + Config.getValue("login") + " ";
        cmd += "--version " + this.core + " " + this.version + " ";
        cmd += "--gameDir " + clientDir.getPath() + " ";
        cmd += "--assetsDir " + new File(Global.LAUNCHER_CLIENTS, "assets/" + this.version) + " ";
        cmd += "--assetIndex " + this.version + " ";
        cmd += "--uuid " + Config.getValue("uuid") + " "; // Айди, выдается при авторизации
        cmd += "--accessToken " + Config.getValue("token") + " "; // Токен доступа, выдается при авторизации
        cmd += "--userProperties [] ";
        cmd += "--userType legacy ";
        cmd += "--tweakClass " + (this.version.equals("1.7.10") ? "cpw.mods.fml.common.launcher.FMLTweaker" : "net.minecraftforge.fml.common.launcher.FMLTweaker") + " ";
        return cmd;
    }

    public void run() {
        final String cmd = getCmd();
        final AtomicInteger exit = new AtomicInteger(-1);

        Process ps = null;
        Log.custom("CMD", cmd);

        try {
            ps = Runtime.getRuntime().exec(cmd, null, clientDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Vars.minecraft = ps;

        StreamGobbler errorGobbler = new StreamGobbler(ps.getErrorStream(), "MC");
        StreamGobbler outputGobbler = new StreamGobbler(ps.getInputStream(), "MC");
        errorGobbler.start();
        outputGobbler.start();

        Process finalPs = ps;
        Platform.runLater(() -> {
            if (Config.getBooleanValue("debug")) {
                Gui.openPane(Vars.frameController.DEBUG_PANE);
            } else {
                Gui.getStage().hide();
            }

            try {
                exit.set(finalPs.waitFor()); // Ждем когда майн закроется

                errorGobbler.stop();
                outputGobbler.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.info("Process closed");
            if (Config.getBooleanValue("debug")) {
                // TODO: добавить кнопку выхода из панели дебага
                Gui.openPane(Vars.frameController.MAIN_PANE);
            } else {
                Gui.getStage().show();
                Gui.openPane(Vars.frameController.MAIN_PANE);
            }
        });
    }
}

class Classpath {
    public File dir;

    public Classpath(File dir) {
        this.dir = dir;
    }

    public String getCmd() {
        String cmd = "";

        String separator = Global.OS.toLowerCase().contains("win") ? ";" : ":";

        for(File f : dir.listFiles()) {
            if(f.isDirectory()) {
                cmd += new Classpath(f).getCmd();
            } else {
                cmd += f.getPath() + separator;
            }
        }

        return cmd;
    }
}
