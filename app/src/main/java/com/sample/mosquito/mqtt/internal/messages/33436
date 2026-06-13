package com.sample.mosquito.mqtt.internal.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MqttPublishAckMessage extends MqttMessage {

    public MqttPublishAckMessage(byte type, byte[] data) throws IOException {
        super(MESSAGE_TYPE_PUBLICATION_ACK);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        messageId = dis.readUnsignedShort();
        dis.close();
    }

    public MqttPublishAckMessage(MqttPublishMessage publish) {
        super(MESSAGE_TYPE_PUBLICATION_ACK);
        messageId = publish.getMessageId();
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
