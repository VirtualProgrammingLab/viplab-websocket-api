package de.uni_stuttgart.tik.viplab.websocket;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import uk.co.datumedge.hamcrest.json.SameJSONAs;

@ExtendWith(MockitoExtension.class)
class ComputationWebSocketIT {

	private static String websocketPort = System
			.getProperty("liberty.http.port");
	private static String jwksPath = System
			.getProperty("viplab.jwt.jwks.file.private.test");
	private static String kid = System.getProperty("viplab.jwt.jwks.kid.test");

	@Mock
	Function<JSONObject, String> massageHandler;
	private static Algorithm algorithm;

	@BeforeAll
	public static void setup() throws MalformedURLException {
		algorithm = JWTUtil.getAlgorithm(Paths.get(jwksPath).toUri().toURL(),
				kid);
	}

	@Test
	void test() throws Exception {
		Mockito.when(
				massageHandler.apply(ArgumentMatchers.any(JSONObject.class)))
				.thenReturn("");

		TestWebSocket websocket = new TestWebSocket(new URI("ws://localhost:"
				+ websocketPort + "/websocket-api/computations"),
				massageHandler);
		assertThat("WebSocket connection",
				websocket.connectBlocking(100, TimeUnit.MILLISECONDS));
		JSONObject authenticateMessage = new JSONObject();
		authenticateMessage.put("type", "authenticate");
		String jwt = JWT.create().withIssuer("test").sign(algorithm);
		authenticateMessage.put("content", jwt);
		websocket.send(authenticateMessage.toString());

		Mockito.verify(massageHandler, Mockito.timeout(100))
				.apply((JSONObject) argThat(SameJSONAs.sameJSONAs(
						"{\"content\":\"world\",\"type\":\"hello\"}")));
		websocket.closeBlocking();
	}

	private class TestWebSocket extends WebSocketClient {

		private Function<JSONObject, String> function;

		public TestWebSocket(URI serverUri,
				Function<JSONObject, String> function) {
			super(serverUri);
			this.function = function;
		}

		@Override
		public void onMessage(String message) {
			try {
				this.send(this.function.apply(new JSONObject(message)));
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
	}

}
