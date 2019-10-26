package de.uni_stuttgart.tik.viplab.websocket_api;

import java.io.IOException;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;

import com.auth0.jwt.interfaces.DecodedJWT;

import de.uni_stuttgart.tik.viplab.websocket_api.messages.AuthenticateMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.CreateComputationMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.Message;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.MessageDecoder;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.MessageEncoder;

@ServerEndpoint(value = "/computations", encoders = MessageEncoder.class, decoders = MessageDecoder.class)
public class ComputationWebSocket {

	private Session session;
	private DecodedJWT jwt = null;

	private Jsonb jsonb = JsonbBuilder.create();

	@Inject
	private AuthenticationService authenticationService;

	@Inject
	private Logger logger;

	@OnOpen
	public void onOpen(Session session) throws IOException {
		this.session = session;
		JsonbConfig jsonbConfig = new JsonbConfig();
		jsonb = JsonbBuilder.create(jsonbConfig);

		Message message = new Message();
		message.type = "hello";
		message.content = "world";
		try {
			this.session.getBasicRemote().sendObject(message);
			this.logger.debug("hi3");
		} catch (EncodeException e) {
			throw new IOException(e);
		}

	}

	private <T> T fromJsonObject(Object object, Class<T> type) {
		return jsonb.fromJson(jsonb.toJson(object), type);
	}

	@OnMessage
	public void onMessage(Message message) throws IOException {
		this.logger.debug("got Message of type {} and content {}", message.type,
				message.content);

		switch (message.type) {
		case AuthenticateMessage.MESSAGE_TYPE:
			AuthenticateMessage authenticateMessage = new AuthenticateMessage();
			authenticateMessage.jwt = (String) message.content;
			this.onAuthentication(authenticateMessage);
			break;
		case CreateComputationMessage.MESSAGE_TYPE:
			this.onCreateComputation(fromJsonObject(message.content,
					CreateComputationMessage.class));
			break;
		default:
			throw new IllegalArgumentException(
					"Unkown message type: " + message.type);
		}
	}

	private void onAuthentication(AuthenticateMessage message) {
		if (this.jwt != null) {
			throw new IllegalStateException("JWT is already set.");
		}
		this.jwt = this.authenticationService.authenticate(message.jwt);
		this.logger.debug("set JWT: {}", this.jwt);
	}

	private void onCreateComputation(CreateComputationMessage message) {
		// TODO

	}

	@OnClose
	public void onClose() throws IOException {
		// WebSocket connection closes
	}

	@OnError
	public void onError(Throwable throwable) {
		this.logger.debug("WebSocket Error: ", throwable);
	}
}
