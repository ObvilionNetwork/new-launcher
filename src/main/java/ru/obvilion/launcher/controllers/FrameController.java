package ru.obvilion.launcher.controllers;

import com.sun.management.OperatingSystemMXBean;
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
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.api.Request;
import ru.obvilion.launcher.api.RequestType;
import ru.obvilion.launcher.client.Client;
import ru.obvilion.launcher.client.Downloader;
import ru.obvilion.launcher.config.Config;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.gui.ResizeListener;
import ru.obvilion.launcher.utils.DesktopUtil;
import ru.obvilion.launcher.utils.Log;
import ru.obvilion.launcher.utils.StyleUtil;
import ru.obvilion.launcher.utils.WindowMoveUtil;

import java.awt.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
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

    @FXML public Label NICKNAME;
    @FXML public Circle AVATAR;
    @FXML public Label BALANCE;

    @FXML public TextArea SERVER_DESC;
    @FXML public Arc SELECTED_SERVER_ONLINE_ARC;
    @FXML public VBox SERVERS;
    @FXML public Label SELECTED_SERVER_WIPE_DATE;
    @FXML public Label SELECTED_SERVER_NAME;
    @FXML public Label SELECTED_SERVER_ONLINE;

    @FXML public Pane TO_GAME;
    @FXML public Label TO_GAME_TEXT;
    @FXML public Label TO_SITE;
    @FXML public Label TO_CABINET;
    @FXML public Label TO_NEWS;
    @FXML public Label TO_FORUM;
    @FXML public Label TO_SETTINGS;
    @FXML public Label TO_RULES;

    @FXML public Pane LOADING_PANE;
    @FXML public Pane SETTINGS_PANE;
    @FXML public Pane SERVER_BUTTON;

    public String selectedServerImage = "";
    public Circle curRamMin;
    public Label RAM_MIN;
    public Circle curRamMax;
    public Label RAM_MAX;
    public Pane RAM_WIDTH;
    public Pane RAM_SELECTOR;
    public Label SETTINGS_BACK;
    public CheckBox DEBUG_CB;
    public CheckBox AUTOUPDATE_CB;
    public CheckBox SAVEPASS_CB;
    public Pane DEBUG_PANE;
    public TextArea DEBUG_TEXT;
    public Label PERSENT;
    public Label STATUS;
    public Pane STATUS_L;
    public Pane DOWNLOADING_PANE;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Vars.selectedPane = MAIN_PANE;

        /* Top bar */
        CLOSE_BUTTON.setOnMouseClicked(e -> Runtime.getRuntime().exit(0));
        MAXIMISE_BUTTON.setOnMouseClicked(e -> Gui.maximise());
        HIDE_BUTTON.setOnMouseClicked(e -> Gui.getStage().setIconified(true));

        /* Menu buttons */
        TO_SITE.setOnMouseClicked(e -> DesktopUtil.openWebpage("https://obvilionnetwork.ru"));
        TO_CABINET.setOnMouseClicked(e -> DesktopUtil.openWebpage("https://obvilionnetwork.ru/cabinet"));
        TO_NEWS.setOnMouseClicked(e -> DesktopUtil.openWebpage("https://obvilionnetwork.ru/news"));
        TO_FORUM.setOnMouseClicked(e -> DesktopUtil.openWebpage("https://forum.obvilionnetwork.ru"));
        TO_SETTINGS.setOnMouseClicked(e -> Gui.openPane(SETTINGS_PANE));
        TO_RULES.setOnMouseClicked(e -> DesktopUtil.openWebpage("https://obvilionnetwork.ru/rules"));
        SETTINGS_BACK.setOnMouseClicked(e -> Gui.openPane(MAIN_PANE));

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

            Gui.openPane(MAIN_PANE);
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
            Downloader.setClient(SELECTED_SERVER_NAME.getText());
            new Thread(() -> {
                Client client = Downloader.load();
                client.run();
            }).start();

            Gui.openPane(DOWNLOADING_PANE);
        });

        int min = Config.getIntValue("minRam");
        int max =  Config.getIntValue("maxRam");
        int maxRam = (int) (((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() / 1024 / 1024);
        Vars.maxRam = maxRam;

        RAM_MIN.setText("от " + (float)((int) (min / 1024f * 100)) / 100 + "ГБ");
        RAM_MAX.setText("до " + (float)((int) (max / 1024f * 100)) / 100 + "ГБ");
        curRamMin.setLayoutX((float)(min) / maxRam * RAM_WIDTH.getWidth());
        curRamMax.setLayoutX((float)(max) / maxRam * RAM_WIDTH.getWidth());

        curRamMin.setOnMouseDragged(event -> {
            double newX = event.getSceneX() - RAM_SELECTOR.getLayoutX();

            if (newX < 0) {
                newX = 0;
            } else if (newX > curRamMax.getLayoutX() - 20) {
                newX = curRamMax.getLayoutX() - 20;
            }

            curRamMin.setLayoutX(newX);
            int size = (int) (newX / RAM_WIDTH.getWidth() * (maxRam - 200)) + 200;

            Config.setValue("minRam", size + "");
            RAM_MIN.setText("от " + (float)((int) (size / 1024f * 100)) / 100 + "ГБ");
        });
        curRamMax.setOnMouseDragged(event -> {
            double newX = event.getSceneX() - RAM_SELECTOR.getLayoutX();

            if (newX < curRamMin.getLayoutX() + 20) {
                newX = curRamMin.getLayoutX() + 20;
            } else if (newX > RAM_WIDTH.getWidth()) {
                newX = RAM_WIDTH.getWidth();
            }

            curRamMax.setLayoutX(newX);
            int size = (int) (newX / RAM_WIDTH.getWidth() * (maxRam - 200)) + 200;

            Config.setValue("maxRam", size + "");
            RAM_MAX.setText("до " + (float)((int) (size / 1024f * 100)) / 100 + "ГБ");
        });

        SAVEPASS_CB.setSelected(Config.getBooleanValue("savePass"));
        DEBUG_CB.setSelected(Config.getBooleanValue("debug"));
        AUTOUPDATE_CB.setSelected(Config.getBooleanValue("autoupdate"));

        DEBUG_CB.setOnMouseClicked(e -> Config.setValue("debug", DEBUG_CB.isSelected() ? "true" : "false"));
        AUTOUPDATE_CB.setOnMouseClicked(e -> Config.setValue("autoupdate", AUTOUPDATE_CB.isSelected() ? "true" : "false"));
        SAVEPASS_CB.setOnMouseClicked(e -> Config.setValue("savePass", SAVEPASS_CB.isSelected() ? "true" : "false"));
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