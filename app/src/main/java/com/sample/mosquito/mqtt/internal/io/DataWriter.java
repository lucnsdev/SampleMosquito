package com.sample.mosquito.mqtt.internal.io;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sample.mosquito.mqtt.internal.TcpNetwork;
import com.sample.mosquito.mqtt.internal.messages.MqttConnectMessage;
import com.sample.mosquito.mqtt.internal.messages.MqttMessage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class DataWriter {
    public interface Callback {
        void onBrokenPipe();
    }

    private final Callback callback;
    private final TcpNetwork tcpNetwork;
    private final MqttOutputStream mqttOutputStream;
    private Thread thread;
    private final Handler handler;
    private final Queue<MqttMessage> queue;

    public DataWriter(TcpNetwork tcpNetwork, Callback callback) {
        this.tcpNetwork = tcpNetwork;
        this.callback = callback;
        handler = new Handler(Looper.getMainLooper());
        mqttOutputStream = new MqttOutputStream(tcpNetwork.getOutputStream());
        queue = new LinkedList<>();
    }

    public void put(MqttMessage mqttMessage) {
        queue.add(mqttMessage);
    }

    public void write(MqttMessage mqttMessage) throws IOException {
        // Log.d("lucas", "mosquito sample send:" + mqttMessage.getType());
        mqttOutputStream.write(mqttMessage);
        mqttOutputStream.flush();
    }

    public boolean isRunning() {
        return thread != null && !thread.isInterrupted() && tcpNetwork.isConnected();
    }

    public void start() {
        if (isRunning()) return;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isBrokenPipe = false;
                while (isRunning()) {
                    while (queue.size() > 0) {
                        try {
                            write(queue.remove());
                        } catch (IOException e) {
                            e.printStackTrace();
                            isBrokenPipe = true;
                            break;
                        }
                    }
                    if (isBrokenPipe) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                stop();
                                callback.onBrokenPipe();
                            }
                        });
                        break;
                    }
                }
            }
        });
        thread.start();
    }

    public void stop() {
        queue.clear();
        if (thread != null && !thread.isInterrupted()) thread.interrupt();
        thread = null;
    }
}
