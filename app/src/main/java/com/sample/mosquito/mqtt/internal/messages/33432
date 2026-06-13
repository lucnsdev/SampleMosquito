package com.sample.mosquito.mqtt.internal.messages;

public class MqttDisconnectMessage extends MqttMessage {

    public MqttDisconnectMessage(byte info, byte[] data) {
        super(MESSAGE_TYPE_DISCONNECT);
    }

    public MqttDisconnectMessage() {
        super(MESSAGE_TYPE_DISCONNECT);
    }

    protected byte getMessageInfo() {
        return (byte) 0;
    }

    protected byte[] getVariableHeader() {
        return new byte[0];
    }
}
