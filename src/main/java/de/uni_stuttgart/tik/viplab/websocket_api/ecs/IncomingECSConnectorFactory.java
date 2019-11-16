package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_stuttgart.tik.viplab.websocket_api.ecs.auth.ECSAuthenticationFilter;

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
		URI url = URI.create(config.getValue(SERVER_URL, String.class));

		ECSMessageClient ecsClient = RestClientBuilder.newBuilder().baseUri(url)
				.register(new ECSAuthenticationFilter("id2")).build(ECSMessageClient.class);

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
		Response response = null;
		try {
			response = ecsClient.removeFirstMessage();
		} catch (Exception e) {
			logger.error("Failed to poll ecs", e);
			return;
		}

		if (response.getLength() > 0) {
			Object entity = response.getEntity();

			Message<Object> message = Message.of(entity);
			result.complete(message);
			logger.info("got message");
		} else {
			this.executor.submit(() -> {
				executePollActions(result, ecsClient);
			});
		}
	}

	private static <T> Void logPollFailure(T result, Throwable t) {
		if (t != null) {
			logger.error("Failed to poll ecs", t);
		}
		return null;
	}
}
