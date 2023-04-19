package ru.obvilion.launcher.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log {
    private static final String
            log    = "[L]: ", warn  = "[W]: ",
            err    = "[E]: ", debug = "[D]: ",
            custom = "[%]: ";

    private static String getTime() {
        return new SimpleDateFormat("[HH:mm:ss]").format(Calendar.getInstance().getTime());
    }
    private static String getMessage(String message, Object... n) {
        for(int i = 0; i < n.length; i++) {
            message = message.replace("{"+i+"}", n[i].toString());
        }
        return message;
    }
    private static void print(String message) {
        System.out.println(message);
    }

    public static void info(Object message) {
        print(getTime() + log + message);
    }
    public static void info(String message, Object... n) {
        print(getTime() + log + getMessage(message, n));
    }

    public static void warn(Object message) {
        print(getTime() + warn + message);
    }
    public static void warn(String message, Object... n) {
        print(getTime() + warn + getMessage(message, n));
    }

    public static void err(Object message) {
        print(getTime() + err + message);
    }
    public static void err(String message, Object... n) {
        print(getTime() + err + getMessage(message, n));
    }

    public static void debug(Object message) {
        print(getTime() + debug + message);
    }
    public static void debug(String message, Object... n) {
        print(getTime() + debug + getMessage(message, n));
    }

    public static void custom(String name, Object message) {
        print(getTime() + custom.replace("%", name) + message);
    }
    public static void custom(String name, String message, Object... n) {
        print(getTime() + custom.replace("%", name) + getMessage(message, n));
    }
}
