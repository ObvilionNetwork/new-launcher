package ru.obvilion.launcher.controllers;

import com.sun.management.OperatingSystemMXBean;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import ru.obvilion.json.JSONArray;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.api.Request;
import ru.obvilion.launcher.api.RequestType;
import ru.obvilion.launcher.client.Client;
import ru.obvilion.launcher.client.Downloader;
import ru.obvilion.launcher.client.Loader;
import ru.obvilion.launcher.config.Config;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.controllers.elements.ClientMod;
import ru.obvilion.launcher.controllers.elements.Mod;
import ru.obvilion.launcher.fx.CachingImageLoader;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.gui.plugins.TaskBar;
import ru.obvilion.launcher.utils.Arrays;
import ru.obvilion.launcher.utils.DesktopUtil;
import ru.obvilion.launcher.utils.Log;
import ru.obvilion.launcher.utils.StyleUtil;
import ru.obvilion.launcher.utils.WindowMoveUtil;
import ru.obvilion.progressbar.ProgressState;

import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class FrameController implements Initializable {
    @FXML public TextArea WWWW;

    @FXML public Label DEBUG_VERSION;
    @FXML public Label DEBUG_MEMORY;
    @FXML public Label DEBUG_LASTGC;

    @FXML public Label EXIT;
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
    @FXML public HBox BOTTOM_BUTTONS;

    @FXML public Label NICKNAME;
    @FXML public Circle AVATAR;
    @FXML public Label BALANCE;

    @FXML public TextArea SERVER_DESC;
    @FXML public Arc SELECTED_SERVER_ONLINE_ARC;
    @FXML public VBox SERVERS;
    @FXML public Label SELECTED_SERVER_WIPE_DATE;
    @FXML public Label SELECTED_SERVER_NAME;
    @FXML public Label SELECTED_SERVER_ONLINE;
    @FXML public Pane SELECTED_ADDITIONAL;
    @FXML public Pane MODS_LIST;
    @FXML public ScrollPane MODS_LIST_SCROLL;
    @FXML public Pane MODS_LIST_BOX;

    @FXML public Pane TO_GAME;
    @FXML public Label TO_GAME_TEXT;
    @FXML public Pane TO_GAME_ARROW;
    @FXML public Label TO_SITE;
    @FXML public Label TO_CABINET;
    @FXML public Label TO_NEWS;
    @FXML public Label TO_FORUM;
    @FXML public Label TO_SETTINGS;
    @FXML public Label TO_RULES;

    @FXML public Pane LOADING_PANE;
    @FXML public Pane SETTINGS_PANE;

    @FXML public Pane MODS_LIST_BUTTON;
    @FXML public Pane MODS_LIST_BUTTON2;

    @FXML public Pane SERVER_DESCR;
    @FXML public Pane SERVER_DESCR2;

    @FXML public Pane CLIENT_SETTINGS_BUTTON;

    @FXML public Pane ONLINE_MAP_BUTTON;
    @FXML public Pane ONLINE_MAP_BUTTON3;

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
    public CheckBox ANIMATIONS_CB;
    public CheckBox DEV_INFO;
    public CheckBox SAVEPASS_CB;
    public CheckBox HIDE_LAUNCHER;
    public Pane DEBUG_PANE;
    public TextArea DEBUG_TEXT;
    public Label PERSENT;
    public Label STATUS;
    public Pane STATUS_L;
    public Pane DOWNLOADING_PANE;
    public Label DEBUG_BACK;
    public Label DEBUG_GO;
    public Pane NO_INTERNET;
    public Pane NO_INTERNET_BG;
    public Pane BG_TOP;
    public Label NO_INTERNET_TITLE;
    public Label NO_INTERNET_SUBTITLE;
    public Label SKIP;
    public Label SPEED;
    public Label SELECTED_SERVER_VERSION;
    public ImageView yes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Vars.selectedPane = MAIN_PANE;

        /* Top bar */
        CLOSE_BUTTON.setOnMouseClicked(e -> Runtime.getRuntime().exit(0));
        MAXIMISE_BUTTON.setOnMouseClicked(e -> Gui.maximise());
        HIDE_BUTTON.setOnMouseClicked(e -> Gui.getStage().setIconified(true));

        /* Menu buttons */
        TO_SITE.setOnMouseClicked(e -> DesktopUtil.openWebpage("https://mc.obvilion.ru"));
        TO_CABINET.setOnMouseClicked(e -> DesktopUtil.openWebpage("https://mc.obvilion.ru/cabinet"));
        TO_NEWS.setOnMouseClicked(e -> DesktopUtil.openWebpage("https://mc.obvilion.ru/news"));
        TO_FORUM.setOnMouseClicked(e -> DesktopUtil.openWebpage("https://forum.mc.obvilion.ru"));
        TO_SETTINGS.setOnMouseClicked(e -> Gui.openPane(SETTINGS_PANE));
        TO_RULES.setOnMouseClicked(e -> DesktopUtil.openWebpage("https://mc.obvilion.ru/rules"));
        SETTINGS_BACK.setOnMouseClicked(e -> Gui.openPane(MAIN_PANE));
        DEBUG_BACK.setOnMouseClicked(e -> Gui.openPane(MAIN_PANE));
        DEBUG_GO.setOnMouseClicked(e -> Gui.openPane(DEBUG_PANE));


        boolean ok = Config.getBooleanValue("devInfo", false);
        DEBUG_VERSION.setVisible(ok);
        DEBUG_VERSION.setText("Бета версия " + Global.VERSION);
        DEBUG_MEMORY.setVisible(ok);
        DEBUG_LASTGC.setVisible(ok);
        DEV_INFO.setSelected(ok);

        DEV_INFO.setOnMouseClicked(event -> {
            boolean c = !Config.getBooleanValue("devInfo");
            Config.setBoolean("devInfo", c);

            DEBUG_VERSION.setVisible(c);
            DEBUG_MEMORY.setVisible(c);
            DEBUG_LASTGC.setVisible(c);
        });

        HIDE_LAUNCHER.setSelected(Config.getBooleanValue("hideLauncher", false));
        HIDE_LAUNCHER.setOnMouseClicked(event -> {
            boolean c = !Config.getBooleanValue("hideLauncher");
            Config.setBoolean("hideLauncher", c);
        });

        SKIP.setCursor(Cursor.HAND);
        SKIP.setOnMouseClicked(e -> {
            Downloader.INSTANCE.skip = true;
            Vars.frameController.STATUS.setText("Досрочный запуск клиента...");
        });

        EXIT.setOnMouseClicked(event -> {
            Config.setValue("password", "");
            AUTH_PASSWORD.clear();
            Gui.openPane(AUTHORIZATION_PANE);
        });

        Runnable authorization = () -> {
            Request r1 = new Request(RequestType.POST, Global.API_LINK + "auth/login");
            r1.setBody(new JSONObject().put("name", AUTH_LOGIN.getText()).put("password", AUTH_PASSWORD.getText()));
            JSONObject result = r1.connectAndGetJSON();

            if (result == null) {
                Log.err("Error on connecting to API");

                NO_INTERNET_BG.setOpacity(0.35);
                NO_INTERNET_TITLE.setText("НЕТ ПОДКЛЮЧЕНИЯ К СЕРВЕРАМ OBVILION NETWORK");
                NO_INTERNET_SUBTITLE.setText("Проверьте подключение к сети или обратитесь к техподдержке при помощи Discord: https://discord.gg/cg82mjh");

                if (Loader.lastChangedPosition + 1400 < System.currentTimeMillis()) {
                    StyleUtil.changePosition(NO_INTERNET, 0, 0, 1400);
                }
                Loader.lastChangedPosition = System.currentTimeMillis();

                return;
            }

            if (!result.has("token")) {
                NO_INTERNET_BG.setOpacity(0.35);
                NO_INTERNET_TITLE.setText("ОШИБКА АВТОРИЗАЦИИ ПОЛЬЗОВАТЕЛЯ");

                if (result.getString("error").equals("User not found")) {
                    Log.debug("Invalid user: {0}", AUTH_LOGIN.getText());
                    NO_INTERNET_SUBTITLE.setText("Данного пользователя не существует. Проверьте правильность введённых данных.");
                }
                else if (result.getString("error").equals("Invalid password")) {
                    Log.debug("Invalid password for user {0}", AUTH_LOGIN.getText());
                    NO_INTERNET_SUBTITLE.setText("Введён неверный пароль пользователя. Проверьте правильность введённых данных.");
                }
                else {
                    Log.debug("Unknown error {0}", result.getString("error"));
                    NO_INTERNET_SUBTITLE.setText("Введены неверные данные. Проверьте правильность введённых данных.");
                }

                if (Loader.lastChangedPosition + 1400 < System.currentTimeMillis()) {
                    StyleUtil.changePosition(NO_INTERNET, 0, 0, 1400);
                }
                Loader.lastChangedPosition = System.currentTimeMillis();

                new Thread(() -> {
                    try {
                        Thread.sleep(4000);
                    } catch (Exception ignored) { }

                    if (Loader.lastChangedPosition + 1400 < System.currentTimeMillis()) {
                        StyleUtil.changePosition(NO_INTERNET, 0, -150, 1400);
                    }
                    Loader.lastChangedPosition = System.currentTimeMillis();

                    Thread.currentThread().interrupt();
                }).start();

                return;
            }

            Vars.userData = result;

            if (Config.getBooleanValue("savePass")) {
                Config.setPasswordValue("password", AUTH_PASSWORD.getText());
            }

            Config.setValue("token", result.getString("token"));
            Config.setValue("uuid", result.getString("uuid"));
            Config.setValue("login", result.getString("name"));
            BALANCE.setText("Баланс: " + result.getInt("money") + "p.");

            new CachingImageLoader()
                    .load(Global.API_LINK + "users/" + DesktopUtil.encodeValue(Config.getValue("login")) + "/avatar")
                    .useLoadingGif(true)
                    .setLifetime(1000 * 60 * 60)
                    .setCallback(img -> {
                        if (img.isError()) {
                            AVATAR.setFill(Color.valueOf("#192331"));
                            return;
                        }

                        AVATAR.setFill(new ImagePattern(img));
                    })
                    .setRequestedSize(40, 40)
                    .runRequest();

            NICKNAME.setText(Config.getValue("login"));

            Gui.openPane(MAIN_PANE);
            StyleUtil.createFadeAnimation(BG_TOP, 600, 0);
            BG.setStyle("-fx-background-image: url(\"" + selectedServerImage + "\");");

            if (Loader.lastChangedPosition + 1400 < System.currentTimeMillis()) {
                StyleUtil.changePosition(NO_INTERNET, 0, -150, 2400);
            }
            Loader.lastChangedPosition = System.currentTimeMillis();

            if (Vars.richPresence != null) {
                Vars.richPresence.updateDescription("Игрок " + Config.getValue("login"));
                Vars.richPresence.updateState("Выбирает сервер");
                Vars.richPresence.disableInvite();
            }
        };

        /* Authorisation */
        AUTH_DESC.setTextFormatter(new TextFormatter<String>((change) -> {
            change.setAnchor(change.getCaretPosition());
            return change;
        }));
        AUTH_BUTTON.setOnMouseClicked(e -> authorization.run());
        AUTH_LOGIN.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                AUTH_PASSWORD.requestFocus();
            }
        });
        AUTH_PASSWORD.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                authorization.run();
            }
        });

        /* Auto login */
        AUTH_LOGIN.setText(Config.getValue("login"));
        AUTH_PASSWORD.setText(Config.getPasswordValue("password"));
        BG_TOP.setStyle("-fx-background-image: url(\"images/bg.jpg\");");

