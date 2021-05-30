package ru.obvilion.launcher.utils;

import java.util.ArrayList;
import java.util.List;

class Range {
    public List<String> rangeList;

    Range(long startBytes, long endBytes, int c_thread) {
        rangeList = getThreadsDiapason(startBytes, endBytes, c_thread);
    }

    public String getRangeString(int rangeId) {
        if (rangeList.size() < rangeId)
            return null;
        else
            return rangeList.get(rangeId);
    }

    public long getFirstRangeInt(int rangeId) {
        if (rangeList.size() < rangeId)
            return 0;
        else
            return Long.parseLong(rangeList.get(rangeId).split("-")[0]);
    }

    public long getLastRangeInt(int rangeId) {
        if (rangeList.size() < rangeId)
            return 0;
        else
            return Long.parseLong(rangeList.get(rangeId).split("-")[1]);
    }

    private List<String> getThreadsDiapason(long startBytes, long endBytes, int c_thread) {
        long step = (long)(endBytes / c_thread);
        List<Long> temp_start = new ArrayList<>();
        List<Long> temp_end = new ArrayList<>();
        temp_start.add(startBytes);
        temp_end.add(step);

        try {
            for (int i = 1; i < c_thread; i++) {
                temp_start.add(i, temp_end.get(i - 1) + 1);
                temp_end.add(i, temp_end.get(i - 1) + step);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        long last_value = temp_end.get(c_thread - 1) + (endBytes - temp_end.get(c_thread - 1));
        temp_end.set(c_thread - 1, last_value);

        List<String> finalList = new ArrayList < > ();
        try {
            for (int i = 0; i < c_thread; i++) {
                finalList.add(temp_start.get(i) + "-" + temp_end.get(i));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return finalList;
    }
}