package ru.obvilion.launcher.config;

import java.io.File;

public class Global {
    public static final String DISCORD_APP_ID = "657878741703327754";
    public static final String API_LINK = "https://mc.obvilion.ru/api/";
    public static final String VERSION  = "2.10.6";

    public static final boolean PRINT_GC_STATUS = false;

    public static final File USER_HOME = new File(System.getProperty("user.home"));
    public static final File LAUNCHER_HOME = new File(USER_HOME, ".ObvilionNetwork");
    public static final File LAUNCHER_CONFIG = new File(LAUNCHER_HOME, "config.properties");
    public static final File LAUNCHER_FILE_VERSIONS = new File(LAUNCHER_HOME, "file_versions.properties");
    public static final File LAUNCHER_CACHE = new File(LAUNCHER_HOME, ".cache");
    public static final File LAUNCHER_PLUGINS = new File(LAUNCHER_HOME, ".plugins");
    public static File LAUNCHER_CLIENTS = LAUNCHER_HOME;

    public static final String OS = System.getProperty("os.name");
    public static String JAVA_HOME = System.getProperty("java.home");
    public static String JAVA_BIN = JAVA_HOME +
            File.separator + "bin" +
            File.separator + "java";
}
