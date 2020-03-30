package de.uni_stuttgart.tik.viplab.websocket_api.validation;

import javax.enterprise.context.ApplicationScoped;

import de.uni_stuttgart.tik.viplab.websocket_api.model.Check;

@ApplicationScoped
public class InputValidatorImpl implements InputValidator {

	@Override
	public boolean isValid(String input, Check check) {
		// TODO
		return true;
	}
}
