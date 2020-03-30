package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.util.Map;

public interface ConfigurationTemplateRendererManager {
	public Map<String, Object> render(Map<String, Object> configuration, Map<String, String> arguments, String environment);
}
