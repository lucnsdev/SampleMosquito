package com.sample.mosquito.mqtt.internal.messages;

import java.io.IOException;

public class MqttPingResponseMessage extends MqttMessage {

    public MqttPingResponseMessage(byte info, byte[] data) {
        super(MESSAGE_TYPE_PING_RESPONSE);
    }

	@Override
	protected byte getMessageInfo() {
		return 0;
	}

	protected byte[] getVariableHeader() throws IOException {
        return new byte[0];
    }
}
