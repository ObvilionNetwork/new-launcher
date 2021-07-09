package ru.obvilion.launcher.gui;

import javafx.application.Platform;
import javafx.scene.text.Font;
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
        Platform.runLater(() -> {
            Font font = Font.font("Istok Web Bold", 8 * Gui.getStage().getWidth() / 1165 + 8);
            Vars.frameController.TO_CABINET.fontProperty().setValue(font);
            Vars.frameController.TO_RULES.setFont(font);
            Vars.frameController.TO_FORUM.setFont(font);
            Vars.frameController.TO_NEWS.setFont(font);
            Vars.frameController.TO_SETTINGS.setFont(font);
            Vars.frameController.TO_SITE.setFont(font);

            font = Font.font("Istok Web Bold", 14 * Gui.getStage().getWidth() / 1165 + 7);
            Vars.frameController.TO_GAME_TEXT.setFont(font);
        });

        Text t = (Text) Vars.frameController.SERVER_DESC.lookup(".text");
        double f = t.getBoundsInLocal().getHeight();
        Vars.frameController.server_button.setLayoutY(59 * Gui.getStage().getHeight() / 660 + f + 110);

        new Thread(() -> {
            try {
                Thread.sleep(20);
            } catch (Exception e) {

            }

            Platform.runLater(() -> {
                double f1 = t.getBoundsInLocal().getHeight();
                Vars.frameController.server_button.setLayoutY(59 * Gui.getStage().getHeight() / 660 + f1 + 110);
            });

            Thread.currentThread().interrupt();
        }).start();
    }
}
