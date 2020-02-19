package de.uni_stuttgart.tik.viplab.websocket_api;

import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.interfaces.RSAKeyProvider;

/**
 * A public RSA KeyProvider that uses a JwkProvider to load the public RSA key
 * form a JSON Web Key Set.
 */
public class JWKSRSAKeyProvider implements RSAKeyProvider {

	private final JwkProvider provider;

	public JWKSRSAKeyProvider(URL url) {
		this.provider = new JwkProviderBuilder(url).cached(true).build();
	}

	@Override
	public RSAPublicKey getPublicKeyById(String keyId) {
		try {
			return (RSAPublicKey) this.provider.get(keyId).getPublicKey();
		} catch (JwkException e) {
			return null;
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
