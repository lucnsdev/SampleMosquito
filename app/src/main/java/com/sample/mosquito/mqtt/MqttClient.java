package com.sample.mosquito.mqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sample.mosquito.mqtt.internal.PingHandler;
import com.sample.mosquito.mqtt.internal.TcpNetwork;
import com.sample.mosquito.mqtt.internal.io.DataReader;
import com.sample.mosquito.mqtt.internal.io.DataWriter;
import com.sample.mosquito.mqtt.internal.messages.MqttConnectMessage;
import com.sample.mosquito.mqtt.internal.messages.MqttDisconnectMessage;
import com.sample.mosquito.mqtt.internal.messages.MqttMessage;
import com.sample.mosquito.mqtt.internal.messages.MqttPublishAckMessage;
import com.sample.mosquito.mqtt.internal.messages.MqttPublishMessage;
import com.sample.mosquito.mqtt.internal.messages.MqttSubscribeMessage;
import com.sample.mosquito.mqtt.internal.messages.MqttUnsubscribeMessage;

import java.io.IOException;

public class MqttClient {

    public interface Callback {
        void onBrokerConnectionChanged(boolean isConnected);

        void onSubscribeChanged(boolean isSubscribed);

        void onPublicationArrived();

        void onReceive(String topic, String publication);

        void onPingCompleted();
    }

    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final Callback callback;
    private final TcpNetwork tcpNetwork;
    private final Handler handler;
    private final String clientId;
    private DataReader dataReader;
    private DataWriter dataWriter;
    private int currentMessageId;
    private boolean isSubscribed;
    private boolean waitingForPingResponse;
    private boolean retryConnect;
    private boolean isConnecting;
    private String lastBroker;
    private PingHandler pingHandler;

    public MqttClient(Context context, String clientId, Callback callback) {
        this.context = context;
        this.callback = callback;
        this.clientId = clientId;
        tcpNetwork = new TcpNetwork();
        handler = new Handler(Looper.getMainLooper());
        currentMessageId = 2;

        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.build();
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
    }

    public void setRetryConnect(boolean retryConnect) {
        this.retryConnect = retryConnect;
    }

