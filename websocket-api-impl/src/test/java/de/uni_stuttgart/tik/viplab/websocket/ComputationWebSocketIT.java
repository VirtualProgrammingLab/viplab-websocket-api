package de.uni_stuttgart.tik.viplab.websocket;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.tomakehurst.wiremock.WireMockServer;

import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import ru.lanwen.wiremock.ext.WiremockUriResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver.WiremockUri;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

@ExtendWith({ MockitoExtension.class, WiremockResolver.class, WiremockUriResolver.class })
class ComputationWebSocketIT {

	private static String websocketPort = System.getProperty("liberty.http.port");
	private static String jwksPath = System.getProperty("viplab.jwt.jwks.file.private.test");
	private static String kid = System.getProperty("viplab.jwt.jwks.kid.test");

	@Mock
	MessageHandler massageHandler;

	private static Algorithm algorithm;
	private static URI webSocketUri;

	@BeforeAll
	public static void setup() throws MalformedURLException, URISyntaxException {
		algorithm = JWTUtil.getAlgorithm(Paths.get(jwksPath).toUri().toURL(), kid);
		webSocketUri = new URI("ws://localhost:" + websocketPort + "/websocket-api/computations");
	}

	@Test
	void testCreateComputation(@Wiremock WireMockServer server, @WiremockUri String uri) throws Exception {

		TestWebSocket websocket = new TestWebSocket(webSocketUri, massageHandler);
		assertThat("WebSocket connection to " + webSocketUri, websocket.connectBlocking(1000, TimeUnit.MILLISECONDS));
		JSONObject authenticateMessage = new JSONObject();
		authenticateMessage.put("type", "authenticate");
		String computationTemplate = JWTUtil.jsonToBase64(TestJSONMessageProvider.getComputationTemplate());
		String computationTemplateHash = JWTUtil.sha256(computationTemplate);
		System.out.println(computationTemplateHash);
		String jwt = JWT.create().withIssuer("test")
				.withClaim("viplab.computation-template.digest", computationTemplateHash).sign(algorithm);
		authenticateMessage.put("content", TestJSONMessageProvider.getAuthenticationMessage(jwt));
		websocket.send(authenticateMessage);
		JSONObject computationTask = TestJSONMessageProvider.getComputationTask();
		websocket.send(TestJSONMessageProvider.getCreateComputationMessage(computationTemplate, computationTask));

		Mockito.verify(massageHandler, Mockito.timeout(300)).onMessage(
				(JSONObject) argThat(SameJSONAs.sameJSONObjectAs(new JSONObject()).allowingExtraUnexpectedFields()));
		websocket.closeBlocking();
	}

	private interface MessageHandler {
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
