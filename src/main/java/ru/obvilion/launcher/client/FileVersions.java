package ru.obvilion.launcher.client;

import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.utils.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class FileVersions {
    public static Properties store;

    static {
        init();
    }

    public static void init() {
        try {
            store = new Properties();

            if (!Global.LAUNCHER_FILE_VERSIONS.exists()) {
                if (Global.LAUNCHER_FILE_VERSIONS.createNewFile()) {
                    Log.info("Config successfully created");

                    OutputStream out = new FileOutputStream(Global.LAUNCHER_FILE_VERSIONS);
                    store.store(out, null);
                    out.close();

                    init();
                }
            } else {
                FileInputStream fi = new FileInputStream(Global.LAUNCHER_FILE_VERSIONS);
                store.load(fi);
                fi.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setValue(String name, String value) {
        store.setProperty(name, value);

        try {
            OutputStream out = new FileOutputStream(Global.LAUNCHER_FILE_VERSIONS);
            store.store(out, null);
            out.close();
        } catch (IOException e) {
            Log.err("Error setting value " + name);
        }
    }

    public static String getValue(String name) {
        return store.getProperty(name);
    }
}
