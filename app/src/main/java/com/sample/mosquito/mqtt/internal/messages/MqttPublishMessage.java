package com.sample.mosquito.mqtt.internal.messages;

import com.sample.mosquito.mqtt.internal.io.CountingInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MqttPublishMessage extends MqttMessage {

    private String topic;
    private int qos;
    private boolean isRetained;
    protected byte[] payload;

    public MqttPublishMessage() {
        super(MESSAGE_TYPE_PUBLISH);
    }

    public MqttPublishMessage(String topic, byte[] payload, boolean isRetained, int qos) {
        super(MESSAGE_TYPE_PUBLISH);
        this.topic = topic;
        this.payload = payload;
        this.isRetained = isRetained;
        this.qos = qos;
    }

    public MqttPublishMessage(String topic, String publication, int qos) {
        super(MESSAGE_TYPE_PUBLISH);
        this.topic = topic;
        this.payload = publication.getBytes(StandardCharsets.UTF_8);
        this.isRetained = false;
        this.qos = qos;
    }

    public MqttPublishMessage(String topic, String publication, boolean isRetained, int qos) {
        super(MESSAGE_TYPE_PUBLISH);
        this.topic = topic;
        this.payload = publication.getBytes(StandardCharsets.UTF_8);
        this.isRetained = isRetained;
        this.qos = qos;
    }

    public MqttPublishMessage(byte info, byte[] data) throws IOException {
        super(MESSAGE_TYPE_PUBLISH);
        qos = (info >> 1) & 0x03;
        isRetained = (info & 0x01) == 0x01;
        isDuplicated = (info & 0x08) == 0x08;

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        CountingInputStream counter = new CountingInputStream(bais);
        DataInputStream dis = new DataInputStream(counter);
        topic = decodeUTF8(dis);
        if (qos > 0) {
            messageId = dis.readUnsignedShort();
        }
        payload = new byte[data.length - counter.getCounter()];
        dis.readFully(payload);
        dis.close();
    }

    protected byte getMessageInfo() {
        byte info = (byte) (qos << 1);
        if (isRetained) info |= 0x01;
        if (isDuplicated) info |= 0x08;
        return info;
    }

    public void setRetained(boolean isRetained) {
        this.isRetained = isRetained;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setMessage(String message) {
        this.payload = message.getBytes(StandardCharsets.UTF_8);
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public int getQos() {
        return qos;
    }

    public String getTopic() {
        return topic;
    }

    public byte[] getPayload() {
        return payload;
    }

    protected byte[] getVariableHeader() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        encodeUTF8(dos, topic);
        if (qos > 0) dos.writeShort(messageId);
        dos.flush();
        return baos.toByteArray();
    }
}