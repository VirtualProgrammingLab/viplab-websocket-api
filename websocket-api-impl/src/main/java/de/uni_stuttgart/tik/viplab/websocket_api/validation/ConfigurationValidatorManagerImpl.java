package de.uni_stuttgart.tik.viplab.websocket_api.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConfigurationValidatorManagerImpl implements ConfigurationValidatorManager {

	@Inject
	@ConfigProperty(name = "viplab.validation.configuration.mustValidate", defaultValue = "false")
	boolean mustValidate;
	private final Map<String, ConfigurationValidator> configurationValidators = new HashMap<>();

	@PostConstruct
	void setup() {
		ServiceLoader<ConfigurationValidator> serviceLoader = ServiceLoader.load(ConfigurationValidator.class);
		for (ConfigurationValidator configurationValidator : serviceLoader) {
			String environment = getConfigurationValidatorEnvironment(configurationValidator);
			if (configurationValidators.containsKey(environment)) {
				throw new IllegalStateException("Multiple Configuration Validators for the environment:" + environment);
			}
			configurationValidators.put(environment, configurationValidator);
		}
	}

	private String getConfigurationValidatorEnvironment(ConfigurationValidator configurationValidator) {
		if (!configurationValidator.getClass().isAnnotationPresent(Environment.class)) {
			throw new IllegalArgumentException("The Configuration Validator has no Environment specified: "
					+ configurationValidator.getClass().getName());
		}
		return configurationValidator.getClass().getAnnotation(Environment.class).value();
	}

	@Override
	public boolean isValid(Map<String, Object> configuration, String environment) {
		ConfigurationValidator configurationValidator = configurationValidators.get(environment);
		if (configurationValidator == null) {
			return !mustValidate;
		}

		return configurationValidator.isValid(configuration);
	}
}
