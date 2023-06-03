package ru.obvilion.launcher.controllers.elements;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ru.obvilion.json.JSONArray;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.config.Config;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.fx.CachingImageLoader;
import ru.obvilion.launcher.utils.Arrays;
import ru.obvilion.launcher.utils.Log;

public class ClientMod extends VBox {
    public Region parent;

    public Label name;
    public Label desc;
    public ImageView image;

    private boolean single = true;
    private CheckBox last_selected;
    private JSONArray mods;


    public ClientMod() {
        this.setId("mod_item");
        this.setStyle("-fx-padding: 15 20 20 20");

        desc = new Label("При загрузке опциональной категории не были введены данные для отображения. Скорее всего это временная недоработка.");
        desc.setId("mod_item_desc");
        desc.setWrapText(true);
        desc.setMaxWidth(640);

        this.parentProperty().addListener((observable, oldValue, newValue) -> {
            parent = (Region) newValue;

            if (parent == null) return;

            ChangeListener<Number> width_edit = (obs, oldV, newV) -> {
                desc.setMaxWidth(newV.doubleValue() - 85);
            };
            parent.prefWidthProperty().addListener(width_edit);
        });

        name = new Label("Возможно, это ошибка");
        name.setId("mod_item_name");
        name.setStyle("-fx-padding: 0 0 5 0");

        this.getChildren().addAll(name, desc);
    }

    public ClientMod(JSONObject data) {
        this();
        updateData(data);
    }

    public void updateData(JSONObject data) {
        name.setText(data.getString("name"));
        desc.setText(data.getString("description"));

        single = !data.getBoolean("multiple");

        this.getChildren().clear();
        this.getChildren().addAll(name, desc);

        this.mods = data.getJSONArray("mods");
        this.mods = Arrays.sortByName(this.mods);

        for (Object mod : this.mods) {
            this.getChildren().add(
                    getMod((JSONObject) mod)
            );
        }
    }

    private Pane getMod(JSONObject mod_data) {
        Pane mod = new Pane();

        CheckBox selected = new CheckBox();
        selected.setLayoutX(30);
        selected.setLayoutY(24);
        selected.setId(mod_data.getInt("id") + "");

        for (JSONObject obj : Vars.optionalMods) {
            if (obj.getInt("id") == mod_data.getInt("id")) {
                last_selected = selected;
                selected.setSelected(true);
                break;
            }
        }

        mod.setOnMouseClicked(event -> {
            selected.setSelected(!selected.isSelected());
        });

        selected.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (single) {
                if (last_selected != null && last_selected != selected) {
                    last_selected.setSelected(false);

                    for (int i = 0; i < Vars.optionalMods.size(); i++) {
                        if (Vars.optionalMods.get(i).getInt("id") == Integer.parseInt(last_selected.getId())) {
                            Vars.optionalMods.remove(i);
                            break;
                        }
                    }
                }

                if (newValue) {
                    Vars.optionalMods.add(mod_data);
                }
                else {
                    Vars.optionalMods.remove(mod_data);
                }

                last_selected = selected;
            }

            else if (newValue) {
                Vars.optionalMods.add(mod_data);
            } else {
                Vars.optionalMods.remove(mod_data);
            }

            JSONArray arr = new JSONArray();

            for (Object o : Vars.optionalMods) {
                JSONObject object = (JSONObject) o;
                arr.put(object.getInt("id"));
            }

            Vars.clientMods.put(Vars.selectedServer.getInt("clientId") + "", arr);
            Config.setJSONObject("clientMods", Vars.clientMods);
        });

        image = new ImageView();
        image.setLayoutX(68);
        image.setLayoutY(16);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);

        new CachingImageLoader()
                .load(Global.API_LINK + "files/" + mod_data.getString("icon"))
                .useLoadingGif(true)
                .setCallback(image::setImage)
                .setRequestedSize(40, 40)
                .runRequest();

        Label name = new Label(mod_data.getString("name"));
        name.setId("mod_item_name");
        name.setLayoutX(122);
        name.setLayoutY(9);

        Label desc = new Label(mod_data.getString("description"));
        desc.setId("mod_item_desc");
        desc.setLayoutX(122);
        desc.setLayoutY(39);
        desc.setMaxWidth(590);

        mod.getChildren().addAll(selected, image, name, desc);
        return mod;
    }
}
