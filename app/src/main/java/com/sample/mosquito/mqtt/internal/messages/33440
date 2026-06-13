package com.sample.mosquito.mqtt.internal.messages;

import java.io.IOException;

public class MqttPublishRelMessage extends MqttMessage {

    public MqttPublishRelMessage(MqttPublishReceivedMessage message) {
        super(MESSAGE_TYPE_PUBLICATION_REL);
        messageId = message.getMessageId();
    }

    @Override
    protected byte getMessageInfo() {
        return (byte) (2 | (isDuplicated ? 8 : 0));
    }

    @Override
    protected byte[] getVariableHeader() throws IOException {
        return new byte[0];
    }
}
