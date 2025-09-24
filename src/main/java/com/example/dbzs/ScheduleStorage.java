// ScheduleStorage.java
package com.example.dbzs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ScheduleStorage {
    private static final String PREF_NAME = "shift_schedules";
    private static final String KEY_SCHEDULES = "schedules";

    public static List<NamedShiftSchedule> getSchedules(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SCHEDULES, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<NamedShiftSchedule>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public static void saveSchedules(Context context, List<NamedShiftSchedule> schedules) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_SCHEDULES, new Gson().toJson(schedules));
        editor.apply();
    }

    public static NamedShiftSchedule getActiveSchedule(Context context) {
        List<NamedShiftSchedule> schedules = getSchedules(context);
        for (NamedShiftSchedule schedule : schedules) {
            if (schedule.isActive()) {
                return schedule;
            }
        }
        return null;
    }

    public static void setActiveSchedule(Context context, String scheduleName) {
        List<NamedShiftSchedule> schedules = getSchedules(context);
        for (NamedShiftSchedule schedule : schedules) {
            schedule.setActive(schedule.getName().equals(scheduleName));
        }
        saveSchedules(context, schedules);
    }
}