package de.uni_stuttgart.tik.viplab.websocket_api.validation;

import java.util.Map;

public interface ConfigurationValidator {
	/**
	 * Validate the given configuration for an environment
	 * 
	 * @param configuration
	 *            the configuration parsed for json, not null
	 * @return true if the given configuration is valid for the environment and
	 *         all required properties are set.
	 */
	public boolean isValid(Map<String, Object> configuration);
}
