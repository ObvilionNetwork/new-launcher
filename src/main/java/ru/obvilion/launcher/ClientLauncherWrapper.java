package ru.obvilion.launcher;

import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.utils.DualStream;
import ru.obvilion.launcher.utils.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class ClientLauncherWrapper {
    public static void main(String[] args) {
        try {
            Global.LAUNCHER_HOME.mkdirs();
            PrintStream out = new PrintStream(new FileOutputStream(new File(Global.LAUNCHER_HOME, "latest.log")));
            System.setOut(new DualStream(System.out, out));
            System.setErr(new DualStream(System.err, out));
        } catch (Exception e) {
            Log.err("Error set log file: {0}", e.getLocalizedMessage());
        }

        Log.info("Starting launcher...");
        Gui.load();
    }
}
