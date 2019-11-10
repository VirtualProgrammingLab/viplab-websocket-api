package de.uni_stuttgart.tik.viplab.websocket_api;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.slf4j.Logger;

import com.auth0.jwt.interfaces.DecodedJWT;

import de.uni_stuttgart.tik.viplab.websocket_api.messages.AuthenticateMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.ComputationMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.CreateComputationMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.Message;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.MessageDecoder;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.MessageEncoder;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.MessageUtil;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.SubscribeMessage;

@ServerEndpoint(value = "/computations", encoders = MessageEncoder.class, decoders = MessageDecoder.class)
public class ComputationWebSocket {
	private DecodedJWT jwt = null;

	private Jsonb jsonb;

	@Inject
	private AuthenticationService authenticationService;

	@Inject
	private ECSComputationService computationService;

	@Inject
	private NotificationService notificationService;

	@Inject
	private Logger logger;

	@PostConstruct
	private void setup() {
		JsonbConfig jsonbConfig = new JsonbConfig();
		jsonb = JsonbBuilder.create(jsonbConfig);
	}

	/**
	 * Send a message to the remote WebSocket endpoint. The message is encoded
	 * in the json format.
	 * 
	 * @param message
	 * @param session
	 *            the session representing the peer
	 * @throws IllegalStateException
	 *             if the WebSocket is not connected
	 * @throws IllegalArgumentException
	 *             if there is something wrong with the message object
	 */
	public static void send(Object message, Session session) {
		if (session == null) {
			throw new IllegalStateException("The WebSocket is not open jet.");
		}
		Message messageEnvelop = new Message();
		messageEnvelop.type = MessageUtil.getTypeOfMessageObject(message);
		messageEnvelop.content = message;
		try {
			session.getBasicRemote().sendObject(messageEnvelop);
		} catch (EncodeException e) {
			throw new IllegalArgumentException("The message of type " + messageEnvelop.type + " can't be encoded.", e);
		} catch (IOException e) {
			throw new IllegalStateException("The message of type " + messageEnvelop.type + " can't be send.", e);
		}
	}

	private <T> T fromJsonObject(Object object, Class<T> type) {
		return jsonb.fromJson(jsonb.toJson(object), type);
	}

	@Counted
	@OnMessage
	public void onMessage(Message message, Session session) {
		this.logger.debug("got Message of type {} and content {}", message.type, message.content);

		switch (message.type) {
		case AuthenticateMessage.MESSAGE_TYPE:
			this.onAuthentication(fromJsonObject(message.content, AuthenticateMessage.class));
			break;
		case CreateComputationMessage.MESSAGE_TYPE:
			this.onCreateComputation(fromJsonObject(message.content, CreateComputationMessage.class), session);
			break;
		case SubscribeMessage.MESSAGE_TYPE:
			this.onSubscribe(fromJsonObject(message.content, SubscribeMessage.class), session);
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
		this.logger.debug("set JWT: {}", this.jwt.getClaims());
	}

	private void onCreateComputation(CreateComputationMessage message, Session session) {
		computationService.createComputation();
		ComputationMessage computationMessage = new ComputationMessage();
		computationMessage.created = ZonedDateTime.now();
		computationMessage.expires = ZonedDateTime.now().plusHours(3);
		computationMessage.id = UUID.randomUUID().toString();
		computationMessage.status = "created";
		notificationService.subscribe("computation:" + computationMessage.id, session);
		send(computationMessage, session);
	}

	private void onSubscribe(SubscribeMessage subscribeMessage, Session session) {
		notificationService.subscribe(subscribeMessage.topic, session);
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
