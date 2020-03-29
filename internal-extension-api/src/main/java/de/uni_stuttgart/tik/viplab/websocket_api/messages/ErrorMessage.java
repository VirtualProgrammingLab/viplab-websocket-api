package de.uni_stuttgart.tik.viplab.websocket_api.messages;

@MessageType(ErrorMessage.MESSAGE_TYPE)
public class ErrorMessage {
	public static final String MESSAGE_TYPE = "error";
	public String message;
}
