package ru.obvilion.launcher.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.client.Loader;
import ru.obvilion.launcher.utils.StyleUtil;
import ru.obvilion.launcher.utils.WindowResizeUtil;

public class Gui extends Application {
    private static Stage stage;

    public static boolean maximised = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Vars.app = this;
        stage = primaryStage;

        final ClassLoader loader = getClass().getClassLoader();
        final FXMLLoader fxmlLoader = new FXMLLoader(loader.getResource("Frame.fxml"));
        final Parent root = fxmlLoader.load();

        Vars.frameController = fxmlLoader.getController();
        new Thread(Loader::load).start();

        root.getStylesheets().add((loader.getResource("style.css")).toExternalForm());

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(new Image(loader.getResourceAsStream("images/logo.png")));
        stage.setTitle("Obvilion Network Launcher");
        stage.setScene(new Scene(root));
        stage.show();

        stage.setMinWidth(900);
        stage.setMinHeight(520);
        stage.getScene().setFill(Color.TRANSPARENT);

        stage.getScene().getRoot().setEffect(new DropShadow(5, Color.color(0.1, 0.1, 0.1,0.93f)));

        stage.heightProperty().addListener(o -> ResizeListener.onHeightResize());
        stage.widthProperty().addListener(observable -> ResizeListener.onWidthResize());
        WindowResizeUtil.addResizeListener(stage);
    }

    public static Stage getStage() {
        return stage;
    }

    public static void maximise() {
        maximised = !maximised;

        final Screen screen = Screen.getPrimary();
        final Rectangle2D bounds = screen.getVisualBounds();

        final double height = bounds.getHeight();
        final double width = bounds.getWidth();

        double x = width > height ? height * 1.078 : width * 0.8;
        if (x < stage.getMinWidth()) {
            x = stage.getMinWidth();
        }

        double y = width > height ? height * 0.61 : width * 0.5;
        if (y < stage.getMinHeight()) {
            y = stage.getMinHeight();
        }

        if (maximised) {
            stage.setX(bounds.getMinX() - 5);
            stage.setY(bounds.getMinY() - 5);
            stage.setWidth(bounds.getWidth() + 10);
            stage.setHeight(height + 10);
        } else {
            stage.setX((bounds.getWidth() - x) / 2);
            stage.setY((height - y) / 2);
            stage.setWidth(x);
            stage.setHeight(y);
        }
    }

    public static void openPane(Pane newPane) {
        final Pane p = Vars.selectedPane;

        Platform.runLater(() -> {
            StyleUtil.createFadeAnimation(p, 400, 0);
        });

        new Thread(() -> {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Vars.selectedPane = newPane;
            Platform.runLater(() -> {
                ResizeListener.textMarginUpdate();

                newPane.setOpacity(0);
                p.setVisible(false);
                newPane.setVisible(true);
                StyleUtil.createFadeAnimation(newPane, 400, 1);
            });

            Thread.currentThread().interrupt();
        }).start();
    }

    public static void load() {
        launch();
    }
}
