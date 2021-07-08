package ru.obvilion.launcher.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.api.Request;
import ru.obvilion.launcher.api.RequestType;
import ru.obvilion.launcher.client.Client;
import ru.obvilion.launcher.client.Downloader;
import ru.obvilion.launcher.config.Config;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.utils.Log;
import ru.obvilion.launcher.utils.StyleUtil;
import ru.obvilion.launcher.utils.WindowMoveUtil;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class FrameController implements Initializable {
    @FXML public AnchorPane root;
    @FXML public Pane BG;
    @FXML public Pane TOP_BAR;
    @FXML public Pane CONTENT;

    @FXML public Pane CLOSE_BUTTON;
    @FXML public Pane MAXIMISE_BUTTON;
    @FXML public Pane HIDE_BUTTON;

    @FXML public Pane AUTHORIZATION_PANE;
    @FXML public TextArea AUTH_DESC;
    @FXML public Pane AUTH_BUTTON;
    @FXML public TextField AUTH_LOGIN;
    @FXML public PasswordField AUTH_PASSWORD;
    @FXML public Label REGISTER;
    @FXML public Label RESTORE_PASSWORD;
    @FXML public Label REMEMBER_PASSWORD;

    @FXML public Pane MAIN_PANE;
    @FXML public Circle AVATAR;
    @FXML public TextArea SERVER_DESC;
    @FXML public Arc SELECTED_SERVER_ONLINE_ARC;
    @FXML public VBox SERVERS;
    @FXML public Label SELECTED_SERVER_WIPE_DATE;
    @FXML public Label SELECTED_SERVER_NAME;
    @FXML public Label SELECTED_SERVER_ONLINE;
    @FXML public Pane TO_GAME;
    @FXML public Label LOADING;

    public String selectedServerImage = "";
    public Label NICKNAME;
    public Label TO_GAME_TEXT;
    public Pane LOADING_PANE;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* Top bar */
        CLOSE_BUTTON.setOnMouseClicked(e -> Runtime.getRuntime().exit(0));
        MAXIMISE_BUTTON.setOnMouseClicked(e -> Gui.maximise());
        HIDE_BUTTON.setOnMouseClicked(e -> Gui.getStage().setIconified(true));

        /* Authorisation */
        AUTH_DESC.setTextFormatter(new TextFormatter<String>((change) -> {
            change.setAnchor(change.getCaretPosition());
            return change;
        }));
        AUTH_BUTTON.setOnMouseClicked(e -> {
            Request r1 = new Request(RequestType.POST, "https://obvilionnetwork.ru/api/auth/login");
            r1.setBody(new JSONObject().put("name", AUTH_LOGIN.getText()).put("password", AUTH_PASSWORD.getText()));
            JSONObject result = r1.connectAndGetJSON();

            if (result == null || !result.has("token")) {
                //TODO
                Log.debug("Invalid user: {0}", AUTH_LOGIN.getText());
                return;
            }

            if (Config.getBooleanValue("savePass")) {
                Config.setPasswordValue("password", AUTH_PASSWORD.getText());
            }
            Config.setValue("token", result.getString("token"));
            Config.setValue("uuid", result.getString("uuid"));
            Config.setValue("login", AUTH_LOGIN.getText());

            Image avatar = new Image("https://obvilionnetwork.ru/api/users/get/" + Config.getValue("login") + "/avatar");
            if (!avatar.isError())
                AVATAR.setFill(new ImagePattern(avatar));
            NICKNAME.setText(Config.getValue("login"));

            Platform.runLater(() -> {
                StyleUtil.createFadeAnimation(AUTHORIZATION_PANE, 600, 0);
            });

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                Platform.runLater(() -> {
                    MAIN_PANE.setOpacity(0);
                    AUTHORIZATION_PANE.setVisible(false);
                    MAIN_PANE.setVisible(true);
                    StyleUtil.createFadeAnimation(MAIN_PANE, 500, 1);
                });
            }).start();

            BG.setStyle("-fx-background-image: url(\"" + selectedServerImage + "\");");
        });

        /* Auto login */
        AUTH_LOGIN.setText(Config.getValue("login"));
        AUTH_PASSWORD.setText(Config.getPasswordValue("password"));
        BG.setStyle("-fx-background-image: url(\"images/bg.jpg\");");

        Image avatar = new Image("https://obvilionnetwork.ru/api/users/get/" + Config.getValue("login") + "/avatar");
        if (!avatar.isError())
            AVATAR.setFill(new ImagePattern(avatar));
        NICKNAME.setText(Config.getValue("login"));

        openWebsite(REGISTER, "https://obvilionnetwork.ru/register");
        openWebsite(RESTORE_PASSWORD, "https://obvilionnetwork.ru/restore");
        WindowMoveUtil.addMoveListener(TOP_BAR);

        /* Main */
        SERVER_DESC.setTextFormatter(new TextFormatter<String>((change) -> {
            change.setAnchor(change.getCaretPosition());
            return change;
        }));

        TO_GAME.setOnMouseClicked(event -> {
            Downloader.setClient("HiTech");
            new Thread(() -> {
                Client client = Downloader.load();
                client.run();
            }).start();

            TO_GAME.setDisable(true);
            TO_GAME_TEXT.setText("Загрузка");
        });
    }

    public static void openWebsite(Node node, String url) {
        node.setOnMouseClicked(event -> {
            try {
                Desktop.getDesktop().browse(URI.create(url));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}