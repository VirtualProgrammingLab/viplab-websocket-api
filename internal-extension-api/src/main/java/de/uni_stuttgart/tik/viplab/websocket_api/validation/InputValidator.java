package de.uni_stuttgart.tik.viplab.websocket_api.validation;

import de.uni_stuttgart.tik.viplab.websocket_api.model.Validation;

public interface InputValidator {
	boolean isValid(String input, Validation validation);
}
