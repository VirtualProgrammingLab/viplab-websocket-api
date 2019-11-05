package de.uni_stuttgart.tik.viplab.websocket;

import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAMultiPrimePrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Base64;
import java.util.Map;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.interfaces.RSAKeyProvider;

public class JWKSPrivateRSAKeyProvider implements RSAKeyProvider {

	private URL url;
	private String privateKeyId;

	public JWKSPrivateRSAKeyProvider(URL url, String privateKeyId) {
		this.url = url;
		this.privateKeyId = privateKeyId;
	}

	@Override
	public RSAPublicKey getPublicKeyById(String keyId) {
		try {
			return (RSAPublicKey) new JwkProviderBuilder(url).build().get(keyId)
					.getPublicKey();
		} catch (JwkException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public String getPrivateKeyId() {
		return privateKeyId;
	}

	@Override
	public RSAPrivateKey getPrivateKey() {
		// From:
		// https://github.com/auth0/jwks-rsa-java/issues/53#issuecomment-479001886
		// For details of magic strings below see:
		// https://tools.ietf.org/html/rfc7518#section-6.3
		try {
			Jwk jwk = new JwkProviderBuilder(url).build().get(privateKeyId);
			Map<String, Object> additionalAttributes = jwk
					.getAdditionalAttributes();
			KeyFactory kf = KeyFactory.getInstance("RSA");

			BigInteger modules = getValues(additionalAttributes, "n");
			BigInteger publicExponent = getValues(additionalAttributes, "e");
			BigInteger privateExponent = getValues(additionalAttributes, "d");
			BigInteger primeP = getValues(additionalAttributes, "p");
			BigInteger primeQ = getValues(additionalAttributes, "q");
			BigInteger primeExponentP = getValues(additionalAttributes, "dp");
			BigInteger primeExponentQ = getValues(additionalAttributes, "dq");
			BigInteger crtCoefficient = getValues(additionalAttributes, "qi");

			RSAPrivateKeySpec privateKeySpec = new RSAMultiPrimePrivateCrtKeySpec(
					modules, publicExponent, privateExponent, primeP, primeQ,
					primeExponentP, primeExponentQ, crtCoefficient, null);

			return (RSAPrivateKey) kf.generatePrivate(privateKeySpec);

		} catch (JwkException e) {
			throw new IllegalStateException("Private key not found", e);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	private BigInteger getValues(Map<String, Object> additionalAttributes,
			String key) {
		return new BigInteger(1, Base64.getUrlDecoder()
				.decode((String) additionalAttributes.get(key)));
	}
}
