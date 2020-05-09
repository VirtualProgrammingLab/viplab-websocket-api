package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.util.Map;

public class ConfigurationUtil {
	private final Map<String, String> arguments;
	private final TemplateRenderer templateRenderer;

	public ConfigurationUtil(Map<String, String> arguments, TemplateRenderer templateRenderer) {
		this.arguments = arguments;
		this.templateRenderer = templateRenderer;
	}

	/**
	 * Render a template in a property of a map. The current value of the
	 * property will be used as template if present in the map else nothing
	 * happens. The template will be rendered using the TemplateRenderer and the
	 * the property value is updated with the rendered template.
	 * 
	 * @param map
	 *            The object which contains the property.
	 * @param propertyName
	 *            the name of the property which contains a template string, the
	 *            property doesn't have to exist.
	 */
	public void renderPropertyTemplate(Map<String, Object> map, String propertyName) {
		String template = (String) map.get(propertyName);
		if (template != null) {
			map.put(propertyName, templateRenderer.renderTemplate(template, arguments));
		}
	}
}
