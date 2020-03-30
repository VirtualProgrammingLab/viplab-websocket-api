package de.uni_stuttgart.tik.viplab.websocket_api.messages;

@MessageType(SubscribeMessage.MESSAGE_TYPE)
public class SubscribeMessage {
	public static final String MESSAGE_TYPE = "subscribe";
	public String topic;
}
