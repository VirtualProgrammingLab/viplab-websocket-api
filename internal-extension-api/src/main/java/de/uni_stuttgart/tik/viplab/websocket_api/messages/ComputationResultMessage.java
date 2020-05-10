package de.uni_stuttgart.tik.viplab.websocket_api.messages;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult;

@MessageType(ComputationResultMessage.MESSAGE_TYPE)
public class ComputationResultMessage {
	public static final String MESSAGE_TYPE = "result";
	public ComputationResult result;

	public ComputationResultMessage(ComputationResult result) {
		this.result = result;
	}
}
