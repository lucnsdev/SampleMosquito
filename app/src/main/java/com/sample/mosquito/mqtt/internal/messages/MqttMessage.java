package com.sample.mosquito.mqtt.internal.messages;

import android.util.Log;

import com.sample.mosquito.mqtt.internal.io.CountingInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.charset.StandardCharsets;

public abstract class MqttMessage {

    public static final byte MESSAGE_TYPE_CONNECT = 1;
    public static final byte MESSAGE_TYPE_CONNECT_ACK = 2;
    public static final byte MESSAGE_TYPE_PUBLISH = 3;
    public static final byte MESSAGE_TYPE_PUBLICATION_ACK = 4;
    public static final byte MESSAGE_TYPE_PUBLICATION_RECEIVED = 5;
    public static final byte MESSAGE_TYPE_PUBLICATION_REL = 6;
    public static final byte MESSAGE_TYPE_PUBLICATION_COMP = 7;
    public static final byte MESSAGE_TYPE_SUBSCRIBE = 8;
    public static final byte MESSAGE_TYPE_SUBSCRIBE_ACK = 9;
    public static final byte MESSAGE_TYPE_UNSUBSCRIBE = 10;
    public static final byte MESSAGE_TYPE_UNSUBSCRIBE_ACK = 11;
    public static final byte MESSAGE_TYPE_PING_REQUEST = 12;
    public static final byte MESSAGE_TYPE_PING_RESPONSE = 13;
    public static final byte MESSAGE_TYPE_DISCONNECT = 14;

    private final byte type;
    protected int messageId;
    protected boolean isDuplicated;

    public MqttMessage(byte type) {
        this.type = type;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public boolean isDuplicated() {
        return isDuplicated;
    }

    public void setDuplicated(boolean isDuplicated) {
        this.isDuplicated = isDuplicated;
    }

    public byte[] getHeader() throws IOException {
        int first = ((type & 0x0f) << 4) ^ (getMessageInfo() & 0x0f);
        byte[] varHeader = getVariableHeader();
        int remLen = varHeader.length + getPayload().length;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeByte(first);
        dos.write(encodeMBI(remLen));
        dos.write(varHeader);
        dos.flush();
        return baos.toByteArray();
    }

    protected abstract byte getMessageInfo();

    public byte[] getPayload() throws IOException {
        return new byte[0];
    }

    public byte getType() {
        return type;
    }

    protected abstract byte[] getVariableHeader() throws IOException;

    protected String decodeUTF8(DataInputStream input) throws IOException {
        int encodedLength = input.readUnsignedShort();
        byte[] encodedString = new byte[encodedLength];
        input.readFully(encodedString);
        return new String(encodedString, StandardCharsets.UTF_8);
    }

    protected void encodeUTF8(DataOutputStream dos, String stringToEncode) throws IOException {
        byte[] encodedString = stringToEncode.getBytes(StandardCharsets.UTF_8);
        byte byte1 = (byte) ((encodedString.length >>> 8) & 0xFF);
        byte byte2 = (byte) ((encodedString.length >>> 0) & 0xFF);
        dos.write(byte1);
        dos.write(byte2);
        dos.write(encodedString);
    }

    private byte[] encodeMBI(long number) {
        int numBytes = 0;
        long no = number;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        do {
            byte digit = (byte) (no % 128);
            no = no / 128;
            if (no > 0) {
                digit |= 0x80;
            }
            bos.write(digit);
            numBytes++;
        } while ((no > 0) && (numBytes < 4));
        return bos.toByteArray();
    }

    private static long decodeMBI(DataInputStream in) throws IOException {
        byte digit;
        long msgLength = 0;
        long multiplier = 1;
        do {
            digit = in.readByte();
            msgLength += ((digit & 0x7F) * multiplier);
            multiplier *= 128;
        } while ((digit & 0x80) != 0);
        return msgLength;
    }

    public static class Creator {

        public MqttMessage getMessage(byte[] packet) throws IOException {
            CountingInputStream counter = new CountingInputStream(new ByteArrayInputStream(packet));
            DataInputStream in = new DataInputStream(counter);
            int first = in.readUnsignedByte();
            byte type = (byte) (first >> 4);
            byte info = (byte) (first &= 0x0f);
            long remLen = decodeMBI(in);
            long totalToRead = counter.getCounter() + remLen;

            long remainder = totalToRead - counter.getCounter();
            byte[] data = new byte[0];
            if (remainder > 0) {
                data = new byte[(int) remainder];
                in.readFully(data, 0, data.length);
            }

            MqttMessage result = null;
            if (type == MESSAGE_TYPE_CONNECT) {
                result = new MqttConnectMessage(info, data);
            } else if (type == MESSAGE_TYPE_PUBLISH) {
                result = new MqttPublishMessage(info, data);
            } else if (type == MESSAGE_TYPE_PUBLICATION_ACK) {
                // result = new MqttPubAck(info, data);
            } else if (type == MESSAGE_TYPE_PUBLICATION_COMP) {
                // result = new MqttPublishCompMessage(info, data);
            } else if (type == MESSAGE_TYPE_CONNECT_ACK) {
                result = new MqttConnackMessage(info, data);
            } else if (type == MESSAGE_TYPE_PING_REQUEST) {
                // result = new MqttPingReq(info, data);
            } else if (type == MESSAGE_TYPE_PING_RESPONSE) {
                result = new MqttPingResponseMessage(info, data);
            } else if (type == MESSAGE_TYPE_SUBSCRIBE) {
                // result = new MqttSubscribe(info, data);
            } else if (type == MESSAGE_TYPE_SUBSCRIBE_ACK) {
                result = new MqttSubscribeAckMessage(info, data);
            } else if (type == MESSAGE_TYPE_UNSUBSCRIBE) {
                // result = new MqttUnsubscribe(info, data);
            } else if (type == MESSAGE_TYPE_UNSUBSCRIBE_ACK) {
                result = new MqttUnsubscribeAckMessage(info, data);
            } else if (type == MESSAGE_TYPE_PUBLICATION_REL) {
                // result = new MqttPubRel(info, data);
            } else if (type == MESSAGE_TYPE_PUBLICATION_RECEIVED) {
                result = new MqttPublishReceivedMessage(info, data);
            } else if (type == MESSAGE_TYPE_DISCONNECT) {
                result = new MqttDisconnectMessage(info, data);
            } else {
                throw new InvalidObjectException("Invalid message type!");
            }
            return result;
        }
    }
}
