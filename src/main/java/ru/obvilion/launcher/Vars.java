package ru.obvilion.launcher;

import javafx.application.Application;
import javafx.scene.layout.Pane;
import ru.obvilion.json.JSONArray;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.controllers.FrameController;
import ru.obvilion.launcher.controllers.ServersController;
import ru.obvilion.launcher.utils.RichPresence;

public class Vars {
    public static Application app;
    public static FrameController frameController;
    public static ServersController serversController;
    public static JSONArray servers;
    public static boolean useCustomJRE = false;
    public static boolean useAnimations = true;
    public static Process minecraft;
    public static int maxRam;
    public static Pane selectedPane;
    public static RichPresence richPresence;
    public static JSONObject selectedServer;
}
