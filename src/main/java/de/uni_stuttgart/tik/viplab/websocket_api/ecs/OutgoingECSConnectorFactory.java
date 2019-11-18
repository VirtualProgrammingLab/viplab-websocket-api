package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import de.uni_stuttgart.tik.viplab.websocket_api.ecs.auth.BasicAuthenticationFilter;

@ApplicationScoped
@Connector("ecs")
public class OutgoingECSConnectorFactory implements OutgoingConnectorFactory {

	@Override
	public SubscriberBuilder<? extends Message<?>, Void> getSubscriberBuilder(Config config) {
		URI url = URI.create(config.getValue(ECSConnector.SERVER_URL, String.class));
		String username = config.getValue(ECSConnector.USERNAME, String.class);
		String password = config.getValue(ECSConnector.PASSWORD, String.class);

		ECSMessageClient ecsClient = RestClientBuilder.newBuilder().baseUri(url)
				.register(new BasicAuthenticationFilter(username, password)).build(ECSMessageClient.class);

		ECSOutput<Object> ecsOutput = new ECSOutput<>(ecsClient);
		SubscriberBuilder<Message<Object>, Void> subscriber = ecsOutput.getSubscriber();

		return ReactiveStreams.<Message<Object>> builder().to(subscriber);
	}
}
