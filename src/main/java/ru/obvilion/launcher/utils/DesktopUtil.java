package ru.obvilion.launcher.utils;

import java.awt.*;
import java.net.URI;

public class DesktopUtil {
    public static boolean openWebpage(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
