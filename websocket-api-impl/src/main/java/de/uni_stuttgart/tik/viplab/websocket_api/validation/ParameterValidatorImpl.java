package de.uni_stuttgart.tik.viplab.websocket_api.validation;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.annotation.Timed;
import org.json.JSONArray;

import de.uni_stuttgart.tik.viplab.websocket_api.model.AnyValueParameter;
import de.uni_stuttgart.tik.viplab.websocket_api.model.FixedValueParameter;

@ApplicationScoped
public class ParameterValidatorImpl implements ParameterValidator {

	/**
	* Validate Parameters of mode any
	* Input can be String, Number or Array of Numbers
	*/
	@Timed(name = "InputValidation", description = "A measure of how long it takes to perform the validation of the input.")
	@Override
	public boolean isValid(Object input, AnyValueParameter parameter) {
		boolean valid = true;

		String inputString = null;
		Double inputNumber = null;
		JSONArray inputArray = null;
		if (input instanceof String) {
			inputString = input.toString();
		} else if (input instanceof Integer) {
			inputNumber = ((Integer) input).doubleValue();
		} else if (input instanceof Double) {
			inputNumber = ((double) input);
		} else if (input.getClass().isArray()) {
			inputArray = ((JSONArray) input);
		}
		
		switch(parameter.validation) {
			case range: 
				if (inputNumber != null) {
					valid &= isValidForNumbers(inputNumber, parameter.min, parameter.max, parameter.step);
				} else if (inputArray != null) {
					for (int i = 0; i < inputArray.length(); i++) {
						Double val = inputArray.getDouble(i);
						valid &= isValidForNumbers(val, parameter.min, parameter.max, parameter.step);
					}
				}
				break;
			case pattern:
				if (parameter.pattern != null) {
					if (inputString.startsWith("base64:")) {
						inputString = inputString.replaceFirst("^base64:", "");
						inputString = new String(Base64.getUrlDecoder()
								.decode(inputString), StandardCharsets.UTF_8);
					}
					valid &= isValidForPattern(inputString, parameter.pattern);
				}
				break;
			case none:
				valid &= true;
				break;
		}

		if (parameter.maxlength != null) {
			valid &= ((parameter.maxlength >= inputString.length()) ? true : false);
		}

		return valid;
	}

	/*
	* Validate Parameters of mode fixed
	* Input can be Array or String
	*/
	@Timed(name = "InputValidation", description = "A measure of how long it takes to perform the validation of the input.")
	@Override
	public boolean isValid(Object input, FixedValueParameter parameter) {
		boolean valid = true;
		
		switch(parameter.validation) {
			case oneof:
				if (input.getClass().isArray()) {
					JSONArray inputArray = ((JSONArray) input);
					valid &= (inputArray.length() == 1) ? true : false;
				} else if (input instanceof String) {
					valid &= true;
				} else {
					valid &= false;
				}
				break;
			case minone:
				JSONArray inputArray = ((JSONArray) input);
				valid &= (inputArray.length() > 0) ? true : false;
				break;
			case anyof:
				valid &= true;
				break;
		}

		return valid;
	}

	/**
	 * Check if String matches Pattern 
	 * @param input String
	 * @param pattern Regex-Pattern to check String with
	 * @return
	 */
	private boolean isValidForPattern(String input, String pattern) {
		return Pattern.matches(pattern, input);
	}

	/**
	 * Check if input is in range and if it is divisible by step
	 * @param input check for this number if it is valid
	 * @param min minimum
	 * @param max maximum
	 * @param step stepsize - input has to be divisible by step
	 * @return
	 */
	private boolean isValidForNumbers(Double input, Double min, Double max, Double step) {
		boolean valid = true;
		if (min != null) {
			valid &= ((min <= input) ? true : false);
		}
		if (max != null) {
			valid &= ((max >= input) ? true : false);
		}
		if (step != null) {
			valid &= (((input - min) % step == 0) ? true : false);
		}
		return valid;
	}
}
