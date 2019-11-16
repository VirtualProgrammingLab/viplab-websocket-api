package de.uni_stuttgart.tik.viplab.websocket_api.ecs.viplab;

import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import de.uni_stuttgart.tik.viplab.websocket_api.ComputationWebSocket;
import de.uni_stuttgart.tik.viplab.websocket_api.NotificationService;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;

@ApplicationScoped
public class ECSComputationService {

	@Inject
	@Channel("exercises")
	private Emitter<Object> exercises;

	@Inject
	@Channel("solutions")
	private Emitter<Object> solutions;

	@Inject
	private NotificationService notificationService;

	public void createComputation(ComputationTemplate template, ComputationTask task) {
		createExercise(template);
		sendSolutions(task);
	}

	@Incoming("results")
	public CompletionStage<Void> getMessages(Message<Object> message) {
		notificationService.notify("test", session -> {
			ComputationWebSocket.send(message.getPayload(), session);
		});
		return message.ack();
	}

	public void createExercise(Object msg) {
		exercises.send(msg);
	}

	public void sendSolutions(Object msg) {
		solutions.send(msg);
	}

}
