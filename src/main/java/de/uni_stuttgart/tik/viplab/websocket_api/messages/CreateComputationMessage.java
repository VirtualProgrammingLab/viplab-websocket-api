package de.uni_stuttgart.tik.viplab.websocket_api.messages;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;

@MessageType(CreateComputationMessage.MESSAGE_TYPE)
public class CreateComputationMessage {
	public static final String MESSAGE_TYPE = "create-computation";
	
	public String template;
	public ComputationTask task;
}
