package de.uni_stuttgart.tik.viplab.websocket_api.amqp;

import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import de.uni_stuttgart.tik.viplab.websocket_api.NotificationService;
import de.uni_stuttgart.tik.viplab.websocket_api.ViPLabBackendConnector;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.ComputationResultMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.model.Computation;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import de.uni_stuttgart.tik.viplab.websocket_api.transformation.ComputationMerger;

@ApplicationScoped
public class AMQPConnector implements ViPLabBackendConnector {

	@Inject
	@Channel("computations")
	Emitter<String> computations;

	@Inject
	NotificationService notificationService;

	@Inject
	ComputationMerger merger;

	private Jsonb jsonb;

	@PostConstruct
	public void init() {
		JsonbConfig config = new JsonbConfig().withDeserializers(new ArtifactDeserializer());
		jsonb = JsonbBuilder.create(config);
	}

	@Override
	public CompletionStage<String> createComputation(ComputationTemplate template, ComputationTask task) {
		Computation computation = merger.merge(template, task);
		String computationJson = jsonb.toJson(computation);
		
		return computations.send(computationJson).thenApply(v -> {
			return computation.identifier;
		});
	}

	@Incoming("results")
	public CompletionStage<Void> processResults(Message<String> message) {
		try {
			ComputationResult result = jsonb.fromJson(message.getPayload(), ComputationResult.class);
			ComputationResultMessage resultMessage = new ComputationResultMessage(result);
			notificationService.notify("computation:" + result.computation, session -> {
				session.send(resultMessage);
			});
			return message.ack();
		} catch (Exception e) {
			e.printStackTrace();
			return message.ack();
		}
	}

}
