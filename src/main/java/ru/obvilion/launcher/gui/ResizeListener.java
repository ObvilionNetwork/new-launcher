package ru.obvilion.launcher.gui;

import javafx.application.Platform;
import javafx.scene.text.Text;
import ru.obvilion.launcher.Vars;

public class ResizeListener {
    public static void onHeightResize() {
        textMarginUpdate();
    }

    public static void onWidthResize() {
        textMarginUpdate();
    }

    public static void textMarginUpdate() {
        new Thread(() -> {
            try {
                Thread.sleep(20);
            } catch (Exception e) {

            }

            Platform.runLater(() -> {
                Text t = (Text) Vars.frameController.SERVER_DESC.lookup(".text");
                double f = t.getBoundsInLocal().getHeight();
                Vars.frameController.server_button.setLayoutY(59 * Gui.getStage().getHeight() / 660 + f + 110);
            });

            Thread.currentThread().interrupt();
        }).start();
    }
}
