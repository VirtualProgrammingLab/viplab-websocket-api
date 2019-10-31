package de.uni_stuttgart.tik.viplab.websocket_api.messages;

@MessageType(AuthenticateMessage.MESSAGE_TYPE)
public class AuthenticateMessage {
	public static final String MESSAGE_TYPE = "authenticate";
	public String jwt;
}
