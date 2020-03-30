package de.uni_stuttgart.tik.viplab.websocket_api.validation;

import java.util.Map;

public interface ConfigurationValidatorManager {

	boolean isValid(Map<String, Object> configuration, String environment);

}
