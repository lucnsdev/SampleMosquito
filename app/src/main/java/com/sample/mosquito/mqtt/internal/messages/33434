package com.sample.mosquito.mqtt.internal.messages;

import java.io.IOException;

public class MqttPingMessage extends MqttMessage {

    public MqttPingMessage() {
        super(MESSAGE_TYPE_PING_REQUEST);
    }

    protected byte[] getVariableHeader() throws IOException {
        return new byte[0];
    }

    protected byte getMessageInfo() {
        return 0;
    }
}

