package de.uni_stuttgart.tik.viplab.websocket_api.messages;

import java.time.ZonedDateTime;

@MessageType(ComputationMessage.MESSAGE_TYPE)
public class ComputationMessage {
	public static final String MESSAGE_TYPE = "computation";
	public String id;
	public ZonedDateTime created;
	public ZonedDateTime expires;
	public String status;
}
