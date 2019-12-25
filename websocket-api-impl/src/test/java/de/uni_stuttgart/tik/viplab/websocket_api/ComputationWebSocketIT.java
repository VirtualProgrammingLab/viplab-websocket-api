package de.uni_stuttgart.tik.viplab.websocket_api;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Options.ChunkedEncodingPolicy;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

import de.uni_stuttgart.tik.viplab.websocket_api.ecs.Solution;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

@ExtendWith(MockitoExtension.class)
class ComputationWebSocketIT {

	private static String websocketPort = System.getProperty("liberty.http.port");
	private static String jwksPath = System.getProperty("viplab.jwt.jwks.file.private.test");
	private static String kid = System.getProperty("viplab.jwt.jwks.kid.test");

	@Mock
	MessageHandler massageHandler;
	private WireMockServer ecs;

	private static Algorithm algorithm;
	private static URI webSocketUri;

	@BeforeAll
	public static void setup() throws MalformedURLException, URISyntaxException {
		algorithm = JWTUtil.getAlgorithm(Paths.get(jwksPath).toUri().toURL(), kid);
		webSocketUri = new URI("ws://localhost:" + websocketPort + "/websocket-api/computations");
	}

	@BeforeEach
	public void setupWireMockServer() {
		ecs = new WireMockServer(options().port(8080).useChunkedTransferEncoding(ChunkedEncodingPolicy.NEVER)
				.extensions(new ResponseTemplateTransformer(false, "computation-id", new Helper<String>() {
					@Override
					String apply(String context, com.github.jknack.handlebars.Options options) {
						return computationID;
					}
				})));
		ecs.start();
	}

	@AfterEach
	private void stopWireMockServer() {
		ecs.stop();
		ecs = null;
	}

	@Test
	void testConnectionIsPossible() throws InterruptedException {
		TestWebSocket websocket = new TestWebSocket(webSocketUri, massageHandler);
		assertThat("WebSocket connection to " + webSocketUri, websocket.connectBlocking(1000, TimeUnit.MILLISECONDS));
	}

	String computationID = null;

	@Test
	void testCreateComputation() throws Exception {

		// stubbing
		ecs.stubFor(post("/numlab/exercises").inScenario("Create Computation"));
		ecs.stubFor(post("/numlab/solutions").inScenario("Create Computation").willSetStateTo("Result available"));
		ecs.stubFor(post("/numlab/results/fifo").inScenario("Create Computation").whenScenarioStateIs(Scenario.STARTED)
				.willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
		ecs.stubFor(
				post("/numlab/results/fifo").inScenario("Create Computation").whenScenarioStateIs("Result available")
						.willReturn(ResponseDefinitionBuilder.okForJson(TestECSJSONProvider.getResult(new Solution())))
						.willSetStateTo(Scenario.STARTED));
		ecs.addMockServiceRequestListener((request, response) -> {
			if (request.getUrl().contains("/numlab/solutions")) {
				String id = Json.read(request.getBodyAsString(), Solution.Wrapper.class).Solution.ID;
				assertThat(id, not(emptyOrNullString()));
				assertThat(computationID, is(nullValue()));
				computationID = id;
			}
		});
		// create resources
		String computationTemplate = JWTUtil.jsonToBase64(TestJSONMessageProvider.getComputationTemplate("Java"));
		String computationTemplateHash = JWTUtil.sha256(computationTemplate);
		String jwt = JWT.create().withIssuer("test")
				.withClaim("viplab.computation-template.digest", computationTemplateHash).sign(algorithm);
		// connection
		TestWebSocket websocket = new TestWebSocket(webSocketUri, massageHandler);
		websocket.connectBlocking(1000, TimeUnit.MILLISECONDS);
		// authenticate
		websocket.send(TestJSONMessageProvider.getAuthenticationMessage(jwt));
		// create computation
		JSONObject computationTask = TestJSONMessageProvider.getComputationTask();
		websocket.send(TestJSONMessageProvider.getCreateComputationMessage(computationTemplate, computationTask));
		// stub the result

		// verify result
		ArgumentCaptor<JSONObject> messagesCaptor = ArgumentCaptor.forClass(JSONObject.class);
		Mockito.verify(massageHandler, Mockito.timeout(1100)).onMessage(messagesCaptor.capture());
		List<JSONObject> messages = messagesCaptor.getAllValues();
		assertThat(messages.get(0), SameJSONAs.sameJSONObjectAs(new JSONObject().put("type", "computation"))
				.allowingExtraUnexpectedFields());
		assertThat(messages.get(1),
				SameJSONAs.sameJSONObjectAs(new JSONObject().put("type", "result")).allowingExtraUnexpectedFields());
		// close connection
		websocket.closeBlocking();
	}

	@Test
	void testAuthenticationIsPerformed() throws InterruptedException, JSONException {
		// create resources
		String computationTemplate = JWTUtil.jsonToBase64(TestJSONMessageProvider.getComputationTemplate("Java"));
		String computationTemplate2 = JWTUtil.jsonToBase64(TestJSONMessageProvider.getComputationTemplate("C"));
		String computationTemplateHash = JWTUtil.sha256(computationTemplate);
		String jwt = JWT.create().withIssuer("test")
				.withClaim("viplab.computation-template.digest", computationTemplateHash).sign(algorithm);
		// connection
		TestWebSocket websocket = new TestWebSocket(webSocketUri, massageHandler);
		websocket.connectBlocking(1000, TimeUnit.MILLISECONDS);
		// authenticate
		websocket.send(TestJSONMessageProvider.getAuthenticationMessage(jwt));
		// try create Task
		JSONObject computationTask = TestJSONMessageProvider.getComputationTask();
		websocket.send(TestJSONMessageProvider.getCreateComputationMessage(computationTemplate2, computationTask));
		// verify result
		ArgumentCaptor<JSONObject> messagesCaptor = ArgumentCaptor.forClass(JSONObject.class);
		Mockito.verify(massageHandler, Mockito.timeout(1000)).onMessage(messagesCaptor.capture());
		List<JSONObject> messages = messagesCaptor.getAllValues();
		assertThat("received one response", messages, hasSize(1));
		assertThat(messages.get(0),
				SameJSONAs.sameJSONObjectAs(new JSONObject().put("type", "error")).allowingExtraUnexpectedFields());
		// close connection
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
