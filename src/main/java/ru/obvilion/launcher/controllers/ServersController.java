package ru.obvilion.launcher.controllers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.config.Config;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.gui.Gui;
import ru.obvilion.launcher.utils.FileUtil;
import ru.obvilion.launcher.utils.Log;
import ru.obvilion.launcher.utils.StyleUtil;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ServersController {
    FrameController c;

    public String priority;

    public void init() {

        /* Add servers to list */
        int selected = Config.getIntValue("last_server", -1);
        boolean is_first = true;
        JSONObject selected_server = null;

        for (Object obj : Vars.servers) {
            JSONObject tec = (JSONObject) obj;

            String[] s = tec.getString("image").split("/");
            File image = new File(Global.LAUNCHER_CACHE, s[s.length - 1]);

            try {
                tec.put("image_file", image.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (is_first) {
                selected_server = tec;
                is_first = false;
            }

            if (selected != -1) {
                if (tec.getInt("id") == selected) {
                    selected_server = tec;
                }
            }

            Platform.runLater(() -> {
                c.SERVERS.getChildren().add(Vars.serversController.getServer(tec));
            });
        }

        c = Vars.frameController;
        Vars.serversController = this;

        JSONObject finalSelected_server = selected_server;
        Platform.runLater(() -> {
            setSelectedServer(finalSelected_server);
        });

        Global.LAUNCHER_CACHE.mkdirs();

        for (Object obj : Vars.servers) {
            JSONObject tec = (JSONObject) obj;

            if (priority != null) {
                String[] s = priority.split("/");
                File image = new File(Global.LAUNCHER_CACHE, s[s.length - 1]);

                FileUtil.downloadFromURL(priority, image, "Downloading background image: {0}");

                try {
                    c.BG.setBackground(
                            new Background(
                                    new BackgroundImage(
                                            new Image(new FileInputStream(image.getCanonicalPath())),
                                            BackgroundRepeat.NO_REPEAT,
                                            BackgroundRepeat.NO_REPEAT,
                                            BackgroundPosition.CENTER,
                                            new BackgroundSize(0, 0, true, true, false, true)
                                    )
                            )
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }

                priority = null;
            }

            String[] s = tec.getString("image").split("/");
            File image = new File(Global.LAUNCHER_CACHE, s[s.length - 1]);

            // TODO: check images size
            if (!image.exists()) {
                FileUtil.downloadFromURL(tec.getString("image"), image, "Downloading background image: {0}");
            }

            if (priority != null && priority.contains(image.getName())) {
                try {
                    c.BG.setBackground(
                            new Background(
                                    new BackgroundImage(
                                            new Image(new FileInputStream(image.getCanonicalPath())),
                                            BackgroundRepeat.NO_REPEAT,
                                            BackgroundRepeat.NO_REPEAT,
                                            BackgroundPosition.CENTER,
                                            new BackgroundSize(0, 0, true, true, false, true)
                                    )
                            )
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }

                priority = null;
            }
        }
    }

    public void setSelectedServer(JSONObject server) {
        Vars.selectedServer = server;

        c.SELECTED_SERVER_NAME.setText(server.getString("name"));
        c.SELECTED_SERVER_VERSION.setText(server.getString("version"));

        c.selectedServerImage = server.getString("image_file");

        try {
            try {
                Image i = new Image(new FileInputStream(c.selectedServerImage));

                if (i.getException() != null) {
                    priority = server.getString("image");
                }

                c.BG.setBackground(
                        new Background(
                                new BackgroundImage(
                                        i,
                                        BackgroundRepeat.NO_REPEAT,
                                        BackgroundRepeat.NO_REPEAT,
                                        BackgroundPosition.CENTER,
                                        new BackgroundSize(0, 0, true, true, false, true)
                                )
                        )
                );
            } catch (Exception e) {
                priority = server.getString("image");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        c.SELECTED_SERVER_WIPE_DATE.setText(df.format(Instant.parse(server.getString("wipeDate")).getEpochSecond() * 1000));
        c.SERVER_DESC.setText(server.getString("description"));

        Pane p = null;
        for (Node n : c.SERVERS.getChildren()) {
            Pane pane = (Pane) n;
            if (pane.getChildren().get(0).isVisible()) {
                pane.getChildren().get(0).setVisible(false);
                pane.getChildren().get(1).setVisible(false);
                pane.setCursor(Cursor.HAND);
            }

            if (pane.getId().equals(server.getString("name"))) {
                p = pane;
            }
        }

        String online1 = server.getInt("online") == -1 ? "????????" : server.getInt("online") + "/" + server.getInt("maxOnline");
        c.SELECTED_SERVER_ONLINE.setText(online1);

        c.SELECTED_SERVER_ONLINE_ARC.setLength(
                server.getInt("online") == -1 ? -360 : -360 * server.getInt("online") / server.getInt("maxOnline")
        );

        try {
            p.getChildren().get(0).setVisible(true);
            StyleUtil.createFadeAnimation(p.getChildren().get(0), 200, 1);

            p.getChildren().get(1).setVisible(true);
        } catch (Exception e) {
            Log.err("Error loading main server");
            throw e;
        }

        Text t = (Text) c.SERVER_DESC.lookup(".text");
        double f = t.getBoundsInLocal().getHeight();
        c.SERVER_BUTTON.setLayoutY(59 * Gui.getStage().getHeight() / 660 + f + 110);
    }

    public void hoverServer(JSONObject server) {
        StyleUtil.changeText(c.SELECTED_SERVER_NAME, 400, 1, 0.6f, server.getString("name"));
        StyleUtil.changeText(c.SELECTED_SERVER_VERSION, 400, 1, 0.6f, server.getString("version"));

        c.selectedServerImage = server.getString("image_file");

        try {
            c.BG_TOP.setBackground(
                    new Background(new BackgroundImage(
                            new Image(new FileInputStream(c.selectedServerImage)),
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            new BackgroundSize(0, 0, true, true, false, true)
                    ))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        StyleUtil.to(c.BG, c.BG_TOP, 600, () -> {
            try {
                c.BG.setBackground(
                        new Background(new BackgroundImage(
                                new Image(new FileInputStream(c.selectedServerImage)),
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                new BackgroundSize(0, 0, true, true, false, true)
                        ))
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            c.BG.setOpacity(1);
        });

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        StyleUtil.changeText(c.SELECTED_SERVER_WIPE_DATE, 400, 1, 0.6f, df.format(Instant.parse(server.getString("wipeDate")).getEpochSecond() * 1000));
        StyleUtil.changeText(c.SERVER_DESC, 400, 1, 0.6f, server.getString("description"));

        String online1 = server.getInt("online") == -1 ? "????????" : server.getInt("online") + "/" + server.getInt("maxOnline");
        StyleUtil.changeText(c.SELECTED_SERVER_ONLINE, 400, 1, 0.6f, online1);

        StyleUtil.changeArc(c.SELECTED_SERVER_ONLINE_ARC, 400,
                server.getInt("online") == -1
                        ? -360
                        : -360 * server.getInt("online") / server.getInt("maxOnline"));

        Text t = (Text) c.SERVER_DESC.lookup(".text");
        double f = t.getBoundsInLocal().getHeight();
        StyleUtil.changeYPosition(c.SERVER_BUTTON, 59 * Gui.getStage().getHeight() / 660 + f + 110, 400);
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

        Label version = new Label(serv.getString("version"));
        version.setStyle("-fx-font-family: 'Istok Web Regular', sans-serif; -fx-text-fill: #bbbbbb; -fx-font-size: 13.5;");
        version.setLayoutY(-1);
        Platform.runLater(() -> {
            version.setLayoutX(name.getWidth() + 15);
        });

        Label online_label = new Label(serv.getInt("online") == -1 ? "???????????? ????????????????." : "?????????????? ????????????:");
        online_label.setStyle("-fx-font-family: 'Istok Web Regular', sans-serif; -fx-text-fill: #ADAEB2; -fx-font-size: 12.5;");
        online_label.setLayoutX(8);
        online_label.setLayoutY(17);

        Circle statusG = new Circle(150, 18, 16, Paint.valueOf("transparent"));
        statusG.setStrokeWidth(2);
        statusG.setStroke(Paint.valueOf("#414141"));

        Arc status = serv.getInt("online") == -1 ?
                new Arc(150, 18, 16, 16, 90, -360) :
                new Arc(150, 18, 16, 16, 90, -360 * serv.getInt("online") / serv.getInt("maxOnline"));
        status.setFill(Color.TRANSPARENT);
        status.setStrokeWidth(2);
        status.setStrokeLineCap(StrokeLineCap.ROUND);
        status.setStroke(Color.WHITE);

        String online1 = serv.getInt("online") == -1 ? "????????" : serv.getInt("online") + "/" + serv.getInt("maxOnline");
        Label online = new Label(online1);
        online.setStyle("-fx-font-family: 'Istok Web Bold', sans-serif; -fx-text-fill: white; -fx-font-size: 10.2;");
        online.setLayoutX(134);
        online.setLayoutY(9.5);
        online.setPrefWidth(32);
        online.setAlignment(Pos.CENTER);

        server.getChildren().addAll(
                selected, selected_bg, name, online_label, online, statusG, status, version
        );
        server.setPadding(new Insets(10, 0, 10, 0));

        server.setCursor(Cursor.HAND);
        server.setOnMouseClicked(event -> {
            Config.setValue("last_server", serv.getInt("id") + "");
            setSelectedServer(serv);
        });

        server.setOnMouseEntered(event -> {
            if (selected_bg.isVisible()) {
                return;
            }

            hoverServer(serv);

            if (selected.isVisible()) {
                return;
            }

            selected.setOpacity(0);
            selected.setVisible(true);
            StyleUtil.createFadeAnimation(selected, 100, 0.7f);
        });

        server.setOnMouseExited(event -> {
            if (selected_bg.isVisible()) {
                return;
            }

            hoverServer(Vars.selectedServer);

            StyleUtil.createFadeAnimation(selected, 100, 0);

            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Platform.runLater(() -> {
                    selected.setVisible(false);
                });
            }, "Fade timer (100ms)").start();
        });

        return server;
    }
 }