    public boolean isConnected() {
        return tcpNetwork != null && tcpNetwork.isConnected(); // and more
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public void connect(String broker) {
        if (isConnected()) return;
        isConnecting = true;
        lastBroker = broker;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tcpNetwork.connect(broker.substring(0, broker.lastIndexOf(":")), Integer.parseInt(broker.substring(broker.lastIndexOf(":") + 1)));
                    dataReader = new DataReader(tcpNetwork, new DataReader.Callback() {
                        @Override
                        public void onBrokenPipe() {
                            isSubscribed = false;
                            if (!isConnected()) return;
                            closeNetwork();
                            callback.onBrokerConnectionChanged(false);
                            if (retryConnect && !isConnecting) connect(lastBroker);
                        }

                        @Override
                        public void onMessageReceived(MqttMessage mqttMessage) {
                            retrieveMessage(mqttMessage);
                        }
                    });
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dataReader.start();
                            dataWriter = new DataWriter(tcpNetwork, new DataWriter.Callback() {
                                @Override
                                public void onBrokenPipe() {
                                    isSubscribed = false;
                                    if (!isConnected()) return;
                                    closeNetwork();
                                    callback.onBrokerConnectionChanged(false);
                                    if (retryConnect && !isConnecting) connect(lastBroker);
                                }
                            });
                            sendConnectedMessage();
                        }
                    });
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        isConnecting = false;
                        close();
                        callback.onBrokerConnectionChanged(false);
                    }
                });
            }
        }).start();
    }
    private void sendConnectedMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dataWriter.write(new MqttConnectMessage(clientId, true, 60));
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        close();
                        callback.onBrokerConnectionChanged(false);
                    }
                });
            }
        }).start();
    }

    private void retrieveMessage(MqttMessage mqttMessage) {
        if (mqttMessage == null) return;
        byte type = mqttMessage.getType();
        if (type == MqttMessage.MESSAGE_TYPE_CONNECT) {
            // message type send 1
        } else if (type == MqttMessage.MESSAGE_TYPE_CONNECT_ACK) {
            isConnecting = false;
            waitingForPingResponse = false;
            pingHandler = new PingHandler(dataWriter, new PingHandler.Callback() {
                @Override
                public void onBrokenPipe() {
                    closeNetwork();
                    forceReconnect();
                }

                @Override
                public void onTimeExceeded() {
                    closeNetwork();
                    forceReconnect();
                }

                private void forceReconnect() {
                    if (hasInternetConnection() && retryConnect) {
                        connect(lastBroker);
                        return;
                    }
                    callback.onBrokerConnectionChanged(false);
                }
            });
            dataWriter.start();
            pingHandler.start();
            callback.onBrokerConnectionChanged(true);
        } else if (type == MqttMessage.MESSAGE_TYPE_PUBLISH) {
            MqttPublishMessage message = (MqttPublishMessage) mqttMessage;
            // dataWriter.put(new MqttPublishReceivedMessage(message)); // 5
            dataWriter.put(new MqttPublishAckMessage(message)); // 4
            callback.onReceive(message.getTopic(), new String(message.getPayload()));
        } else if (type == MqttMessage.MESSAGE_TYPE_PUBLICATION_RECEIVED) {
            // MqttPublishReceivedMessage message = (MqttPublishReceivedMessage) mqttMessage;
            // dataWriter.put(new MqttPublishRelMessage(message));
            callback.onPublicationArrived();
        } else if (type == MqttMessage.MESSAGE_TYPE_PUBLICATION_ACK) {
            //result = new MqttPubAck(info, data);
        } else if (type == MqttMessage.MESSAGE_TYPE_PUBLICATION_REL) {
            // Nothing here. This message is generated by server.
        } else if (type == MqttMessage.MESSAGE_TYPE_PUBLICATION_COMP) {
            // nothing 7 to receive
        } else if (type == MqttMessage.MESSAGE_TYPE_PING_REQUEST) {
            // message type send 12
        } else if (type == MqttMessage.MESSAGE_TYPE_PING_RESPONSE) {
            callback.onPingCompleted();
        } else if (type == MqttMessage.MESSAGE_TYPE_SUBSCRIBE) {
            // message type send 8
        } else if (type == MqttMessage.MESSAGE_TYPE_SUBSCRIBE_ACK) {
            isSubscribed = true;
            callback.onSubscribeChanged(true);
        } else if (type == MqttMessage.MESSAGE_TYPE_UNSUBSCRIBE) {
            // message type send 10
        } else if (type == MqttMessage.MESSAGE_TYPE_UNSUBSCRIBE_ACK) {
            isSubscribed = false;
            callback.onSubscribeChanged(false);
        } else if (type == MqttMessage.MESSAGE_TYPE_DISCONNECT) {
            close();
            callback.onBrokerConnectionChanged(false);
            // message type send 14
        } else {
        }
    }

    public void disconnect() {
        if (tcpNetwork != null && tcpNetwork.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (hasInternetConnection() && tcpNetwork.isConnected()) {
                        try {
                            dataWriter.write(new MqttDisconnectMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onBrokerConnectionChanged(false);
                        }
                    });
                }
            }).start();
            return;
        }
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    private void closeNetwork() {
        isConnecting = false;
        isSubscribed = false;
        connectivityManager.unregisterNetworkCallback(networkCallback);
        if (pingHandler != null) pingHandler.stop();
        if (dataWriter != null) dataWriter.stop();
        if (dataReader != null) dataReader.stop();
        try {
            tcpNetwork.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        closeNetwork();
    }

    public void subscribe(String topic) {
        if (!isConnected()) return;
        dataWriter.put(new MqttSubscribeMessage(new String[]{topic}, new int[]{0}));
    }

    public void unsubscribe(String topic) {
        if (!isConnected()) return;
        dataWriter.put(new MqttUnsubscribeMessage(new String[]{topic}));
    }

    public void publish(String topic, String publication) {
        publish(topic, publication, false);
    }

    public void publish(String topic, String publication, boolean retained) {
        publish(new MqttPublishMessage(topic, publication, retained));
    }

    public void publish(MqttMessage mqttMessage) {
        waitingForPingResponse = false;
        pingHandler.start();
        MqttPublishMessage publishMessage = (MqttPublishMessage) mqttMessage;
        if (publishMessage.getQos() > 0) {
            currentMessageId++;
            publishMessage.setMessageId(currentMessageId);
        }
        dataWriter.put(publishMessage);
    }

    public void deleteRetained(String topic) {
        publish(new MqttPublishMessage(topic, new byte[0], true));
    }

    private boolean hasInternetConnection() {
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) return false;
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

        private final Handler h = new Handler(Looper.getMainLooper());
        private final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (hasInternetConnection()) connect(lastBroker);
                else isConnecting = false;
            }
        };

        @Override
        public void onAvailable(Network network) {
            closeNetwork();
            if (isConnected() || isConnecting) callback.onBrokerConnectionChanged(false);
            if (hasInternetConnection()) {
                if (retryConnect) {
                    isConnecting = true;
                    h.removeCallbacks(r);
                    h.postDelayed(r, 2500L);
                }
            } else {
                isConnecting = false;
            }
        }

        @Override
        public void onLost(Network network) {
            callback.onBrokerConnectionChanged(false);
        }
    };
}
