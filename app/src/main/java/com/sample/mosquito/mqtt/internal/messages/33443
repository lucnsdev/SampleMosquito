package com.sample.mosquito.mqtt.internal.messages;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class MqttUnsubscribeAckMessage extends MqttMessage {

    public MqttUnsubscribeAckMessage(byte type, byte[] data) throws IOException {
        super(MESSAGE_TYPE_UNSUBSCRIBE_ACK);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        messageId = dis.readUnsignedShort();
        dis.close();
    }

    @Override
    protected byte getMessageInfo() {
        return 0;
    }

    @Override
    protected byte[] getVariableHeader() throws IOException {
        return new byte[0];
    }
}
