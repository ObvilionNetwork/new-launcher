package ru.obvilion.launcher.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.client.Loader;
import ru.obvilion.launcher.utils.WindowResizeUtil;

public class Gui extends Application {
    private static Stage stage;

    public static boolean maximised = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
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

        stage.setMinWidth(850);
        stage.setMinHeight(500);
        stage.getScene().setFill(Color.TRANSPARENT);

        stage.getScene().getRoot().setEffect(new DropShadow(5, Color.color(0.1, 0.1, 0.1,0.93f)));

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
        final double x = width > height ? height * 1.078 : width * 0.8;
        final double y = width > height ? height * 0.61 : width * 0.5;

        if (maximised) {
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(height);
        } else {
            stage.setX((bounds.getWidth() - x) / 2);
            stage.setY((height - y) / 2);
            stage.setWidth(x);
            stage.setHeight(y);
        }
    }

    public static void load() {
        launch();
    }
}
