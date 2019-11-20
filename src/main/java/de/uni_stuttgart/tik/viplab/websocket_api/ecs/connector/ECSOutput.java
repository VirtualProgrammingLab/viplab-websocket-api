package de.uni_stuttgart.tik.viplab.websocket_api.ecs.connector;

import javax.json.bind.JsonbBuilder;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_stuttgart.tik.viplab.websocket_api.ecs.ECSMessageClient;

public class ECSOutput<T> {
	
	private static Logger logger = LoggerFactory.getLogger(ECSOutput.class); 

	private final ECSMessageClient ecsClient;

	public ECSOutput(ECSMessageClient ecsClient) {
		this.ecsClient = ecsClient;
	}

	public SubscriberBuilder<Message<T>, Void> getSubscriber() {
		return ReactiveStreams.<Message<T>> builder().forEach(this::sendMessage);
	}

	private void sendMessage(Message<T> message) {
		try {
			System.out.println("hu");
			
			this.ecsClient.createMessage(JsonbBuilder.create().toJson(message.getPayload()), "1");
			message.ack();
		} catch (Exception e) {
			logger.error("", e);
			throw e;
		}
	}
}
