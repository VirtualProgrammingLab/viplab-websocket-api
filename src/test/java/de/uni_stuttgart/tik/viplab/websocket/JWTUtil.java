package de.uni_stuttgart.tik.viplab.websocket;

import java.net.URL;

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
}
