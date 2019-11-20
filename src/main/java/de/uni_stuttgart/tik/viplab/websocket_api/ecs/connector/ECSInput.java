package de.uni_stuttgart.tik.viplab.websocket_api.ecs.connector;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_stuttgart.tik.viplab.websocket_api.ecs.ECSMessageClient;

public class ECSInput<T> {

	private static final Logger logger = LoggerFactory.getLogger(ECSInput.class);

	private final ECSMessageClient ecsClient;
	private final ManagedScheduledExecutorService executor;
	private final Class<T> messageType;

	public ECSInput(ECSMessageClient ecsClient, ManagedScheduledExecutorService executor, Class<T> messageType) {
		this.ecsClient = ecsClient;
		this.executor = executor;
		this.messageType = messageType;
	}

	public PublisherBuilder<Message<T>> getPublisher() {
		return ReactiveStreams.generate(() -> 0).flatMapCompletionStage(v -> {
			CompletableFuture<Message<T>> result = new CompletableFuture<>();
			result.handle(ECSInput::logPollFailure);

			this.executor.submit(() -> {
				executePollActions(result, ecsClient);
			});

			return result;
		});
	}

	private void executePollActions(CompletableFuture<Message<T>> result, ECSMessageClient ecsClient) {
		Response response = null;
		try {
			response = ecsClient.removeFirstMessage();
		} catch (Exception e) {
			logger.error("Failed to poll ecs", e);
			return;
		}

		if (response.getLength() > 0) {
			T entity = response.readEntity(messageType);

			Message<T> message = Message.of(entity);
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
