package de.uni_stuttgart.tik.viplab.websocket_api.messages;

@MessageType(PrepareComputationMessage.MESSAGE_TYPE)
public class PrepareComputationMessage {
	public static final String MESSAGE_TYPE = "prepare-computation";
	
	public String template;
}
