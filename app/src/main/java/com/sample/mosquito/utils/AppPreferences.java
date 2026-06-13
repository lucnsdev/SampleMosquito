package com.sample.mosquito.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AppPreferences {

    private static final Context context;

    static {
        context = App.getContext();
    }

    private static SharedPreferences getPrefs() {
        return context.getSharedPreferences("main_app_preferences", Context.MODE_PRIVATE);
    }

    public static boolean hasKey(String key) {
        return getPrefs().contains(key);
    }

    public static void setDouble(String k, double d) {
        Editor editor = getPrefs().edit();
        editor.putFloat(k, (float) d);
        editor.apply();
    }

    public static double getDouble(String k) {
        return getPrefs().getFloat(k, 0);
    }

    public static void setBoolean(String k, boolean b) {
        Editor editor = getPrefs().edit();
        editor.putBoolean(k, b);
        editor.apply();
    }

    public static boolean getBoolean(String k) {
        return getPrefs().getBoolean(k, false);
    }

    public static void setInt(String k, int i) {
        Editor editor = getPrefs().edit();
        editor.putInt(k, i);
        editor.apply();
    }

    public static int getInt(String k, int defaultValue) {
        return getPrefs().getInt(k, defaultValue);
    }

    public static int getInt(String k) {
        return getPrefs().getInt(k, -1);
    }

    public static void setLong(String k, long l) {
        Editor editor = getPrefs().edit();
        editor.putLong(k, l);
        editor.apply();
    }

    public static long getLong(String k) {
        return getPrefs().getLong(k, 0);
    }

    public static void setString(String k, String s) {
        Editor editor = getPrefs().edit();
        editor.putString(k, s);
        editor.apply();
    }

    public static String getString(String k) {
        return getPrefs().getString(k, "");
    }

    public static boolean exist(String k) {
        return getPrefs().contains(k);
    }

    public static void remove(String k) {
        Editor editor = getPrefs().edit();
        editor.remove(k);
        editor.apply();
    }
}
