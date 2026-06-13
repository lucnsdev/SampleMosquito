package com.sample.mosquito.mqtt;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Publication {

    public String topic, message, dateTime;
    public long timestamp;

    public Publication(String topic, String message) {
        this.topic = topic;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.dateTime = getTime(this.timestamp);
    }

    private String getTime(long time) {
        if (time == 0) return "";
        SimpleDateFormat date = new SimpleDateFormat("HH:mm ss", Locale.getDefault());
        return date.format(time);
    }
}
