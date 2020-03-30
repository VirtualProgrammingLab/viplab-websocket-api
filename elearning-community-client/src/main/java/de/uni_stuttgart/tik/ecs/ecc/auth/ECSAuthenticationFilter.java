package de.uni_stuttgart.tik.ecs.ecc.auth;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@Provider
public class ECSAuthenticationFilter implements ClientRequestFilter {

	private final String ecsAuthId;

	public ECSAuthenticationFilter(String ecsAuthId) {
		if (ecsAuthId == null) {
			throw new IllegalArgumentException("ecsAuthId can't be null");
		}
		this.ecsAuthId = ecsAuthId;
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		MultivaluedMap<String, Object> headers = requestContext.getHeaders();
		headers.add("X-EcsAuthId", ecsAuthId);
	}
}
