package de.uni_stuttgart.tik.viplab.websocket_api;

import static de.uni_stuttgart.tik.viplab.websocket_api.ComputationSession.mustBeAuthenticated;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.concurrent.Future;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.slf4j.Logger;

import com.auth0.jwt.interfaces.DecodedJWT;

import de.uni_stuttgart.tik.viplab.websocket_api.authentication.AuthenticationService;
import de.uni_stuttgart.tik.viplab.websocket_api.jsontransformers.ParameterDeserializer;
import de.uni_stuttgart.tik.viplab.websocket_api.message.Message;
import de.uni_stuttgart.tik.viplab.websocket_api.message.MessageDecoder;
import de.uni_stuttgart.tik.viplab.websocket_api.message.MessageEncoder;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.AuthenticateMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.ComputationMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.CreateComputationMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.ErrorMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.MessageUtil;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.PrepareComputationMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.PreparedComputationMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.SubscribeMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import io.quarkus.logging.Log;

@ServerEndpoint(value = "/computations", encoders = MessageEncoder.class, decoders = MessageDecoder.class)
@ApplicationScoped
public class ComputationWebSocket {

	private final Jsonb jsonb = JsonbBuilder.create();

	@Inject
	AuthenticationService authenticationService;

	@Inject
	ViPLabBackendConnector backendConnector;

	@Inject
	NotificationService notificationService;

	@Inject
	Logger logger;

	/**
	 * Send a message to the remote WebSocket endpoint. The message is encoded
	 * in the json format.
	 * 
	 * @param message
	 * @param session
	 *            the session representing the peer
	 * @return
	 * @throws IllegalStateException
	 *             if the WebSocket is not connected
	 * @throws IllegalArgumentException
	 *             if there is something wrong with the message object
	 */
	public static Future<Void> send(Object message, Session session) {
		if (session == null) {
			throw new IllegalStateException("The WebSocket is not open jet.");
		}
		Message messageEnvelop = new Message();
		messageEnvelop.type = MessageUtil.getTypeOfMessageObject(message);
		messageEnvelop.content = message;
		return session.getAsyncRemote().sendObject(messageEnvelop);
	}

	private <T> T fromJsonObject(Object object, Class<T> type) {
		try {
			return jsonb.fromJson(jsonb.toJson(object), type);
		} catch (JsonbException e) {
			throw new ComputationWebsocketException("Invalid message", e);
		}
	}

	@Counted
	@OnMessage
	public void onMessage(Message message, Session session) {
		this.logger.debug("got Message of type {} and content {}", message.type, message.content);
		try {
			switch (message.type) {
			case AuthenticateMessage.MESSAGE_TYPE:
				this.onAuthentication(fromJsonObject(message.content, AuthenticateMessage.class), session);
				break;
			case CreateComputationMessage.MESSAGE_TYPE:
				mustBeAuthenticated(session);
				this.onCreateComputation(fromJsonObject(message.content, CreateComputationMessage.class), session);
				break;
			case PrepareComputationMessage.MESSAGE_TYPE:
				mustBeAuthenticated(session);
				this.onPrepareComputation(fromJsonObject(message.content, PrepareComputationMessage.class), session);
				break;
			case SubscribeMessage.MESSAGE_TYPE:
				mustBeAuthenticated(session);
				this.onSubscribe(fromJsonObject(message.content, SubscribeMessage.class), session);
				break;
			default:
				throw new ComputationWebsocketException("Unkown message type: " + message.type);
			}
		} catch (ComputationWebsocketException e) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.message = e.getMessage();
			send(errorMessage, session);
		}
	}

	private void onAuthentication(AuthenticateMessage message, Session session) {
		if (session.getUserProperties().containsKey(ComputationSession.SESSION_JWT) &&
		  !ComputationSession.getJWT(session).getClaims().containsKey("viplab.digest-replaceable")){
			throw new ComputationWebsocketException("JWT is already set.");
		}
		DecodedJWT jwt;
		try {
			jwt = this.authenticationService.authenticate(message.jwt);
		} catch (Exception e) {
			throw new ComputationWebsocketException("JWT is not valid");
		}

		session.getUserProperties().put(ComputationSession.SESSION_JWT, jwt);
	}

	private void onCreateComputation(CreateComputationMessage message, Session session) {
		try {
			authenticationService.verify(message.template,
					ComputationSession.getJWT(session).getClaim("viplab.computation-template.digest").asString());
		} catch (IllegalArgumentException e) {
			throw new ComputationWebsocketException("The integrity of the Computation Template can't be verified", e);
		}

		String templateJson = new String(Base64.getUrlDecoder().decode(message.template), StandardCharsets.UTF_8);
		JsonbConfig config = new JsonbConfig().withDeserializers(new ParameterDeserializer());
		Jsonb jsonbParam = JsonbBuilder.create(config);
		ComputationTemplate template = jsonbParam.fromJson(templateJson, ComputationTemplate.class);
		
		backendConnector.createComputation(template, message.task).thenAcceptAsync(computationId -> {
			ComputationMessage computationMessage = new ComputationMessage(computationId, ZonedDateTime.now(),
					ZonedDateTime.now().plusHours(3), "created");
			send(computationMessage, session);
			notificationService.subscribe("computation:" + computationId, new WebsocketSessionWrapper(session));
		});
	}

	private void onPrepareComputation(PrepareComputationMessage message, Session session) {
		try {
			authenticationService.verify(message.template,
					ComputationSession.getJWT(session).getClaim("viplab.computation-template.digest").asString());
		} catch (IllegalArgumentException e) {
			throw new ComputationWebsocketException("The integrity of the Computation Template can't be verified", e);
		}

		String templateJson = new String(Base64.getUrlDecoder().decode(message.template), StandardCharsets.UTF_8);
		JsonbConfig config = new JsonbConfig().withDeserializers(new ParameterDeserializer());
		Jsonb jsonbParam = JsonbBuilder.create(config);
		ComputationTemplate template = jsonbParam.fromJson(templateJson, ComputationTemplate.class);
		
		Log.info("!!!PREPARE COMPUTATION!!!");

		if (template.environment.equalsIgnoreCase("Container") || template.environment.equalsIgnoreCase("Matlab")) {
			Log.info(template.environment);
			backendConnector.prepareComputation(template).thenAcceptAsync(preparationId -> {
				PreparedComputationMessage prepareComputationMessage = new PreparedComputationMessage(preparationId, ZonedDateTime.now(),
						ZonedDateTime.now().plusHours(3), "prepared");
				send(prepareComputationMessage, session);
				notificationService.subscribe("prepared-computation:" + preparationId, new WebsocketSessionWrapper(session));
			});
		}
	}

	private void onSubscribe(SubscribeMessage subscribeMessage, Session session) {
		notificationService.subscribe(subscribeMessage.topic, new WebsocketSessionWrapper(session));
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
