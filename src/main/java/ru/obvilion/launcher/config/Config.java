package ru.obvilion.launcher.config;

import ru.obvilion.launcher.utils.Base64;
import ru.obvilion.launcher.utils.Log;

import java.io.*;
import java.util.Properties;

public class Config {
    public static Properties config;

    static {
        try {
            if(!Global.LAUNCHER_HOME.isDirectory()) {
                Global.LAUNCHER_HOME.mkdir();
            }

            config = new Properties();
            if(!Global.LAUNCHER_CONFIG.exists()) {
                if(Global.LAUNCHER_CONFIG.createNewFile()) {
                    Log.info("Config successfully created");
                    config.load(new FileInputStream(Global.LAUNCHER_CONFIG));

                    config.setProperty("login", "");
                    config.setProperty("password", "");
                    config.setProperty("uuid", "");
                    config.setProperty("token", "");
                    config.setProperty("fullscreen", "false");
                    config.setProperty("savePass", "true");
                    config.setProperty("debug", "false");
                    config.setProperty("minRam", "256");
                    config.setProperty("maxRam", "1024");
                    config.setProperty("clientsDir", "");
                    config.setProperty("javaDir", "");
                    config.setProperty("lastServer", "");
                    config.setProperty("optionalMods", "");
                }
            } else {
                config.load(new FileInputStream(Global.LAUNCHER_CONFIG));
            }

            OutputStream out = new FileOutputStream(Global.LAUNCHER_CONFIG);
            config.store(out, null);
            out.close();
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

    public static int getIntValue(String name) {
        return Integer.parseInt(getValue(name));
    }

    public static boolean getBooleanValue(String name) {
        return Boolean.parseBoolean(getValue(name));
    }
}
