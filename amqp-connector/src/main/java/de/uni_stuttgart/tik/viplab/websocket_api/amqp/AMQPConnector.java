package de.uni_stuttgart.tik.viplab.websocket_api.amqp;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import de.uni_stuttgart.tik.viplab.websocket_api.ViPLabBackendConnector;
import de.uni_stuttgart.tik.viplab.websocket_api.model.Computation;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import de.uni_stuttgart.tik.viplab.websocket_api.transformation.ComputationMerger;

@ApplicationScoped
public class AMQPConnector implements ViPLabBackendConnector {

	@Inject
	private ComputationMerger merger;

	@Override
	public String createComputation(ComputationTemplate template, ComputationTask task) {


		Computation computation = merger.merge(template, task);
		
		System.out.println("Template: " + template.identifier);
		System.out.println("Task: " + task.identifier);
		System.out.println("Computation: " + computation.identifier);
		return computation.identifier;
	}

}
