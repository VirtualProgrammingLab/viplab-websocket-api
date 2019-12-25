package de.uni_stuttgart.tik.viplab.websocket_api;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.json.JSONObject;

import com.auth0.jwt.algorithms.Algorithm;

public class JWTUtil {

	/**
	 * Create an Algorithm from a private key given by JWKSet and the id. The
	 * Algorithm can be used to sign JWTs.
	 * 
	 * @param jwks
	 * @param privateKeyId
	 * @return
	 */
	public static Algorithm getAlgorithm(URL jwks, String privateKeyId) {
		JWKSPrivateRSAKeyProvider privateKeyProvidery = new JWKSPrivateRSAKeyProvider(
				jwks, privateKeyId);
		return Algorithm.RSA512(privateKeyProvidery);
	}
	
	public static String sha256(String input) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
		return Util.bytesToHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
	}
	
	public static String jsonToBase64(JSONObject input) {
		return Base64.getUrlEncoder().encodeToString(input.toString().getBytes(StandardCharsets.UTF_8));
	}
}
