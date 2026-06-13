package com.sample.mosquito.mqtt.internal;

import android.os.Handler;
import android.os.Looper;

import com.sample.mosquito.mqtt.internal.io.DataWriter;
import com.sample.mosquito.mqtt.internal.messages.MqttPingMessage;

import java.io.IOException;

public class PingHandler {

    public interface Callback {
        void onBrokenPipe();
        void onTimeExceeded();
    }

    private final Callback callback;
    private final Runnable runnablePing;
    private final Handler handlerPing;
    private final DataWriter dataWriter;
    private long pingStartTime;
    private int counter;

    public PingHandler(DataWriter dataWriter, Callback callback) {
        this.dataWriter = dataWriter;
        this.callback = callback;
        handlerPing = new Handler(Looper.getMainLooper());
        runnablePing = new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - pingStartTime >= 70000L) {
                    callback.onTimeExceeded();
                    return;
                }
                counter++;
                handlerPing.postDelayed(this, 10000L);
                if (counter < 6) return;
                counter = 0;
                sendPing();
            }
        };
    }

    public void stop() {
        handlerPing.removeCallbacks(runnablePing);
    }

    public void start() {
        counter = 0;
        pingStartTime = System.currentTimeMillis();
        handlerPing.removeCallbacks(runnablePing);
        handlerPing.postDelayed(runnablePing, 10000);
    }

    public void sendPing() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dataWriter.write(new MqttPingMessage());
                    pingStartTime = System.currentTimeMillis();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        stop();
                        callback.onBrokenPipe();
                    }
                });
            }
        }).start();
    }
}
