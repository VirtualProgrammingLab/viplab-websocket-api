package de.uni_stuttgart.tik.viplab.websocket_api;

import java.util.concurrent.CompletionStage;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;

/**
 * These connectors are used by the websocket api to interact with the rest of
 * the ViPLab backend infrastructure.
 * 
 * @author Leon Kiefer
 */
public interface ViPLabBackendConnector {

	/**
	 * Create and start a new Computation on the backend using this connector.
	 * 
	 * @param template
	 *            the Computation Template
	 * @param task
	 *            the Computation Task
	 * @return The correlation id of the created computation
	 * @throws IllegalArgumentException
	 *             if the given template or task are not valid
	 */
	CompletionStage<String> createComputation(ComputationTemplate template, ComputationTask task);

	/**
	 * Prepare the Computation on the backend using this connector
	 * 
	 * @param template the Computation Template
	 * 
	 * @return The correlation id of the prepared computation
	 */
	CompletionStage<String> prepareComputation(ComputationTemplate template);
}
