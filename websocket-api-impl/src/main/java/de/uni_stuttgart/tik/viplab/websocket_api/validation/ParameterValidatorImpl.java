package de.uni_stuttgart.tik.viplab.websocket_api.validation;

import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.annotation.Timed;

import de.uni_stuttgart.tik.viplab.websocket_api.model.Parameter;

@ApplicationScoped
public class ParameterValidatorImpl implements ParameterValidator {

	@Timed(name = "InputValidation", description = "A measure of how long it takes to perform the validation of the input.")
	@Override
	public boolean isValid(String input, Parameter parameter) {
		boolean valid = true;
//		if (validation.pattern != null) {
//			valid &= isValidForPattern(input, validation.pattern);
//		}

		// TODO
		return valid;
	}

	private boolean isValidForPattern(String input, String pattern) {
		return Pattern.matches(pattern, input);
	}
}
