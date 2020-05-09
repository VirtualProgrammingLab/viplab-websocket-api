package de.uni_stuttgart.tik.viplab.websocket_api.validation;

import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.annotation.Timed;

import de.uni_stuttgart.tik.viplab.websocket_api.model.Validation;

@ApplicationScoped
public class InputValidatorImpl implements InputValidator {

	@Timed(name = "InputValidation", description = "A measure of how long it takes to perform the validation of the input.")
	@Override
	public boolean isValid(String input, Validation validation) {
		boolean valid = true;
		if (validation.pattern != null) {
			valid &= isValidForPattern(input, validation.pattern);
		}

		// TODO
		return valid;
	}

	private boolean isValidForPattern(String input, String pattern) {
		return Pattern.matches(pattern, input);
	}
}
