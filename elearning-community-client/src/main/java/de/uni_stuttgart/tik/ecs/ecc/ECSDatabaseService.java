package de.uni_stuttgart.tik.ecs.ecc;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import de.uni_stuttgart.tik.ecs.ecc.auth.BasicAuthenticationFilter;

/**
 * Represent a persistent ECS Resource
 *
 * @param <T>
 */
public class ECSDatabaseService<T> {

	private final ECSMessageClient ecsClient;
	private final String receiverMemberships;
	private final URI url;

	public ECSDatabaseService(URI url, String username, String password, String receiverMemberships) {
		this.url = url;
		this.receiverMemberships = receiverMemberships;
		this.ecsClient = RestClientBuilder.newBuilder().baseUri(url)
				.register(new BasicAuthenticationFilter(username, password)).build(ECSMessageClient.class);
	}

	public URI store(T data) {
		try (Response createMessage = this.ecsClient.createMessage(data, receiverMemberships)) {
			return this.url.resolve(createMessage.getLocation());
		}
	}
}
