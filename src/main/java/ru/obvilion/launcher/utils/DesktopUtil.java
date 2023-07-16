package ru.obvilion.launcher.utils;

import ru.obvilion.launcher.ClientLauncherWrapper;
import ru.obvilion.launcher.config.Global;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class DesktopUtil {
    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    public static String getOs() {
        String os = "";
        String real = Global.OS.toLowerCase();

        if (real.contains("win")) {
            os = "windows";
        } else if (real.contains("lin") || real.contains("nix") || real.contains("aix")) {
            os = "linux";
        } else if (real.contains("mac")) {
            os = "macos";
        }

        os += System.getProperty("sun.arch.data.model");
        return os;
    }

    public static boolean openWebpage(String url) {
        try {
            URI.create(url);
        } catch(Exception wrong) {
            return false;
        }

        Runtime rt = Runtime.getRuntime();

        Thread th = new Thread(() -> {
            try {
                String os = Global.OS.toLowerCase();

                if (os.contains("mac")) {
                    rt.exec(new String[]{ "open", url });
                }
                else if (os.contains("lin") || os.contains("nix") || os.contains("aix")) {
                    rt.exec(new String[]{ "xdg-open", url });
                }
                else if (os.contains("win")) {
                    rt.exec(new String[]{ "rundll32", "url.dll,FileProtocolHandler", url });
                }
            } catch (Exception e) {
                Log.err("Unable open web browser: {0}", e.getLocalizedMessage());
            }

            Thread.currentThread().interrupt();
        });

        th.setDaemon(true);
        th.start();

        return true;
    }

    public static File getExecutedFile() {
        try {
            return new File(
                    ClientLauncherWrapper.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation().toURI()
            );
        } catch (Exception e) {
            Log.err("I can't get executed file info:");
            e.printStackTrace();

            return Global.LAUNCHER_HOME;
        }
    }
}
