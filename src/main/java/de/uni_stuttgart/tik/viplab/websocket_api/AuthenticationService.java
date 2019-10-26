package de.uni_stuttgart.tik.viplab.websocket_api;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

@ApplicationScoped
public class AuthenticationService {
	private Algorithm rsa;

	@Inject
	@ConfigProperty(name = "viplab.jwt.jwks.file")
	private String jwksPath;

	@PostConstruct
	private void setup() {
		URL url;
		try {
			url = Paths.get(jwksPath).toUri().toURL();
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Error creating url from viplab.jwt.jwks.file path", e);
		}
		this.rsa = Algorithm.RSA512(new JWKSRSAKeyProvider(url));
	}

	public DecodedJWT authenticate(String rawJWT) {
		return JWT.require(rsa).build().verify(rawJWT);
	}

}
