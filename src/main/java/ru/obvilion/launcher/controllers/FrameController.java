package ru.obvilion.launcher.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import ru.obvilion.json.JSONArray;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.api.Request;
import ru.obvilion.launcher.api.RequestType;
import ru.obvilion.launcher.client.Client;
import ru.obvilion.launcher.client.Downloader;
import ru.obvilion.launcher.client.Loader;
import ru.obvilion.launcher.config.Config;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.utils.Log;
import ru.obvilion.launcher.utils.WindowMoveUtil;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
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

            AUTHORIZATION_PANE.setVisible(false);
            MAIN_PANE.setVisible(true);
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

    public void setSelectedServer(JSONObject server) {
        SELECTED_SERVER_NAME.setText(server.getString("name"));

        selectedServerImage = server.getString("image");
        BG.setStyle("-fx-background-image: url(\"" + selectedServerImage + "\");");

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        SELECTED_SERVER_WIPE_DATE.setText(df.format(Instant.parse(server.getString("wipeDate")).getEpochSecond() * 1000));
        SERVER_DESC.setText(server.getString("description"));

        Pane p = null;
        for (Node n : SERVERS.getChildren()) {
            Pane pane = (Pane) n;
            if (pane.getChildren().get(0).isVisible()) {
                pane.getChildren().get(0).setVisible(false);
                pane.getChildren().get(1).setVisible(false);
            }

            if (pane.getId().equals(server.getString("name"))) {
                p = pane;
            }
        }

        String online1 = server.getInt("players") == -1 ? "Выкл" : server.getInt("players") + "/" + server.getInt("maxPlayers");
        SELECTED_SERVER_ONLINE.setText(online1);

        SELECTED_SERVER_ONLINE_ARC.setLength(
            server.getInt("players") == -1 ? -360 : -360 * server.getInt("players") / server.getInt("maxPlayers")
        );

        try {
            p.getChildren().get(0).setVisible(true);
            p.getChildren().get(1).setVisible(true);
        } catch (Exception e) {
            Log.err("Error loading main server");
        }
    }

    public Pane getServer(JSONObject serv) {
        Pane server = new Pane();
        server.setId(serv.getString("name"));

        Pane selected = new Pane();
        selected.setPrefWidth(1.5);
        selected.setPrefHeight(37);
        selected.setStyle("-fx-background-color: white; -fx-background-radius: 100;");
        selected.setId("sel");
        selected.setVisible(false);

        Pane selected_bg = new Pane();
        selected_bg.setStyle("-fx-background-color: linear-gradient(to right, rgba(233, 233, 233, 0.1) 0%, rgba(196, 196, 196, 0) 82.93%);");
        selected_bg.setPrefHeight(34);
        selected_bg.setPrefWidth(51);
        selected_bg.setLayoutY(1);
        selected_bg.setLayoutX(1);
        selected_bg.setId("sel1");
        selected_bg.setVisible(false);

        Label name = new Label(serv.getString("name"));
        name.setStyle("-fx-font-family: 'Istok Web Bold', sans-serif; -fx-text-fill: white; -fx-font-size: 14.5;");
        name.setLayoutX(8);
        name.setLayoutY(-2);

        Label online_label = new Label(serv.getInt("players") == -1 ? "Сервер выключен." : "Игроков онлайн:");
        online_label.setStyle("-fx-font-family: 'Istok Web Regular', sans-serif; -fx-text-fill: #ADAEB2; -fx-font-size: 12.5;");
        online_label.setLayoutX(8);
        online_label.setLayoutY(17);

        Circle statusG = new Circle(135, 16, 16, Paint.valueOf("transparent"));
        statusG.setStrokeWidth(2);
        statusG.setStroke(Paint.valueOf("#414141"));

        Arc status = serv.getInt("players") == -1 ?
                new Arc(135, 16, 16, 16, 90, -360) :
                new Arc(135, 16, 16, 16, 90, -360 * serv.getInt("players") / serv.getInt("maxPlayers"));
        status.setFill(Color.TRANSPARENT);
        status.setStrokeWidth(2);
        status.setStrokeLineCap(StrokeLineCap.ROUND);
        status.setStroke(Color.WHITE);

        String online1 = serv.getInt("players") == -1 ? "Выкл" : serv.getInt("players") + "/" + serv.getInt("maxPlayers");
        Label online = new Label(online1);
        online.setStyle("-fx-font-family: 'Istok Web Bold', sans-serif; -fx-text-fill: white; -fx-font-size: 10.2;");
        online.setLayoutX(119);
        online.setLayoutY(8);
        online.setPrefWidth(32);
        online.setAlignment(Pos.CENTER);

        server.getChildren().addAll(
            selected, selected_bg, name, online_label, status, online, statusG
        );
        server.setPadding(new Insets(10, 0, 10, 0));

        server.setOnMouseClicked(event -> {
            setSelectedServer(serv);
        });

        return server;
    }
}