package com.sample.mosquito.mqtt.internal.io;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sample.mosquito.R;
import com.sample.mosquito.mqtt.internal.TcpNetwork;
import com.sample.mosquito.mqtt.internal.messages.MqttMessage;

import java.io.IOException;

public class DataReader {

    public interface Callback {
        void onBrokenPipe();

        void onMessageReceived(MqttMessage mqttMessage);
    }

    private final TcpNetwork tcpNetwork;
    private Thread thread;
    private final Callback callback;
    private final Handler handler;

    public DataReader(TcpNetwork tcpNetwork, Callback callback) {
        this.tcpNetwork = tcpNetwork;
        this.callback = callback;
        handler = new Handler(Looper.getMainLooper());
    }

    public boolean isRunning() {
        return thread != null && !thread.isInterrupted() && tcpNetwork.isConnected();
    }

    public void start() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isBrokenPipe = false;
                MqttInputStream mqttInputStream = new MqttInputStream(tcpNetwork.getInputStream());
                while (isRunning()) {
                    try {
                        if (mqttInputStream.available() > 0) {
                            MqttMessage mqttMessage = mqttInputStream.readMqttMessage();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onMessageReceived(mqttMessage);
                                }
                            });
                        }
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
                            callback.onBrokenPipe();
                        }
                    });
                }
            }
        });
        thread.start();
    }

    public void stop() {
        if (thread != null && !thread.isInterrupted()) thread.interrupt();
        thread = null;
    }
}
