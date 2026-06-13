package com.sample.mosquito.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.Locale;

public class TimerCounter {

    public interface Callback {
        void onTimeChanged(long milliseconds);

        void onGate();
    }

    private Callback callback;
    private long milliseconds, currentTime, initialTime;
    private boolean isCountdown;
    private TimeCounter timeCounter;

    public TimerCounter(Callback callback) {
        this.callback = callback;
        this.milliseconds = Long.MAX_VALUE;
    }

    public TimerCounter(long milliseconds, boolean isCountdown, Callback callback) {
        this.isCountdown = isCountdown;
        this.milliseconds = milliseconds;
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setMilliSeconds(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public void setIsCountdown(boolean isCountdown) {
        this.isCountdown = isCountdown;
    }

    public void setInitialTime(long initialTime) {
        this.initialTime = initialTime;
    }

    public boolean isRunning() {
        return timeCounter.isRunning();
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void start(long milliseconds, boolean isCountdown, Callback callback) {
        this.isCountdown = isCountdown;
        this.milliseconds = milliseconds;
        this.callback = callback;
        start();
    }

    public void start(long milliseconds) {
        this.milliseconds = milliseconds;
        start();
    }

    public void start() {
        if ((isCountdown && milliseconds <= 0) || (!isCountdown && currentTime >= milliseconds)) {
            callback.onGate();
            return;
        }
        if (timeCounter != null && timeCounter.isRunning()) return;
        timeCounter = new TimeCounter();
        timeCounter.post();
    }

    public void stop() {
        if (timeCounter != null) timeCounter.cancel();
    }

    public void callGate() {
        callback.onGate();
    }

    public String getTimeString(long milliseconds) {
        boolean reverse;
        if (milliseconds < 0) {
            reverse = true;
            milliseconds *= -1;
        } else {
            reverse = false;
        }
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        while (milliseconds >= 86400000L) { // 24 hours
            milliseconds -= 86400000L;
            days++;
        }
        while (milliseconds >= 3600000L) { // 1 hour
            milliseconds -= 3600000L;
            hours++;
        }
        while (milliseconds >= 60000L) { // 1 minute
            milliseconds -= 60000L;
            minutes++;
        }
        while (milliseconds >= 1000L) { // 1 second
            milliseconds -= 1000L;
            seconds++;
        }
        String s = seconds < 10 ? "0" + seconds : String.valueOf(seconds);
        if (days > 0) {
            return (reverse ? "-" : "") + String.format(Locale.getDefault(), "%dd - %dh %d:%s", days, hours, minutes, s);
        } else if (hours > 1) {
            return (reverse ? "-" : "") + String.format(Locale.getDefault(), "%dh %d:%s", hours, minutes, s);
        } else {
            return (reverse ? "-" : "") + String.format(Locale.getDefault(), "%d:%s", minutes, s);
        }
    }

    private class TimeCounter {

        private Thread thread;
        private Handler handler;
        private boolean isCompleted;

        public TimeCounter() {
            handler = new Handler(Looper.getMainLooper());
        }

        private boolean isRunning() {
            return thread != null && !thread.isInterrupted() && !isCompleted;
        }

        private void cancel() {
            if (isRunning()) thread.interrupt();
            thread = null;
            isCompleted = true;
        }

        private void post() {
            if (isRunning()) return;
            isCompleted = false;
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (isCountdown) currentTime = milliseconds;
                    else currentTime = initialTime;
                    while (isRunning()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignore) {
                            break;
                        }
                        if (!isRunning()) return;
                        if (isCountdown) currentTime -= 1000L;
                        else currentTime += 1000L;
                        if ((isCountdown && currentTime <= 0) || (!isCountdown && currentTime >= milliseconds)) {
                            break;
                        } else {
                            final long c = currentTime;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onTimeChanged(c);
                                }
                            });
                        }
                    }
                    if (!isRunning()) return;
                    isCompleted = true;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onGate();
                        }
                    });
                }
            });
            thread.start();
        }
    }
}
