package de.uni_stuttgart.tik.viplab.websocket_api.validation;

import de.uni_stuttgart.tik.viplab.websocket_api.model.Parameter;

public interface ParameterValidator {
	boolean isValid(String input, Parameter parameter);
}
