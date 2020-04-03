package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import de.uni_stuttgart.tik.viplab.websocket_api.validation.Environment;

@ApplicationScoped
public class ConfigurationTemplateRendererManagerImpl implements ConfigurationTemplateRendererManager {

	@Inject
	TemplateRenderer templateRenderer;

	private final Map<String, ConfigurationTemplateRenderer> configurationTemplateRenderers = new HashMap<>();

	@PostConstruct
	void setup() {
		ServiceLoader<ConfigurationTemplateRenderer> serviceLoader = ServiceLoader
				.load(ConfigurationTemplateRenderer.class);
		for (ConfigurationTemplateRenderer configurationTemplateRenderer : serviceLoader) {
			String environment = getConfigurationTemplateRendererEnvironment(configurationTemplateRenderer);
			if (configurationTemplateRenderers.containsKey(environment)) {
				throw new IllegalStateException(
						"Multiple Configuration Template Renderer for the environment:" + environment);
			}
			configurationTemplateRenderers.put(environment, configurationTemplateRenderer);
		}
	}

	private String getConfigurationTemplateRendererEnvironment(
			ConfigurationTemplateRenderer configurationTemplateRenderer) {
		if (!configurationTemplateRenderer.getClass().isAnnotationPresent(Environment.class)) {
			throw new IllegalArgumentException("The Configuration Template Renderer has no Environment specified: "
					+ configurationTemplateRenderer.getClass().getName());
		}
		return configurationTemplateRenderer.getClass().getAnnotation(Environment.class).value();
	}

	@Override
	public Map<String, Object> render(Map<String, Object> configuration, Map<String, String> arguments,
			String environment) {
		ConfigurationTemplateRenderer configurationTemplateRenderer = configurationTemplateRenderers.get(environment);
		if (configurationTemplateRenderer == null) {
			return configuration;
		}
		return configurationTemplateRenderer.render(configuration, arguments, templateRenderer);
	}
}
