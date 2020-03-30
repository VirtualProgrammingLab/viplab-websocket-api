package de.uni_stuttgart.tik.ecs.ecc.connector;

import java.util.concurrent.CompletableFuture;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_stuttgart.tik.ecs.ecc.ECSMessageClient;

public class ECSInput<T> {
	
	private static Logger logger = LoggerFactory.getLogger(ECSInput.class);

	private final ECSMessageClient ecsClient;
	private final ManagedScheduledExecutorService executor;
	private final Class<T> messageType;
	private final long pollingDelay;

	private boolean running = true;

	public boolean isRunning() {
		return running;
	}

	/**
	 * 
	 * @param ecsClient
	 *            the ecs client used to poll the remote ecs
	 * @param executor
	 *            an ExecutorService to run the polling Thread
	 * @param messageType
	 *            the class of the message type
	 * @param pollingDelay
	 *            the delay in milliseconds between polling when there was no
	 *            message
	 */
	public ECSInput(ECSMessageClient ecsClient, ManagedScheduledExecutorService executor, Class<T> messageType,
			long pollingDelay) {
		this.ecsClient = ecsClient;
		this.executor = executor;
		this.messageType = messageType;
		this.pollingDelay = pollingDelay;
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
			Response response = null;
			while (this.running && response == null) {
				try {
					response = ecsClient.removeFirstMessage();
					if (response.getLength() <= 0) {
						response = null;
						Thread.sleep(pollingDelay);
					}
				} catch (WebApplicationException | ProcessingException e) {
					logger.warn("Could not poll ecs. Waiting {} ms and try again.", pollingDelay, e);
					Thread.sleep(pollingDelay);
				}
			}
			if (response != null) {
				T entity = response.readEntity(messageType);
				Message<T> message = Message.of(entity);
				result.complete(message);
			}
		} catch (Exception t) {
			result.completeExceptionally(new IllegalStateException("Unexpected error occured while polling ecs", t));
			this.running = false;
		}
	}

	/**
	 * Stop the current polling action of this ECSInput.
	 */
	public void shutdown() {
		this.running = false;
	}

}
