package ru.obvilion.launcher;

import ru.obvilion.json.JSONArray;
import ru.obvilion.launcher.controllers.FrameController;
import ru.obvilion.launcher.controllers.ServersController;

public class Vars {
    public static FrameController frameController;
    public static ServersController serversController;
    public static JSONArray servers;
    public static boolean useCustomJRE = false;
    public static Process minecraft;
    public static int maxRam;
}
