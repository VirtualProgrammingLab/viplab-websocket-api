package de.uni_stuttgart.tik.viplab.websocket_api.authentication;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import de.uni_stuttgart.tik.viplab.websocket_api.misc.Util;

@ApplicationScoped
public class AuthenticationService {
	private Algorithm rsa;

	@Inject
	@ConfigProperty(name = "viplab.jwt.jwks.file")
	String jwksPath;

	@PostConstruct
	void setup() {
		URL url;
		try {
			url = Paths.get(jwksPath).toUri().toURL();
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Error creating url from viplab.jwt.jwks.file path", e);
		}
		this.rsa = Algorithm.RSA512(new JWKSRSAKeyProvider(url));
	}

	/**
	 * Try to authenticate with a JWT.
	 * 
	 * @param rawJWT The JWT as encoded string
	 * @return the decoded JWT with claims if valid
	 * @throws IllegalArgumentException if the given string is not a valid JWT
	 */
	public DecodedJWT authenticate(String rawJWT) {
		try {
			return JWT.require(rsa).build().verify(rawJWT);
		} catch (JWTVerificationException e) {
			throw new IllegalArgumentException("The given string is not a valid JWT", e);
		}
	}

	/**
	 * Verify a data string using a digest.
	 * 
	 * @param data
	 *            the data string
	 * @param digest
	 * @throws IllegalArgumentException
	 *             if the data can't be verified
	 */
	public void verify(String data, String digest) {
		MessageDigest digestSHA;
		try {
			digestSHA = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
		String digestOfData = Util.bytesToHex(digestSHA.digest(data.getBytes(StandardCharsets.UTF_8)));
		if (!digestOfData.equals(digest)) {
			throw new IllegalArgumentException("The data can't be verified with the given digest!");
		}
	}

}
