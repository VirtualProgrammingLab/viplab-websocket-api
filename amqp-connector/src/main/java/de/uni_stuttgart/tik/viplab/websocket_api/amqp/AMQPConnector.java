package de.uni_stuttgart.tik.viplab.websocket_api.amqp;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import de.uni_stuttgart.tik.viplab.websocket_api.ViPLabBackendConnector;
import de.uni_stuttgart.tik.viplab.websocket_api.model.Computation;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import de.uni_stuttgart.tik.viplab.websocket_api.transformation.ComputationMerger;

@ApplicationScoped
public class AMQPConnector implements ViPLabBackendConnector {

	@Inject
	@Channel("computations")
	private Emitter<String> computations;

	@Inject
	private ComputationMerger merger;

	private Jsonb jsonb = JsonbBuilder.create();

	@Override
	public String createComputation(ComputationTemplate template, ComputationTask task) {
		Computation computation = merger.merge(template, task);
		String computationJson = jsonb.toJson(computation);
		computations.send(computationJson);

		System.out.println("Template: " + template.identifier);
		System.out.println("Task: " + task.identifier);
		System.out.println("Computation: " + computation.identifier);
		return computation.identifier;
	}

}
