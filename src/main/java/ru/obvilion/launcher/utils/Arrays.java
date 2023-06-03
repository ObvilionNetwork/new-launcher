package ru.obvilion.launcher.utils;

import ru.obvilion.json.JSONArray;
import ru.obvilion.json.JSONObject;

import java.util.*;

public class Arrays {
    public static JSONArray sortByName(JSONArray mods, Comparator comparator) {
        return sortBy(mods, "name", comparator);
    }

    public static JSONArray sortByName(JSONArray mods) {
        return sortBy(mods, "name", Map.Entry.comparingByValue());
    }

    public static JSONArray sortBy(JSONArray mods, String by, Comparator comparator) {
        JSONArray temp = new JSONArray();

        //   index ,  name
        Map<Integer, String> _do = new HashMap<>();

        for (int i = 0; i < mods.length(); i++) {
            JSONObject tec = mods.getJSONObject(i);

            _do.put(i, tec.getString(by));
        }

        List<Map.Entry<Integer, String>> entries = new ArrayList<>(_do.entrySet());
        entries.sort(comparator);

        for (Map.Entry<Integer, String> entry : entries) {
            temp.put(mods.getJSONObject(entry.getKey()));
        }

        return temp;
    }
}
