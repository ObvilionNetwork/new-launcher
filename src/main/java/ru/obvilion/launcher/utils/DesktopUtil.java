package ru.obvilion.launcher.utils;

import ru.obvilion.launcher.config.Global;
import java.net.URI;

public class DesktopUtil {
    public static boolean openWebpage(String url) {
        try {
            URI.create(url);
        } catch(Exception wrong) {
            return false;
        }

        Runtime rt = Runtime.getRuntime();

        new Thread(() -> {
            Thread.currentThread().setDaemon(true);

            try {
                String os = Global.OS.toLowerCase();

                if (os.contains("mac")) {
                    rt.exec(new String[]{ "open", url });
                } else if(os.contains("lin") || os.contains("nix") || os.contains("aix")) {
                    rt.exec(new String[]{ "xdg-open", url });
                } else if(os.contains("win")) {
                    rt.exec(new String[]{ "rundll32", "url.dll,FileProtocolHandler", url });
                }
            } catch (Exception e) {
                Log.err("Unable open web browser: {0}", e.getLocalizedMessage());
            }

            Thread.currentThread().interrupt();
        }).start();
        return true;
    }
}
