package ru.obvilion.launcher.config;

import ru.obvilion.launcher.utils.Base64;
import ru.obvilion.launcher.utils.Log;
import ru.obvilion.launcher.utils.SystemStats;

import java.io.*;
import java.util.Properties;

public class Config {
    public static Properties config;
    public static String version = "2.0";

    static {
        init();
    }

    public static void init() {
        try {
            if (!Global.LAUNCHER_HOME.isDirectory()) {
                Global.LAUNCHER_HOME.mkdir();
            }

            config = new Properties();
            if (!Global.LAUNCHER_CONFIG.exists()) {
                if (Global.LAUNCHER_CONFIG.createNewFile()) {
                    Log.info("Config successfully created");

                    config.setProperty("login", "");
                    config.setProperty("password", "");
                    config.setProperty("uuid", "");
                    config.setProperty("token", "");
                    config.setProperty("fullscreen", "false");
                    config.setProperty("savePass", "true");
                    config.setProperty("debug", "false");
                    config.setProperty("minRam", SystemStats.recommendedMin()+"");
                    config.setProperty("maxRam", SystemStats.recommendedMax()+"");
                    config.setProperty("clientsDir", "");
                    config.setProperty("javaDir", "");
                    config.setProperty("lastServer", "");
                    config.setProperty("optionalMods", "");
                    config.setProperty("version", version);
                    config.setProperty("useAnimations", "true");

                    OutputStream out = new FileOutputStream(Global.LAUNCHER_CONFIG);
                    config.store(out, null);
                    out.close();

                    init();
                }
            } else {
                FileInputStream fi = new FileInputStream(Global.LAUNCHER_CONFIG);
                config.load(fi);
                fi.close();

                if (!version.equals(config.getProperty("version"))) {
                    if (Global.LAUNCHER_CONFIG.delete()) {
                        init();
                    } else {
                        Log.err("Unable to remove config file :(");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setValue(String name, String value) {
        config.setProperty(name, value);

        try {
            OutputStream out = new FileOutputStream(Global.LAUNCHER_CONFIG);
            config.store(out, null);
            out.close();
        } catch (IOException e) {
            Log.err("Error setting value " + name);
        }
    }

    public static void setPasswordValue(String name, String value) {
        setValue(name, Base64.encrypt(value, "ljXocw9WGiwRxn8oIrXn3HfOvEInrOy"));
    }

    public static String getValue(String name) {
        return config.getProperty(name);
    }

    public static String getPasswordValue(String name) {
        if (config.getProperty(name) == null) return null;

        return Base64.decrypt(config.getProperty(name), "ljXocw9WGiwRxn8oIrXn3HfOvEInrOy");
    }

    public static int getIntValue(String name, int def) {
        try {
            return Integer.parseInt(getValue(name));
        } catch (Exception e) {
            return def;
        }
    }

    public static int getIntValue(String name) {
        try {
            return Integer.parseInt(getValue(name));
        } catch (Exception e) {
            Log.err(e);
            return 0;
        }
    }

    public static boolean getBooleanValue(String name) {
        try {
            return Boolean.parseBoolean(getValue(name));
        } catch (Exception e) {
            Log.err(e);
            return false;
        }
    }

    public static boolean getBooleanValue(String name, boolean def) {
        String v = getValue(name);

        if (v == null) {
            setValue(name, def ? "true" : "false");
            return def;
        }

        return Boolean.parseBoolean(v);
    }
}
