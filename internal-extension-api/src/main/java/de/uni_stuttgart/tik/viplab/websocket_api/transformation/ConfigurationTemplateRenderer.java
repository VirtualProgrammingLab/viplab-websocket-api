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
	/**
	 * Replace template string in the Computation Template configuration with
	 * the actual values from the Computation Task arguments. Use the given
	 * {@link TemplateRenderer} to inject the arguments into configuration
	 * properties.
	 * 
	 * @param configuration
	 *            the configuration defined by the Computation Template
	 * @param arguments
	 *            the valid arguments which should be injected into the
	 *            configuration
	 * @param templateRenderer
	 *            the default template renderer used to replace parameter in the
	 *            template string with the argument value
	 * @return the configuration where the arguments were injected
	 */
	public Map<String, Object> render(Map<String, Object> configuration, Map<String, Object> arguments,
			TemplateRenderer templateRenderer);
}
