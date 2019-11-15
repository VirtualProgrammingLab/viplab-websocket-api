package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;

@ApplicationScoped
public class ECSComputationService {
	public void createComputation(ComputationTemplate template, ComputationTask task) {
		//TODO
	}
	
	@Incoming("results")
	public CompletionStage<Void> getMessages(Message<Object> message) {
		return message.ack();
	}
}
