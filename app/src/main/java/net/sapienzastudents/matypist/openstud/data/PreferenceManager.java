package net.sapienzastudents.matypist.openstud.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public class PreferenceManager {
    public final static boolean BIOMETRIC_FEATURE_AVAILABLE = true;
    private static SharedPreferences pref;
    private static List<CustomCourse> courses;

    private static synchronized void setupSharedPreferences(Context context) {
        if (pref != null) return;
        pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean getStatsNotificationEnabled(Context context) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            return pref.getBoolean("statsNotification", true);
        }
    }

    /* public static boolean isLessonEnabled(Context context) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            return pref.getBoolean(context.getResources().getString(R.string.key_enable_lesson), false);
        }
    } */

    public static boolean isBiometricsEnabled(Context context) {
        if (!BIOMETRIC_FEATURE_AVAILABLE) return false;
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            return pref.getBoolean(context.getResources().getString(R.string.key_biometrics), false);
        }
    }

    public static boolean isMinMaxExamIgnoredInBaseGraduation(Context context) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            return pref.getBoolean(context.getResources().getString(R.string.key_minmax_remove), false);
        }
    }

    public static void setBiometricsEnabled(Context context, boolean enabled) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            pref.edit().putBoolean(context.getResources().getString(R.string.key_biometrics), enabled).apply();
        }
    }

    public static boolean isLessonOptionEnabled(Context context) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            return pref.getBoolean(context.getResources().getString(R.string.key_options_lesson), false);
        }
    }


    public static void setLessonEnabled(Context context, boolean enabled) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            pref.edit().putBoolean(context.getResources().getString(R.string.key_default_laude), enabled).apply();
        }
    }

    public static void saveSuggestions(Context context, List suggestions) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List>() {
            }.getType();
            String json = gson.toJson(suggestions, listType);
            pref.edit().putString("suggestion", json).apply();
        }
    }


    public static List<CustomCourse> getCustomCourses(Context context) {
        setupSharedPreferences(context);
        Gson gson = new Gson();
        String json;
        synchronized (PreferenceManager.class) {
            json = pref.getString("customCourses", "null");
        }
        if (json == null) return null;
        Type listType = new TypeToken<List<CustomCourse>>() {
        }.getType();
        return gson.fromJson(json, listType);
    }


    public static void setCustomCourses(Context context, List<CustomCourse> newCourses) {
        setupSharedPreferences(context);
        Gson gson = new Gson();
        synchronized (PreferenceManager.class) {
            Type listType = new TypeToken<List<CustomCourse>>() {
            }.getType();
            pref.edit().putString("customCourses", gson.toJson(newCourses, listType)).apply();
            if (courses == null) courses = new LinkedList<>();
            courses.clear();
            courses.addAll(newCourses);
        }
    }

    public static List getSuggestions(Context context) {
        setupSharedPreferences(context);
        Gson gson = new Gson();
        String json;
        synchronized (PreferenceManager.class) {
            json = pref.getString("suggestion", null);
        }
        if (json == null) return null;
        Type listType = new TypeToken<List>() {
        }.getType();
        return gson.fromJson(json, listType);
    }


    public static void setStatsNotificationEnabled(Context context, boolean enabled) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            pref.edit().putBoolean("statsNotification", enabled).apply();
        }
    }

    public static boolean getCalendarNotificationEnabled(Context context) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            return pref.getBoolean("calendarLessonNotification", true);
        }
    }

    public static void setCalendarNotificationEnabled(Context context, boolean enabled) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            pref.edit().putBoolean("calendarLessonNotification", enabled).apply();
        }
    }

    public static boolean getClassroomNotificationEnabled(Context context) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            return pref.getBoolean("classroomNotification", true);
        }
    }

    public static void setClassroomNotificationEnabled(Context context, boolean enabled) {
        setupSharedPreferences(context);
        synchronized (PreferenceManager.class) {
            pref.edit().putBoolean("classroomNotification", enabled).apply();
        }
    }

    public static void setTheme(Context context, ThemeEngine.Theme theme) {
        setupSharedPreferences(context);
        pref.edit().putInt("appTheme", theme.getValue()).apply();
    }

    public static ThemeEngine.Theme getTheme(Context context) {
        setupSharedPreferences(context);
        return ThemeEngine.Theme.getTheme(pref.getInt("appTheme", 0));
    }

    public synchronized static int getLaudeValue(Context context) {
        setupSharedPreferences(context);
        int laudeValue = 30;
        try {
            laudeValue = Integer.parseInt(pref.getString(context.getResources().getString(R.string.key_default_laude), "30"));
            if (laudeValue < 30 || laudeValue > 34) laudeValue = 30;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return laudeValue;
    }

    public static boolean isExamDateEnabled(Context context) {
        setupSharedPreferences(context);
        return pref.getBoolean(context.getResources().getString(R.string.key_exam_date), true);
    }

}
