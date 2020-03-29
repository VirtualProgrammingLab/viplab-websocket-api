package de.uni_stuttgart.tik.viplab.websocket_api;

import java.net.MalformedURLException;
import java.nio.file.Paths;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

/**
 * This Class/Test is used to generate valid/signed messages to test the
 * websocket-api.
 */
class GenerateJWTTest {

	private static String jwksPath = System.getProperty("viplab.jwt.jwks.file.private.test");
	private static String kid = System.getProperty("viplab.jwt.jwks.kid.test");

	@Test
	void generateAndPrint() throws MalformedURLException {
		Algorithm algorithm = JWTUtil.getAlgorithm(Paths.get(jwksPath).toUri().toURL(), kid);

		JSONObject computationTemplate = TestJSONMessageProvider.getComputationTemplate("Java");
		String computationTemplateBase64 = JWTUtil.jsonToBase64(computationTemplate);
		String computationTemplateHash = JWTUtil.sha256(computationTemplateBase64);
		String jwt = JWT.create().withIssuer("test")
				.withClaim("viplab.computation-template.digest", computationTemplateHash).sign(algorithm);
		JSONObject authenticationMessage = TestJSONMessageProvider.getAuthenticationMessage(jwt);

		JSONObject computationTask = TestJSONMessageProvider.getComputationTask(computationTemplate);
		JSONObject createComputationMessage = TestJSONMessageProvider
				.getCreateComputationMessage(computationTemplateBase64, computationTask);

		MatcherAssert.assertThat(authenticationMessage, Matchers.notNullValue());
		MatcherAssert.assertThat(createComputationMessage, Matchers.notNullValue());
		System.out.println(authenticationMessage);
		System.out.println(createComputationMessage);
	}
}
