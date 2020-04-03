package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.util.Map;

public class ConfigurationUtil {
	private final Map<String, String> arguments;
	private final TemplateRenderer templateRenderer;
	
	public ConfigurationUtil(Map<String, String> arguments, TemplateRenderer templateRenderer) {
		this.arguments = arguments;
		this.templateRenderer = templateRenderer;
	}

	public void renderPropertyTemplate(Map<String, Object> map, String propertyName) {
		String template = (String) map.get(propertyName);
		if (template != null) {
			map.put(propertyName, templateRenderer.renderTemplate(template, arguments));
		}
	}
}
