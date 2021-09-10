package ru.obvilion.launcher.config;

import java.io.File;

public class Global {
    public static final String API_LINK = "https://obvilionnetwork.ru/api/";
    public static final String VERSION  = "2.7dev";
    public static final File USER_HOME = new File(System.getProperty("user.home"));
    public static final File LAUNCHER_HOME = new File(USER_HOME, ".ObvilionNetwork");
    public static final File LAUNCHER_CONFIG = new File(LAUNCHER_HOME, "config.properties");
    public static File LAUNCHER_CLIENTS = LAUNCHER_HOME;

    public static final String OS = System.getProperty("os.name");
    public static String JAVA_HOME = System.getProperty("java.home");
    public static String JAVA_BIN = JAVA_HOME +
            File.separator + "bin" +
            File.separator + "java";
}
