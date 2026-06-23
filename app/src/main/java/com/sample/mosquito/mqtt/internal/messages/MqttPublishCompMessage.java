package com.sample.mosquito.mqtt.internal.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MqttPublishCompMessage extends MqttMessage {

    public MqttPublishCompMessage(MqttPublishRelMessage message) {
        super(MESSAGE_TYPE_PUBLICATION_COMP);
        messageId = message.getMessageId();
    }

    public int getMessageId() {
        return messageId;
    }

    @Override
    protected byte getMessageInfo() {
        return 0;
    }

    @Override
    protected byte[] getVariableHeader() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(messageId);
        dos.flush();
        return baos.toByteArray();
    }
}
