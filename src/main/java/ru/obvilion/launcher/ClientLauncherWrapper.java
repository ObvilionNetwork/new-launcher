package ru.obvilion.launcher;

import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.utils.DualStream;
import ru.obvilion.launcher.utils.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;

public class ClientLauncherWrapper {
    public static void main(String[] args) {
        if (Global.OS.toLowerCase().contains("mac")) {
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.verbose", "true");
        }

        try {
            Global.LAUNCHER_HOME.mkdirs();
            File log_file = new File(Global.LAUNCHER_HOME, "latest.log");
            log_file.createNewFile();

            PrintStream out = new PrintStream(Files.newOutputStream(log_file.toPath()));
            System.setOut(new DualStream(System.out, out));
            System.setErr(new DualStream(System.err, out));
        } catch (Exception e) {
            Log.err("Error set log file: {0}", e.getLocalizedMessage());
        }

        Log.info("Starting launcher...");
        Gui.load();
    }
}
