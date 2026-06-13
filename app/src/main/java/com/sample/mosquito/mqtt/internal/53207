package com.sample.mosquito.mqtt.internal;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.net.SocketFactory;

public class TcpNetwork {

    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    public TcpNetwork() {
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    public void connect(String host, int port) throws IOException {
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        SocketFactory factory = SocketFactory.getDefault();
        socket = factory.createSocket();
        socket.connect(socketAddress, 30 * 1000);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public void disconnect() throws IOException {
        if (isConnected() && !socket.isClosed()) socket.close();
    }
}
