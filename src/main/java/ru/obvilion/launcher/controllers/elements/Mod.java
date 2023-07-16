package ru.obvilion.launcher.controllers.elements;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import javafx.scene.layout.Region;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.fx.CachingImageLoader;

public class Mod extends Region {
    public Pane parent;

    public Label name;
    public Label desc;
    public Label version;
    public ImageView image;

    public Mod() {
        this.setPrefHeight(64);
        this.setId("mod_item");

        desc = new Label();
        desc.setId("mod_item_desc");
        desc.setLayoutX(68);
        desc.setLayoutY(31);
        desc.setMaxWidth(663);

        this.parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            parent = (Pane) newValue;

            ChangeListener<Number> width_edit = (obs, oldV, newV) -> {
                //this.setPrefWidth();
                desc.setMaxWidth(newV.doubleValue() - 85);
            };
            parent.prefWidthProperty().addListener(width_edit);
        });

        name = new Label();
        name.setId("mod_item_name");
        name.setLayoutX(68);
        name.setLayoutY(5);

        version = new Label();
        version.setId("mod_item_version");
        version.setLayoutY(8);

        name.widthProperty().addListener((observable, oldValue, newValue) -> {
            version.setLayoutX(78 + newValue.doubleValue());
        });

        image = new ImageView();
        image.setLayoutX(13);
        image.setLayoutY(13);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);

        this.getChildren().addAll(name, version, desc, image);
    }

    public Mod(JSONObject data) {
        this();
        updateData(data);
    }

    public void updateData(JSONObject data) {

        name.setText(data.getString("name"));
        desc.setText(data.getString("description"));
        version.setText(data.getString("version"));

        new CachingImageLoader()
                .load(Global.API_LINK + "files/" + data.getString("icon"))
                .useLoadingGif(true)
                .setCallback(image::setImage)
                .setLifetime(1000 * 60 * 60 * 24 * 7)
                .setRequestedSize(40, 40)
                .runRequest();
    }
}
