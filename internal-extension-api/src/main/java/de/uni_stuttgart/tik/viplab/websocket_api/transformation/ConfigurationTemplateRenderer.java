package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.util.Map;

import de.uni_stuttgart.tik.viplab.websocket_api.validation.Environment;
/**
 * Implementation of this interface must be registered in the ServiceLoader via
 * the
 * <code>META-INF/services/de.uni_stuttgart.tik.viplab.websocket_api.transformation.ConfigurationTemplateRenderer</code>
 * file. Implementations must also be annotated with {@link Environment}.
 * 
 * @author Leon
 */
public interface ConfigurationTemplateRenderer {
	public Map<String, Object> render(Map<String, Object> configuration, Map<String, String> arguments);
}
