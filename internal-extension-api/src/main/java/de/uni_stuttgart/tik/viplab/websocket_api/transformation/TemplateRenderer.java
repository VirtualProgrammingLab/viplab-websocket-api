package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.util.Map;

public interface TemplateRenderer {
	public String renderTemplate(String template, Map<String, String> parameters);
}
