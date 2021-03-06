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
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
            javaFile = new File(
                    System.getProperty("java.home"), "" +
                    "bin/java" + (Global.OS.toLowerCase().contains("win") ? ".exe" : "")
            );
        }

        try {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr-x");
            Files.setPosixFilePermissions(javaFile.toPath(), perms);
        } catch (UnsupportedOperationException e) {
            // Ignored
        } catch (Exception e) {
            Log.err("Error set posix java file permission:");
            e.printStackTrace();
        }

        try {
            if (Global.OS.toLowerCase().contains("win")) {
                cmd = "\"" + javaFile.getCanonicalPath() + "\" ";
            } else {
                cmd = javaFile.getCanonicalPath() + " ";
            }
        } catch (IOException e) {
            cmd = "\"" + javaFile.getPath() + "\" ";
            e.printStackTrace();
        }

        String quote = Global.OS.toLowerCase().contains("win") ? "\"" : "";

        cmd += "-Xms" + Config.getValue("minRam") + "m "; // ?????????????????????? ??????-???? ??????
        cmd += "-Xmx" + Config.getValue("maxRam") + "m "; // ???????????????????????? ??????-???? ??????
        cmd += "-Djava.library.path=" + quote + new File(clientDir, "natives").getPath() + quote + " ";
        cmd += "-cp " + new Classpath(new File(clientDir, "libraries")).getCmd() + quote + new File(clientDir, "forge.jar").getPath() + quote + " ";
        cmd += "-Duser.language=ru ";

        cmd += "net.minecraft.launchwrapper.Launch ";

        cmd += "--username " + Config.getValue("login") + " ";
        cmd += "--version " + this.core + " " + this.version + " ";
        cmd += "--gameDir " + quote + clientDir.getPath() + quote +" ";
        cmd += "--assetsDir " + quote + new File(Global.LAUNCHER_CLIENTS, "assets/" + this.version) + quote + " ";
        cmd += "--assetIndex " + this.version + " ";
        cmd += "--uuid " + Config.getValue("uuid") + " "; // ????????, ???????????????? ?????? ??????????????????????
        cmd += "--accessToken " + Config.getValue("token") + " "; // ?????????? ??????????????, ???????????????? ?????? ??????????????????????
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

        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            if (Config.getBooleanValue("debug")) {
                Gui.openPane(Vars.frameController.DEBUG_PANE);
                Vars.frameController.DEBUG_BACK.setVisible(false);
            } else {
                Gui.getStage().close();
            }
        });

        try {
            exit.set(finalPs.waitFor()); // ???????? ?????????? ???????? ??????????????????

            errorGobbler.stop();
            outputGobbler.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Vars.richPresence != null) {
            Vars.richPresence.updateDescription("?????????? " + Config.getValue("login"));
            Vars.richPresence.updateState("???????????????? ????????????");
            Vars.richPresence.disableInvite();
        }

        Log.info("Process closed");
        Platform.runLater(() -> {
            if (Config.getBooleanValue("debug")) {
                Vars.frameController.DEBUG_BACK.setVisible(true);
            } else {
                Gui.getStage().show();
                Vars.selectedPane.setOpacity(0);
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

        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                cmd += new Classpath(f).getCmd();
            }
            else if (Global.OS.toLowerCase().contains("win")) {
                cmd += "\"" + f.getPath() + "\"" + separator;
            }
            else {
                cmd += f.getPath() + separator;
            }
        }

        return cmd;
    }
}
