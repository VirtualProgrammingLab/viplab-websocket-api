package de.uni_stuttgart.tik.viplab.websocket_api.message;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

public class MessageDecoder implements Decoder.Text<Message> {

	private Jsonb jsonb = JsonbBuilder.create();

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {

	}

	@Override
	public Message decode(String s) throws DecodeException {
		try {
			return jsonb.fromJson(s, Message.class);
		} catch (JsonbException e) {
			throw new DecodeException(s, e.getMessage(), e.getCause());
		}
	}

	@Override
	public boolean willDecode(String s) {
		try {
			jsonb.fromJson(s, Message.class);
			return true;
		} catch (JsonbException e) {
			return false;
		}
	}
}
