package de.uni_stuttgart.tik.viplab.websocket_api.validation;

import de.uni_stuttgart.tik.viplab.websocket_api.model.AnyValueParameter;
import de.uni_stuttgart.tik.viplab.websocket_api.model.FixedValueParameter;

public interface ParameterValidator {
	boolean isValid(Object input, AnyValueParameter parameter);
	boolean isValid(Object input, FixedValueParameter parameter);
}
