package de.uni_stuttgart.tik.viplab.websocket_api;

import java.io.IOException;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
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
import de.uni_stuttgart.tik.viplab.websocket_api.messages.MessageUtil;

@ServerEndpoint(value = "/computations", encoders = MessageEncoder.class, decoders = MessageDecoder.class)
public class ComputationWebSocket {

	private Session session;
	private DecodedJWT jwt = null;

	private Jsonb jsonb = JsonbBuilder.create();

	@Inject
	private AuthenticationService authenticationService;
	
	@Inject
	private ECSComputationService computationService;

	@Inject
	private Logger logger;

	@OnOpen
	public void onOpen(Session session) throws IOException {
		this.session = session;
	}

	/**
	 * Send a message to the remote WebSocket endpoint. The message is encoded
	 * in the json format.
	 * 
	 * @param message
	 * @throws IllegalStateException
	 *             if the WebSocket is not connected
	 * @throws IllegalArgumentException
	 *             if there is something wrong with the message object
	 */
	public void send(Object message) {
		if (session == null) {
			throw new IllegalStateException("The WebSocket is not open jet.");
		}
		Message messageEnvelop = new Message();
		messageEnvelop.type = MessageUtil.getTypeOfMessageObject(message);
		messageEnvelop.content = message;
		try {
			this.session.getBasicRemote().sendObject(messageEnvelop);
		} catch (EncodeException e) {
			throw new IllegalArgumentException("The message of type " + messageEnvelop.type + " can't be encoded.", e);
		} catch (IOException e) {
			throw new IllegalStateException("The message of type " + messageEnvelop.type + " can't be send.", e);
		}
	}

	private <T> T fromJsonObject(Object object, Class<T> type) {
		return jsonb.fromJson(jsonb.toJson(object), type);
	}

	@OnMessage
	public void onMessage(Message message) throws IOException {
		this.logger.debug("got Message of type {} and content {}", message.type, message.content);

		switch (message.type) {
		case AuthenticateMessage.MESSAGE_TYPE:
			AuthenticateMessage authenticateMessage = new AuthenticateMessage();
			authenticateMessage.jwt = (String) message.content;
			this.onAuthentication(authenticateMessage);
			break;
		case CreateComputationMessage.MESSAGE_TYPE:
			this.onCreateComputation(fromJsonObject(message.content, CreateComputationMessage.class));
			break;
		default:
			throw new IllegalArgumentException("Unkown message type: " + message.type);
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
		computationService.createComputation();
	}

	@OnClose
	public void onClose() throws IOException {
		// WebSocket connection closes
	}

	@OnError
	public void onError(Throwable throwable) {
		this.logger.error("WebSocket Error: ", throwable);
	}
}
