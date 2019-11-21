package de.uni_stuttgart.tik.ecs.ecc.connector;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import de.uni_stuttgart.tik.ecs.ecc.ECSMessageClient;
import de.uni_stuttgart.tik.ecs.ecc.auth.BasicAuthenticationFilter;

@ApplicationScoped
@Connector("ecs")
public class OutgoingECSConnectorFactory implements OutgoingConnectorFactory {

	@Override
	public SubscriberBuilder<? extends Message<?>, Void> getSubscriberBuilder(Config config) {
		URI url = URI.create(config.getValue(ConnectorConfig.SERVER_URL, String.class));
		String username = config.getValue(ConnectorConfig.USERNAME, String.class);
		String password = config.getValue(ConnectorConfig.PASSWORD, String.class);
		String receiverMemberships = config.getValue(ConnectorConfig.RECEIVER_MEMBERSHIPS, String.class);

		ECSMessageClient ecsClient = RestClientBuilder.newBuilder().baseUri(url)
				.register(new BasicAuthenticationFilter(username, password)).build(ECSMessageClient.class);

		ECSOutput<Object> ecsOutput = new ECSOutput<>(ecsClient, receiverMemberships);
		return ecsOutput.getSubscriber();
	}
}
