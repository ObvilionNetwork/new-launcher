package ru.obvilion.launcher.gui.plugins;

import ru.obvilion.launcher.client.Downloader;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.utils.DesktopUtil;
import ru.obvilion.launcher.utils.FileUtil;
import ru.obvilion.launcher.utils.Log;
import ru.obvilion.progressbar.ProgressBar;
import ru.obvilion.progressbar.ProgressState;

import java.io.File;

public class TaskBar {
    private static boolean loaded = false;

    public static void load() {
        if (!DesktopUtil.getOs().contains("windows")) {
            return;
        }

        String bit = System.getProperty("sun.arch.data.model");

        Global.LAUNCHER_PLUGINS.mkdir();

        File native_lib = new File(Global.LAUNCHER_PLUGINS, "progress-bar-x" + bit + ".dll");

        if (!native_lib.exists()) {
            FileUtil.downloadFromURL(Downloader.api + "launcher_files/plugins/progress-bar-x" + bit + ".dll", native_lib);
        }

        try {
            ProgressBar.loadLib(native_lib);
            ProgressBar.setWindowName(Gui.getStage().getTitle());

            loaded = true;
        } catch (java.lang.UnsatisfiedLinkError e) {
            Log.err("Plugin TaskBar: Cannot load native library");
            e.printStackTrace();
        }
    }

    public static void setProgressValue(int value) {
        if (!loaded) {
            return;
        }

        ProgressBar.setProgressValue(value);
    }

    public static void setProgressState(ProgressState state) {
        if (!loaded) {
            return;
        }

        ProgressBar.setProgressState(state);
    }
}
