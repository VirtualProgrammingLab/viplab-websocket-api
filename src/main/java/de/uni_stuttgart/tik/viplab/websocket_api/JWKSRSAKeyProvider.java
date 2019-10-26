package de.uni_stuttgart.tik.viplab.websocket_api;

import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.interfaces.RSAKeyProvider;

public class JWKSRSAKeyProvider implements RSAKeyProvider {

	private URL url;

	public JWKSRSAKeyProvider(URL url) {
		this.url = url;
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
		return null;
	}

	@Override
	public RSAPrivateKey getPrivateKey() {
		return null;
	}

}