//        // FIXME: при отстутствии инета долго грузит
//        Image avatar = new Image(Global.API_LINK + "users/" + DesktopUtil.encodeValue(Config.getValue("login")) + "/avatar");
//
//        if (!avatar.isError()) {
//            AVATAR.setFill(new ImagePattern(avatar));
//        } else {
//            Log.err("Error loading user avatar:");
//            avatar.getException().printStackTrace();
//
//            AVATAR.setFill(Color.valueOf("#192331"));
//        }
        NICKNAME.setText(Config.getValue("login"));

        openWebsite(REGISTER, "https://mc.obvilion.ru/auth/signup");
        openWebsite(RESTORE_PASSWORD, "https://mc.obvilion.ru/auth/restore");
        WindowMoveUtil.addMoveListener(TOP_BAR);

        /* Main */
        SERVER_DESC.setTextFormatter(new TextFormatter<String>((change) -> {
            change.setAnchor(change.getCaretPosition());
            return change;
        }));

        TO_GAME.setOnMouseClicked(event -> {
            if (Vars.selectedServer == null) {
                StyleUtil.openMessage(
                        "СЕРВЕР НЕ ВЫБРАН", "Выберите сервер перед заходом",
                        1400, 5000, 0.85F
                );
                return;
            }

            List<String> permissions = new ArrayList<>();
            JSONArray arr = Vars.userData.getJSONArray("permissions");

            for (int i = 0; i < arr.length(); i++) {
                permissions.add( arr.getString(i) );
            }

            if (Vars.selectedServer.getString("status").equals("EARLY_ACCESS") &&
                    !(permissions.contains("TEST_SERVERS") || permissions.contains("DEV_SERVERS")) ) {

                StyleUtil.openMessage(
                        "СЕРВЕР НАХОДИТСЯ В ЗАКРЫТОМ БЕТА ТЕСТЕ",
                        "Вход разрешен только тестировщикам проекта mc.obvilion.ru",
                        1400, 5000, 0.85F
                );

                return;
            }

            if (Vars.selectedServer.getString("status").equals("IN_DEV") && !permissions.contains("DEV_SERVERS")) {
                StyleUtil.openMessage(
                        "СЕРВЕР НАХОДИТСЯ В РАЗРАБОТКЕ",
                        "Вход разрешен только разработчикам проекта mc.obvilion.ru",
                        1400, 5000, 0.85F
                );
                return;
            }

            new Downloader(SELECTED_SERVER_NAME.getText(), Vars.selectedServer.getInt("clientId"));

            new Thread(() -> {
                Client client = null;

                try {
                    client = Downloader.INSTANCE.loadAll();
                } catch (Exception e) {
                    Log.err("Error on downloading client " + SELECTED_SERVER_NAME.getText());
                    e.printStackTrace();

                    Platform.runLater(() -> {
                        if (e.getMessage().contains("Specified client does not exist")) {
                            StyleUtil.openMessage(
                                    "НЕ МОГУ ЗАГРУЗИТЬ ДАННЫЕ О ЭТОМ КЛИЕНТЕ",
                                    "Данного клиента ещё нет на сервере Obvilion",
                                    1400, 5000,
                                    0.80f
                            );
                        }
                        else {
                            StyleUtil.openMessage(
                                    "НЕ МОГУ ЗАГРУЗИТЬ ДАННЫЕ О ЭТОМ КЛИЕНТЕ",
                                    "Скорее всего данного клиента ещё нет на сервере Obvilion",
                                    1400, 5000,
                                    0.80f
                            );
                        }
                    });
                }

                if (client == null) {
                    Log.delay(410, () -> {
                        Platform.runLater(() -> {
                            Gui.openPane(MAIN_PANE);
                        });
                    });

                    return;
                }

                try {
                    client.run();
                } catch (Exception e) {
                    Gui.openPane(MAIN_PANE);
                    Log.err("Error on running client: ");
                    e.printStackTrace();
                }
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

        Vars.useAnimations = Config.getBooleanValue("useAnimations", true);
        if (!Vars.useAnimations) {
            Log.info("Animations are disabled, you can turn them back on in the settings");
        }

        SAVEPASS_CB.setSelected(Config.getBooleanValue("savePass"));
        DEBUG_CB.setSelected(Config.getBooleanValue("debug"));
        AUTOUPDATE_CB.setSelected(Config.getBooleanValue("autoupdate"));
        ANIMATIONS_CB.setSelected(!Vars.useAnimations);

        DEBUG_CB.setOnMouseClicked(e -> Config.setValue("debug", DEBUG_CB.isSelected() ? "true" : "false"));
        AUTOUPDATE_CB.setOnMouseClicked(e -> Config.setValue("autoupdate", AUTOUPDATE_CB.isSelected() ? "true" : "false"));
        SAVEPASS_CB.setOnMouseClicked(e -> Config.setValue("savePass", SAVEPASS_CB.isSelected() ? "true" : "false"));
        ANIMATIONS_CB.setOnMouseClicked(e -> {
            Config.setValue("useAnimations", ANIMATIONS_CB.isSelected() ? "false" : "true");
            Vars.useAnimations = !ANIMATIONS_CB.isSelected();
        });

        MODS_LIST_BUTTON.setOnMouseClicked(event -> {
            Vars.showModsList = true;
            Vars.isOptionalModsList = false;
            loadModsList();

            StyleUtil.to(SELECTED_ADDITIONAL, MODS_LIST, 400,
                    () -> SELECTED_ADDITIONAL.setVisible(false));
        });
        MODS_LIST_BUTTON2.setOnMouseClicked(event -> {
            Vars.showModsList = true;
            Vars.isOptionalModsList = false;
            loadModsList();

            StyleUtil.to(SELECTED_ADDITIONAL, MODS_LIST, 400,
                    () -> SELECTED_ADDITIONAL.setVisible(false));
        });

        SERVER_DESCR.setOnMouseClicked(event -> {
            Vars.showModsList = false;

            StyleUtil.to(MODS_LIST, SELECTED_ADDITIONAL, 400,
                    () -> {
                        MODS_LIST.setVisible(false);
                        Vars.frameController.MODS_LIST_BOX.getChildren().clear();
                    });
        });
        SERVER_DESCR2.setOnMouseClicked(event -> {
            Vars.showModsList = false;

            StyleUtil.to(MODS_LIST, SELECTED_ADDITIONAL, 400,
                    () -> {
                        MODS_LIST.setVisible(false);
                        Vars.frameController.MODS_LIST_BOX.getChildren().clear();
                    });
        });

        CLIENT_SETTINGS_BUTTON.setOnMouseClicked(event -> {
            Vars.showModsList = true;
            Vars.isOptionalModsList = true;
            loadModsList();

            StyleUtil.to(SELECTED_ADDITIONAL, MODS_LIST, 400,
                    () -> SELECTED_ADDITIONAL.setVisible(false));
        });

        AtomicBoolean played = new AtomicBoolean(false);
        AtomicReference<Double> delta = new AtomicReference<>((double) 0);

        MODS_LIST_BOX.setOnScroll(event -> {
            event.consume();

            double deltaY = event.getDeltaY() * 0.0005 * 4172 / MODS_LIST_BOX.getHeight();
            double _vvalue = MODS_LIST_SCROLL.getVvalue();

            if (!Vars.useAnimations) {
                MODS_LIST_SCROLL.setVvalue(_vvalue + -deltaY);
                return;
            }

            delta.updateAndGet(v -> v + deltaY);

            final Animation animation = new Transition() {
                {
                    setCycleDuration(Duration.millis(10000));
                }

                double d = 0;

                protected void interpolate(double f) {
                    double c = (delta.get() - d) * 0.1;
                    d += c;

                    float op = (float) (MODS_LIST_SCROLL.getVvalue() - c);
                    MODS_LIST_SCROLL.setVvalue(Math.max(op, 0));
                }
            };

            animation.setOnFinished(event1 -> {
                played.set(false);
                delta.set((double) 0);
            });

            if (played.get()) return;

            played.set(true);
            animation.play();
        });
    }


    public static void loadModsList() {
        loadModsList(Vars.selectedServer);
    }
    public static void loadModsList(JSONObject server) {
        if (server == null || server.isNull("clientId")) {
            Pane mod = new Pane();
            mod.setPrefHeight(64);
            mod.setId("mod_item");

            Label name = new Label("Упс... Не могу найти клиент для этого сервера.");
            name.setId("mod_item_name");
            name.setLayoutX(26);
            name.setLayoutY(14);

            mod.getChildren().add(name);

            Vars.frameController.MODS_LIST_BOX.getChildren().clear();
            Vars.frameController.MODS_LIST_BOX.getChildren().add(mod);

            Log.err("Client id is not selected");
            return;
        }

        new Thread(() -> {
            Request req = new Request(RequestType.GET, Global.API_LINK + "clients/" + server.get("clientId"));
            JSONObject out = req.connectAndGetJSON();

            if (out == null || out.getInt("code") != 1000 || !out.has("data")) {
                Pane mod = new Pane();
                mod.setPrefHeight(64);
                mod.setId("mod_item");

                Label name = new Label("Упс... Произошла ошибка при загрузке данных клиента.");
                name.setId("mod_item_name");
                name.setLayoutX(26);
                name.setLayoutY(14);

                mod.getChildren().add(name);

                Platform.runLater(() -> {
                    Vars.frameController.MODS_LIST_BOX.getChildren().clear();
                    Vars.frameController.MODS_LIST_BOX.getChildren().add(mod);
                });

                Log.err(out);
                return;
            }

            if (Vars.isOptionalModsList) {
                JSONArray optional_mods = out.getJSONObject("data").getJSONArray("optionalMods");

                Vars.optionalMods.clear();

                if (Vars.clientMods.has(server.get("clientId") + "")) {
                    for (Object o : Vars.clientMods.getJSONArray(server.get("clientId") + "")) {
                        int find_id = (int) o;
                        JSONObject ok = null;

                        for (Object category : optional_mods) {
                            for (Object _mod : ((JSONObject)category).getJSONArray("mods")) {
                                JSONObject mod = (JSONObject) _mod;

                                if (mod.getInt("id") == find_id) {
                                    ok = mod;
                                    break;
                                }
                            }

                            if (ok != null) break;
                        }

                        if (ok == null) {
                            Log.warn("Client mod with id {0} not allowed on server (on fetch)", find_id);
                            continue;
                        }

                        Vars.optionalMods.add(ok);
                    }
                } else {
                    for (Object category : optional_mods) {
                        for (Object _mod : ((JSONObject)category).getJSONArray("defaultMods")) {
                            JSONObject mod = (JSONObject) _mod;
                            Vars.optionalMods.add(mod);
                        }
                    }
                    Log.warn("Can't find default client mods. Using mods from API.");
                }

                //Vars.optionalMods.add(mod_data);

                JSONArray tmp = Arrays.sortByName(optional_mods);

                Platform.runLater(() -> {
                    Vars.frameController.MODS_LIST_BOX.getChildren().clear();

                    for (Object _mod : tmp) {
                        JSONObject mod = (JSONObject) _mod;

                        Vars.frameController.MODS_LIST_BOX.getChildren().add( new ClientMod(mod) );
                    }
                });
            } else {
                JSONArray mods = out.getJSONObject("data").getJSONArray("mods");
                JSONArray temp = Arrays.sortByName(mods);

                Platform.runLater(() -> {
                    Vars.frameController.MODS_LIST_BOX.getChildren().clear();

                    for (Object _mod : temp) {
                        JSONObject mod = (JSONObject) _mod;

                        if (!Objects.equals(mod.getString("category"), "CLIENT"))
                            Vars.frameController.MODS_LIST_BOX.getChildren().add( new Mod(mod) );
                    }
                });
            }
        }).start();
    }

    public static void openWebsite(Node node, String url) {
        node.setOnMouseClicked(event -> DesktopUtil.openWebpage(url));
    }

    public static void onHover(Node node) {

    }
}