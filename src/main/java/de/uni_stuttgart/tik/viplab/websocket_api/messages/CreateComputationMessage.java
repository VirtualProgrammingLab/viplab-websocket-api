package de.uni_stuttgart.tik.viplab.websocket_api.messages;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;

@MessageType(CreateComputationMessage.MESSAGE_TYPE)
public class CreateComputationMessage {
	public static final String MESSAGE_TYPE = "create-computation";
	
	public ComputationTemplate template;
	public ComputationTask task;
}
