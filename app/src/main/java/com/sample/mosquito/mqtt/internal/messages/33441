package com.sample.mosquito.mqtt.internal.messages;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class MqttSubscribeAckMessage extends MqttMessage {

    private final int[] grantedQos;

    public MqttSubscribeAckMessage(byte info, byte[] data) throws IOException {
        super(MESSAGE_TYPE_SUBSCRIBE_ACK);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        messageId = dis.readUnsignedShort();
        int index = 0;
        grantedQos = new int[data.length - 2];
        int qos = dis.read();
        while (qos != -1) {
            grantedQos[index] = qos;
            index++;
            qos = dis.read();
        }
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
