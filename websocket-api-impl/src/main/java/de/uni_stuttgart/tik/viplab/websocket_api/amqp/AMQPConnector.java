package de.uni_stuttgart.tik.viplab.websocket_api.amqp;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import de.uni_stuttgart.tik.viplab.websocket_api.ViPLabBackendConnector;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import de.uni_stuttgart.tik.viplab.websocket_api.validation.ConfigurationValidatorManager;

@ApplicationScoped
public class AMQPConnector implements ViPLabBackendConnector {

	@Inject
	private ConfigurationValidatorManager configurationValidatorManager;

	@Override
	public String createComputation(ComputationTemplate template, ComputationTask task) {

		if (!configurationValidatorManager.isValid(template.configuration, template.environment)) {
			throw new IllegalArgumentException(
					"The ComputationTemplate configuration is not valid for the envrionment of the ComputationTemplate.");
		}
		// TODO
		return null;
	}

}
