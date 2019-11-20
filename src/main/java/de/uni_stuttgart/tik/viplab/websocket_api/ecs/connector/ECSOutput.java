package de.uni_stuttgart.tik.viplab.websocket_api.ecs.connector;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_stuttgart.tik.viplab.websocket_api.ecs.ECSMessageClient;

public class ECSOutput<T> {

	private static Logger logger = LoggerFactory.getLogger(ECSOutput.class);

	private final ECSMessageClient ecsClient;
	private final String receiverMemberships;

	public ECSOutput(ECSMessageClient ecsClient, String receiverMemberships) {
		this.ecsClient = ecsClient;
		this.receiverMemberships = receiverMemberships;
	}

	public SubscriberBuilder<Message<T>, Void> getSubscriber() {
		return ReactiveStreams.<Message<T>> builder().onError(ECSOutput::reportErrorInStream)
				.forEach(this::sendMessage);
	}

	private void sendMessage(Message<T> message) {
		try {
			this.ecsClient.createMessage(message.getPayload(), receiverMemberships);
			message.ack();
		} catch (Exception e) {
			logger.error("There was an error while sending a message.", e);
		}
	}

	private static void reportErrorInStream(Throwable t) {
		logger.error("There was an error in the output channel.", t);
	}
}
