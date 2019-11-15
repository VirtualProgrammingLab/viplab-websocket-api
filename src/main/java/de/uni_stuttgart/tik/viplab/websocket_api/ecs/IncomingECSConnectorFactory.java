package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.messaging.spi.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ECS Connector as defined by Microprofile Reactive Messaging Specification
 * 
 * @author Leon Kiefer
 */
@ApplicationScoped
@Connector("ecs")
public class IncomingECSConnectorFactory implements IncomingConnectorFactory {

	private static final String SERVER_URL = "url";

	private static final Logger logger = LoggerFactory.getLogger(IncomingECSConnectorFactory.class);

	@Resource
	private ManagedScheduledExecutorService executor;

	@Override
	public PublisherBuilder<? extends Message<?>> getPublisherBuilder(Config config) {
		URI url;
		try {
			url = new URI(config.getValue(SERVER_URL, String.class));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}

		ECSMessageClient ecsClient = null;// RestClientBuilder.newBuilder().baseUri(url).build(ECSMessageClient.class);

		return ReactiveStreams.generate(() -> 0).flatMapCompletionStage(v -> {
			CompletableFuture<Message<Object>> result = new CompletableFuture<>();
			result.handle(IncomingECSConnectorFactory::logPollFailure);

			this.executor.submit(() -> {
				executePollActions(result, ecsClient);
			});

			return result;
		});
	}

	private void executePollActions(CompletableFuture<Message<Object>> result, ECSMessageClient ecsClient) {
		Response response = ecsClient.removeFirstMessage();
		if (response.getLength() > 0) {
			Object entity = response.getEntity();

			Message<Object> message = Message.of(entity);
			result.complete(message);
		} else {
			this.executor.execute(() -> {
				executePollActions(result, ecsClient);
			});
		}
	}

	private static <T> Void logPollFailure(T result, Throwable t) {
		if (t != null) {
			logger.error("Filed to poll ecs", t);
		}
		return null;
	}
}
