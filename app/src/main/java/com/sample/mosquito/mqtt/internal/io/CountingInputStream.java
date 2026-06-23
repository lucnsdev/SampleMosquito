package com.sample.mosquito.mqtt.internal.io;

import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends InputStream {

    private InputStream in;
    private int counter;

    public CountingInputStream(InputStream in) {
        this.in = in;
        this.counter = 0;
    }

    public int read() throws IOException {
        int i = in.read();
        if (i != -1) {
            counter++;
        }
        return i;
    }

    public int getCounter() {
        return counter;
    }

    public void resetCounter() {
        counter = 0;
    }
}
