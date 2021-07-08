package ru.obvilion.launcher.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.utils.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class ServersController {
    FrameController c;
    public void init() {
        c = Vars.frameController;
        Vars.serversController = this;
    }

    public void setSelectedServer(JSONObject server) {
        c.SELECTED_SERVER_NAME.setText(server.getString("name"));

        c.selectedServerImage = server.getString("image");
        c.BG.setStyle("-fx-background-image: url(\"" + c.selectedServerImage + "\");");

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        c.SELECTED_SERVER_WIPE_DATE.setText(df.format(Instant.parse(server.getString("wipeDate")).getEpochSecond() * 1000));
        c.SERVER_DESC.setText(server.getString("description"));

        Pane p = null;
        for (Node n : c.SERVERS.getChildren()) {
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
        c.SELECTED_SERVER_ONLINE.setText(online1);

        c.SELECTED_SERVER_ONLINE_ARC.setLength(
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
                selected, selected_bg, name, online_label, online, statusG, status
        );
        server.setPadding(new Insets(10, 0, 10, 0));

        server.setOnMouseClicked(event -> {
            setSelectedServer(serv);
        });

        return server;
    }
 }
