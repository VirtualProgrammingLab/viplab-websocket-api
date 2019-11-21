package de.uni_stuttgart.tik.ecs.ecc.connector;

import java.util.concurrent.CompletableFuture;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import de.uni_stuttgart.tik.ecs.ecc.ECSMessageClient;

public class ECSInput<T> {

	private final ECSMessageClient ecsClient;
	private final ManagedScheduledExecutorService executor;
	private final Class<T> messageType;

	private boolean running = true;

	public ECSInput(ECSMessageClient ecsClient, ManagedScheduledExecutorService executor, Class<T> messageType) {
		this.ecsClient = ecsClient;
		this.executor = executor;
		this.messageType = messageType;
	}

	public PublisherBuilder<Message<T>> getPublisher() {
		return ReactiveStreams.generate(() -> 0).flatMapCompletionStage(v -> {
			CompletableFuture<Message<T>> result = new CompletableFuture<>();

			this.executor.submit(() -> executePollActions(result, ecsClient));

			return result;
		});
	}

	private void executePollActions(CompletableFuture<Message<T>> result, ECSMessageClient ecsClient) {
		try {
			while (this.running) {
				Response response = ecsClient.removeFirstMessage();

				if (response.getLength() > 0) {
					T entity = response.readEntity(messageType);

					Message<T> message = Message.of(entity);
					result.complete(message);
					break;
				}
			}
		} catch (Throwable t) {
			result.completeExceptionally(new IllegalStateException("Failed to poll ecs", t));
		}
	}

	/**
	 * Stop the current polling action of this ECSInput.
	 */
	public void shutdown() {
		this.running = false;
	}

}
