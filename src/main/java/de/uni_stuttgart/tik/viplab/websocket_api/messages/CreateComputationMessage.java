package de.uni_stuttgart.tik.viplab.websocket_api.messages;

@MessageType(CreateComputationMessage.MESSAGE_TYPE)
public class CreateComputationMessage {
	public static final String MESSAGE_TYPE = "create-computation";
	
	public Object template;//TODO
	public Object task;//TODO
}
