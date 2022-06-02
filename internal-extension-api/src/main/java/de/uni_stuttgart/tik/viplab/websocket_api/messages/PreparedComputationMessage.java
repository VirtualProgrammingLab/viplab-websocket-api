package de.uni_stuttgart.tik.viplab.websocket_api.messages;

import java.time.ZonedDateTime;

@MessageType(PreparedComputationMessage.MESSAGE_TYPE)
public class PreparedComputationMessage {
	public static final String MESSAGE_TYPE = "prepared-computation";
	public String id;
	public ZonedDateTime prepared;
	public ZonedDateTime expires;
	public String status;

	public PreparedComputationMessage(String id, ZonedDateTime prepared, ZonedDateTime expires, String status) {
		this.id = id;
		this.prepared = prepared;
		this.expires = expires;
		this.status = status;
	}
}
