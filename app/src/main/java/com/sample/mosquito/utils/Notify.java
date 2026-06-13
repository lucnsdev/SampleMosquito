package com.sample.mosquito.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.Locale;

public class Notify {

    private static Toast toast;
    private static Handler main;

    static {
        init();
    }

    private static void init() {
        main = new Handler(Looper.getMainLooper());
    }

    public static void showFormattedToast(int format, int resId) {
        showToast(String.format(Locale.getDefault(), App.getContext().getString(format), App.getContext().getString(resId)));
    }

    public static void showFormattedToast(int format, String object) {
        showToast(String.format(Locale.getDefault(), App.getContext().getString(format), object));
    }

    public static void showFormattedToast(String format, String object) {
        showToast(String.format(Locale.getDefault(), format, object));
    }

    public static void showToast(int resId) {
        showToast(App.getContext().getString(resId), Toast.LENGTH_SHORT);
    }

    public static void showToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    public static void showLongToast(int resId) {
        showToast(App.getContext().getString(resId), Toast.LENGTH_LONG);
    }

    public static void showLongToast(String message) {
        showToast(message, Toast.LENGTH_LONG);
    }

    private static void showToast(String message, int type) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            main.post(new Runnable() {
                @Override
                public void run() {
                    //cancel();
                    showToast(message, type);
                }
            });
            return;
        }
        if (toast != null) toast.cancel();
        toast = Toast.makeText(App.getContext(), message, type);
        toast.show();
    }

    public static void cancel() {
        if (toast != null) toast.cancel();
    }
}
