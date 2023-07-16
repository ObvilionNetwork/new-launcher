package ru.obvilion.launcher;

import javafx.application.Application;
import javafx.scene.layout.Pane;
import ru.obvilion.json.JSONArray;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.controllers.FrameController;
import ru.obvilion.launcher.controllers.ServersController;
import ru.obvilion.launcher.utils.RichPresence;

import java.util.ArrayList;
import java.util.List;

public class Vars {
    public static Application app;

    public static FrameController frameController;
    public static ServersController serversController;

    public static JSONArray servers;

    /* Последние данные о пользователе */
    public static JSONObject userData = null;

    public static boolean useCustomJRE = false;
    public static boolean useAnimations = true;

    /* Указывает, открыта ли панель списка модов */
    public static boolean showModsList = false;

    /* Загружать ли опциональные моды */
    public static boolean isOptionalModsList = false;

    /* Текущая версия клиента при скачивании */
    public static String clientVersion = "1.7.10";

    /**
     *  Список в данный момент выбранных опциональных модов
     *  @apiNote Mod[]
     */
    public static List<JSONObject> optionalMods = new ArrayList<>();

    /**
     * Опциональные моды для всех клиентов из конфига
     * @apiNote { clientId: modIds[] }
     */
    public static JSONObject clientMods = new JSONObject();

    /**
     * Все клиентские моды для проверки добавляения новых модов
     * @apiNote { clientId: modIds[] }
     */
    public static JSONObject allClientMods = new JSONObject();

    public static Process minecraft;
    public static int maxRam;
    public static Pane selectedPane;
    public static RichPresence richPresence;
    public static JSONObject selectedServer;
}
