package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.util.Map;

public interface TemplateRenderer {
	/**
	 * Replace all the placeholder in the template string with the parameter
	 * values. The format of the template string depends on the implementation
	 * of the TemplateRenderer.
	 * 
	 * @param template
	 *            the template string containing placeholder for parameter
	 *            values
	 * @param parameters
	 *            map of parameter names to parameter values
	 * @return
	 */
	public String renderTemplate(String template, Map<String, String> parameters);
}
