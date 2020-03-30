package de.uni_stuttgart.tik.viplab.websocket_api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

public class ComputationWebSocketIntegrationTest {

	private static String websocketPort = System.getProperty("default.http.port");
	private static String jwksPath = System.getProperty("viplab.jwt.jwks.file.private.test");
	private static String kid = System.getProperty("viplab.jwt.jwks.kid.test");

	private MessageHandler messageHandler;

	private static Algorithm algorithm;
	private static URI webSocketUri;

	@Inject
	@Any
	private InMemoryConnector connector;

	public static void setup() throws MalformedURLException, URISyntaxException {
		System.out.println("kldsjfjlms,cfsklcfskjfksdjflksd");
		algorithm = JWTUtil.getAlgorithm(Paths.get(jwksPath).toUri().toURL(), kid);
		webSocketUri = new URI("ws://localhost:" + websocketPort + "/websocket-api/computations");
	}

	@BeforeEach
	public void setupClient() {
		System.out.println("kldsjfjlms,cfsklcfskjfksdjflksd - 1");
		messageHandler = Mockito.mock(MessageHandler.class);
	}

	@Test
	public void testConnectionIsPossible() throws InterruptedException {
		System.out.println("kldsjfjlms,cfsklcfskjfksdjflksd - 2");
		TestWebSocket websocket = new TestWebSocket(webSocketUri, messageHandler);
		assertThat("WebSocket connection to " + webSocketUri, websocket.connectBlocking(1000, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testCreateComputation() throws Exception {
		System.out.println("kldsjfjlms,cfsklcfskjfksdjflksd - 3");
		// stubbing

		// create resources
		JSONObject computationTemplate = TestJSONMessageProvider.getComputationTemplate("Java");
		String computationTemplateBase64 = JWTUtil.jsonToBase64(computationTemplate);
		String computationTemplateHash = JWTUtil.sha256(computationTemplateBase64);
		String jwt = JWT.create().withIssuer("test")
				.withClaim("viplab.computation-template.digest", computationTemplateHash).sign(algorithm);
		// connection
		TestWebSocket websocket = new TestWebSocket(webSocketUri, messageHandler);
		websocket.connectBlocking(1000, TimeUnit.MILLISECONDS);
		// authenticate
		websocket.send(TestJSONMessageProvider.getAuthenticationMessage(jwt));
		// create computation
		JSONObject computationTask = TestJSONMessageProvider.getComputationTask(computationTemplate);
		websocket.send(TestJSONMessageProvider.getCreateComputationMessage(computationTemplateBase64, computationTask));
		// stub the result

		// verify result
		ArgumentCaptor<JSONObject> messagesCaptor = ArgumentCaptor.forClass(JSONObject.class);
		Mockito.verify(messageHandler, Mockito.timeout(1100)).onMessage(messagesCaptor.capture());
		List<JSONObject> messages = messagesCaptor.getAllValues();
		assertThat(messages.get(0), SameJSONAs.sameJSONObjectAs(new JSONObject().put("type", "computation"))
				.allowingExtraUnexpectedFields());
		assertThat(messages.get(1),
				SameJSONAs.sameJSONObjectAs(new JSONObject().put("type", "result")).allowingExtraUnexpectedFields());
		// close connection
		websocket.closeBlocking();
	}

	@Test
	public void testAuthenticationIsPerformed() throws InterruptedException, JSONException {
		// create resources
		String computationTemplate = JWTUtil.jsonToBase64(TestJSONMessageProvider.getComputationTemplate("Java"));
		JSONObject computationTemplate2 = TestJSONMessageProvider.getComputationTemplate("C");
		String computationTemplate2Base64 = JWTUtil.jsonToBase64(computationTemplate2);
		String computationTemplateHash = JWTUtil.sha256(computationTemplate);
		String jwt = JWT.create().withIssuer("test")
				.withClaim("viplab.computation-template.digest", computationTemplateHash).sign(algorithm);
		// connection
		TestWebSocket websocket = new TestWebSocket(webSocketUri, messageHandler);
		websocket.connectBlocking(1000, TimeUnit.MILLISECONDS);
		// authenticate
		websocket.send(TestJSONMessageProvider.getAuthenticationMessage(jwt));
		// try create Task
		JSONObject computationTask = TestJSONMessageProvider.getComputationTask(computationTemplate2);
		websocket
				.send(TestJSONMessageProvider.getCreateComputationMessage(computationTemplate2Base64, computationTask));
		// verify result
		ArgumentCaptor<JSONObject> messagesCaptor = ArgumentCaptor.forClass(JSONObject.class);
		Mockito.verify(messageHandler, Mockito.timeout(1000)).onMessage(messagesCaptor.capture());
		List<JSONObject> messages = messagesCaptor.getAllValues();
		assertThat("received one response", messages, hasSize(1));
		assertThat(messages.get(0),
				SameJSONAs.sameJSONObjectAs(new JSONObject().put("type", "error")).allowingExtraUnexpectedFields());
		// close connection
		websocket.closeBlocking();
	}

	public interface MessageHandler {
		public void onMessage(JSONObject json);
	}

	private class TestWebSocket extends WebSocketClient {

		private MessageHandler function;

		public TestWebSocket(URI serverUri, MessageHandler function) {
			super(serverUri);
			this.function = function;
		}

		@Override
		public void onMessage(String message) {
			try {
				this.function.onMessage(new JSONObject(message));
			} catch (JSONException e) {
				fail(e);
			}
		}

		@Override
		public void onError(Exception e) {
			fail(e);
		}

		@Override
		public void onOpen(ServerHandshake handshakedata) {

		}

		@Override
		public void onClose(int code, String reason, boolean remote) {
		}

		public void send(JSONObject data) {
			this.send(data.toString());
		}
	}

}
