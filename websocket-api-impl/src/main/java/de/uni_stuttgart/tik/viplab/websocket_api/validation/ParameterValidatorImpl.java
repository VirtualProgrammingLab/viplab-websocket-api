package de.uni_stuttgart.tik.viplab.websocket_api.validation;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.annotation.Timed;
import org.json.JSONArray;

import de.uni_stuttgart.tik.viplab.websocket_api.model.AnyValueParameter;
import de.uni_stuttgart.tik.viplab.websocket_api.model.FixedValueParameter;
import de.uni_stuttgart.tik.viplab.websocket_api.model.FixedValueParameter.Option;

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
		
		String inputString = null;
		JSONArray inputArray = null;
		if (input instanceof String) {
			inputString = input.toString();
		} else if (input.getClass().isArray()) {
			inputArray = ((JSONArray) input);
		}

		switch(parameter.validation) {
			case oneof:
				if (inputArray != null) {
					inputString = inputArray.getString(0);
					valid &= (inputArray.length() == 1) ? true : false;
				}
				if (inputString != null) {
					valid &= isValidForOptions(inputString, parameter.options);
				} else {
					valid &= false;
				}
				break;
			case minone:
				if (inputArray != null) {
					valid &= (inputArray.length() > 0) ? true : false;
					for (int i = 0; i < inputArray.length(); i++) {
						String val = inputArray.getString(i);
						valid &= isValidForOptions(val, parameter.options);
					}
				}
				break;
			case anyof:
				valid &= true;
				break;
		}

		return valid;
	}

	/**
	 * Check if input matches one of the options
	 * @param input string to be checked
	 * @param options list of valid strings
	 * @return true if input matches, false otherwise
	 */
	private boolean isValidForOptions(String input, List<Option> options) {
		boolean valid = false;
		for (int i = 0; i < options.size(); i++) {
			if (input.equals(options.get(i).value)) {
				valid = true;
			}
		}
		return valid;
	}

	/**
	 * Check if String matches Pattern 
	 * @param input string to be checked
	 * @param pattern regex-Pattern to check String with
	 * @return true if pattern matches, false otherwise
	 */
	private boolean isValidForPattern(String input, String pattern) {
		return Pattern.matches(pattern, input);
	}

	/**
	 * Check if input is in range and if it is divisible by step
	 * @param input number to be checked
	 * @param min minimum of the range
	 * @param max maximum of the range
	 * @param step stepsize inside the range
	 * @return true if conditions fullfilled, false otherwise
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
			double epsilon = (step > 1) ? 0.000001d : 0.000001d * step;
			valid &= ((((input - min) % step) < epsilon) ? true : false);
		}
		return valid;
	}
}
