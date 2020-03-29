package de.uni_stuttgart.tik.viplab.websocket_api.message;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageEncoder implements Encoder.Text<Message> {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Jsonb jsonb= JsonbBuilder.create();

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public String encode(Message message) throws EncodeException {
		String json = this.jsonb.toJson(message);
		logger.debug("Sending json: {}", json);
		return json;
	}

}
