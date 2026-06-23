package com.sample.mosquito.services;

import android.provider.Settings;

import com.sample.mosquito.R;
import com.sample.mosquito.activities.TopicsActivity;
import com.sample.mosquito.mqtt.MqttClient;
import com.sample.mosquito.utils.TimerCounter;
import com.sample.mosquito.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainService extends BaseService {

    public interface Callback {
        void onBrokerConnectionChanged(boolean isConnected);

        void onPublicationArrived();

        void onSubscribeChanged(boolean isSubscribed);

        void onReceive(String topic, String publication);

        void onTimeChanged(long milliseconds);

        void onGate();
    }

    private NotificationProvider notificationProvider;
    private MqttClient mosquito;
    private Callback callback;
    private TimerCounter timerCounter;
    private long timerCounterValue;
    private String lastTopic, lastPublication, lastSubscribedTopic;
    private boolean keepActiveService;
    private long lastReceivedTime;

    @Override
    public void onCreate() {
        super.onCreate();


        notificationProvider = new NotificationProvider(this, new NotificationProvider.OnNotificationClick() {
            @Override
            public void onButtonClick() {
                stopTimerCounter();
                disconnect();
                stopForeground();
                notificationProvider.hide();
            }
        });
        notificationProvider.setActivityClass(TopicsActivity.class);

        timerCounter = new TimerCounter(timerCounterValue, true, new TimerCounter.Callback() {
            @Override
            public void onTimeChanged(long milliseconds) {
                //Log.d("Lucas", "onTimeChanged " + (milliseconds / 1000) + "s");
                if (callback != null) callback.onTimeChanged(milliseconds);
                else notificationProvider.show((milliseconds / 1000) + "s", getTime(lastReceivedTime));
            }

            @Override
            public void onGate() {
                if (callback != null) {
                    callback.onGate();
                } else {
                    notificationProvider.show(getString(R.string.sending), getTime(lastReceivedTime));
                }
                sendMessage(lastTopic, lastPublication);
            }
        });

        String androidId = Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mosquito = MqttClient.getInstance();
        mosquito.setClientId(androidId);
        mosquito.setCallback(new MqttClient.Callback() {
            @Override
            public void onBrokerConnectionChanged(boolean isConnected) {
                timerCounter.stop();
                if (callback == null) {
                    if (isConnected) {
                        if (keepActiveService) {
                            if (lastSubscribedTopic != null) {
                                notificationProvider.show(getString(R.string.connected), getString(R.string.subscribing));
                                mosquito.subscribe(lastSubscribedTopic);
                            } else {
                                timerCounter.start(timerCounterValue);
                            }
                        }
                    } else {
                        if (keepActiveService) {
                            notificationProvider.show(getString(R.string.disconnect), getString(Utils.hasInternetConnection() ? R.string.connecting : R.string.wait_for_internet_connection));
                        } else {
                            notificationProvider.show(getString(R.string.disconnect), getString(Utils.hasInternetConnection() ? R.string.connecting : R.string.wait_for_internet_connection), getString(android.R.string.cancel));
                        }
                    }
                } else {
                    callback.onBrokerConnectionChanged(isConnected);
                }
            }

            @Override
            public void onSubscribeChanged(boolean subscribed) {
                if (lastSubscribedTopic != null && keepActiveService) timerCounter.start(timerCounterValue);
                if (callback == null) {
                    notificationProvider.show(getString(R.string.connected), getString(subscribed ? R.string.subscribed : R.string.unsubscribed));
                } else {
                    callback.onSubscribeChanged(subscribed);
                }
            }

            @Override
            public void onPublicationArrived() {
                if (keepActiveService) timerCounter.start(timerCounterValue);
                if (callback != null) callback.onPublicationArrived();
                else notificationProvider.show((timerCounterValue / 1000) + "s", getTime(lastReceivedTime));
            }

            @Override
            public void onReceive(String topic, String publication) {
                lastReceivedTime = System.currentTimeMillis();
                if (callback == null) {
                    notificationProvider.show(getString(R.string.received_message), topic + ": " + publication);
                } else {
                    callback.onReceive(topic, publication);
                }
            }

            @Override
            public void onPingCompleted() {
            }
        });
    }

    private String getTime(long time) {
        if (time == 0) return "";
        SimpleDateFormat date = new SimpleDateFormat("HH:mm ss", Locale.getDefault());
        return date.format(time);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimerCounter();
        disconnect();
    }

    @Override
    public NotificationProvider onForegroundRequested() {
        notificationProvider.show(getString(R.string.connected), getString(mosquito.isSubscribed() ? R.string.subscribed : R.string.unsubscribed));
        return notificationProvider;
    }

    @Override
    public void onForegroundStarted() {
    }

    @Override
    public void onForegroundStopped() {
        notificationProvider.hide();
    }

    public void startForeground() {
        startForeground(MainService.class);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setReconnect(boolean reconnect) {
        mosquito.setRetryConnect(reconnect);
    }

    public void setTimerCounterValue(long timerCounterValue) {
        this.timerCounterValue = timerCounterValue;
        timerCounter.setMilliSeconds(timerCounterValue);
    }

    public void startTimerCounter(String topic, String publication, long timerCounterValue) {
        this.lastTopic = topic;
        this.lastPublication = publication;
        this.timerCounterValue = timerCounterValue;
        keepActiveService = true;
        timerCounter.start(timerCounterValue);
        // sendMessage(topic, publication);
    }

    public String getLastTopic() {
        return lastTopic;
    }

    public String getLastPublication() {
        return lastPublication;
    }

    public String getLastSubscribedTopic() {
        return lastSubscribedTopic;
    }

    public void stopTimerCounter() {
        keepActiveService = false;
        timerCounter.stop();
    }

    public boolean isTimerCounterRunning() {
        return keepActiveService;
    }

    public boolean isConnecting() {
        return mosquito.isConnecting();
    }

    public void connect(String broker) {
        mosquito.connect(broker);
    }

    public boolean isConnected() {
        return mosquito.isConnected();
    }

    public void disconnect() {
        mosquito.disconnect();
    }

    public void subscribe(String topic) {
        this.lastSubscribedTopic = topic;
        mosquito.subscribe(topic);
    }

    public boolean isSubscribed() {
        return mosquito.isSubscribed();
    }

    public void unsubscribe(String topic) {
        lastSubscribedTopic = null;
        mosquito.unsubscribe(topic);
    }

    public void sendMessage(String topic, String publication) {
        this.lastTopic = topic;
        this.lastPublication = publication;
        mosquito.publish(topic, publication);
    }

    public void sendRetainedMessage(String message, String topic) {
        mosquito.publish(message, topic, true);
    }

    public void deleteRetainedMessage(String topic) {
        mosquito.deleteRetained(topic);
    }
}
