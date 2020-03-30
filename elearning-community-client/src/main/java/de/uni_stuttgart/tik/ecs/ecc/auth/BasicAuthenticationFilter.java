package de.uni_stuttgart.tik.ecs.ecc.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@Provider
public class BasicAuthenticationFilter implements ClientRequestFilter {

	private final String user;
	private final String password;

	public BasicAuthenticationFilter(String user, String password) {
		if (user == null || password == null) {
			throw new NullPointerException();
		}
		this.user = user;
		this.password = password;
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		MultivaluedMap<String, Object> headers = requestContext.getHeaders();
		String value = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + value);
	}

}
