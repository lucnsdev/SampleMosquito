package com.sample.mosquito.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class Utils {

    private static Vibrator vibrator;

    static {
        init();
    }

    private static void init() {
        Context context = App.getContext();
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public static void vibrate(int duration) {
        if (duration > 0) {
            vibrator.cancel();
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    public static void vibrate() {
        vibrator.cancel();
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    public static void pulsate() {
        vibrate(50);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                vibrate(50);
            }
        }, 100);
    }

    public static void fastPulsate() {
        vibrate(50);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                vibrate(50);
            }
        }, 100);
    }

    public static boolean hasInternetConnection() {
        ConnectivityManager connectivity = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivity.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities = connectivity.getNetworkCapabilities(network);
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
