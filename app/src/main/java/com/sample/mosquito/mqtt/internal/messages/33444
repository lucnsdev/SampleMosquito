package com.sample.mosquito.mqtt.internal.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MqttUnsubscribeMessage extends MqttMessage {

    private final String[] names;

    public MqttUnsubscribeMessage(String[] names) {
        super(MESSAGE_TYPE_UNSUBSCRIBE);
        this.names = names;
        messageId = 2;
    }

    protected byte getMessageInfo() {
        return (byte) (2 | (isDuplicated ? 8 : 0));
    }

    protected byte[] getVariableHeader() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(messageId);
        dos.flush();
        return baos.toByteArray();
    }

    public byte[] getPayload() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (int i = 0; i < names.length; i++) {
            encodeUTF8(dos, names[i]);
        }
        dos.flush();
        return baos.toByteArray();
    }
}
