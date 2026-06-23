package com.sample.mosquito.mqtt;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class QueuePublications {
    private final Queue<Publication> queue;
    private final Handler handler;
    private final Runnable runnable;
    private long lastSent;
    private MqttClient mqttClient;

    public QueuePublications(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
        queue = new LinkedList<>();
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                lastSent = System.currentTimeMillis();
                Publication publication = queue.remove();
                mqttClient.publish(publication.topic, publication.message);
                if (queue.isEmpty()) return;
                handler.postDelayed(this, 1000);
            }
        };
    }

    public void put(Publication publication) {
        if (queue.isEmpty()) {
            long m = System.currentTimeMillis();
            if (m - lastSent > 999) {
                lastSent = m;
                mqttClient.publish(publication.topic, publication.message);
            } else {
                queue.add(publication);
                long time = m - lastSent > 250 ? m - lastSent : 250;
                handler.postDelayed(runnable, time);
            }
            return;
        }
        queue.add(publication);
    }
}
