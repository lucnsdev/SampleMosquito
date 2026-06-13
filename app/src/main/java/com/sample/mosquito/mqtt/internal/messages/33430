package com.sample.mosquito.mqtt.internal.messages;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class MqttConnackMessage extends MqttMessage {

	private final int returnCode;
	private final boolean sessionPresent;
	
	public MqttConnackMessage(byte info, byte[] variableHeader) throws IOException {
		super(MESSAGE_TYPE_CONNECT_ACK);
		ByteArrayInputStream bais = new ByteArrayInputStream(variableHeader);
		DataInputStream dis = new DataInputStream(bais);
		sessionPresent = (dis.readUnsignedByte() & 0x01) == 0x01;
		returnCode = dis.readUnsignedByte();
		dis.close();
	}

	public boolean getSessionPresent() {
		return sessionPresent;
	}

	public int getReturnCode() {
		return returnCode;
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
