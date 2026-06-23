package com.sample.mosquito.mqtt.internal.io;

import com.sample.mosquito.mqtt.internal.messages.MqttMessage;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MqttOutputStream {

    private final BufferedOutputStream out;

    public MqttOutputStream(OutputStream out) {
        this.out = new BufferedOutputStream(out);
    }

    public void close() throws IOException {
        out.close();
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void write(int b) throws IOException {
        out.write(b);
    }

    public void write(MqttMessage message) throws IOException {
        //Log.d("Lucas", "write: " + message.getType());
        out.write(message.getHeader());
        out.write(message.getPayload());
/*
        if (message.getType() != MqttMessage.MESSAGE_TYPE_PUBLICATION_ACK) return;
        byte[] header = message.getHeader();
        byte[] payload = message.getPayload();
        for (int i = 0; i < header.length; i++) {
            Log.d("Lucas", "header[" + i + "] = " + ((int) header[i]));
        }
        for (int i = 0; i < payload.length; i++) {
            Log.d("Lucas", "payload[" + i + "] = " + ((int) payload[i]));
        }
 */
    }
}

