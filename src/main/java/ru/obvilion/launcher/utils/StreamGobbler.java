package ru.obvilion.launcher.utils;

import javafx.application.Platform;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {
    InputStream is;
    String type;
    String log = "";

    public StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    @Override
    public void run() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (Config.getBooleanValue("debug"))
                Platform.runLater(() -> {
                    Vars.frameController.DEBUG_TEXT.appendText(log);
                    log = "";
                });
            }
        }).start();

        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String line = null;
            while ((line = br.readLine()) != null) {
                Log.custom(type, line);

                if (Config.getBooleanValue("debug")) {
                    log += (line + "\n");
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}