package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import de.uni_stuttgart.tik.viplab.websocket_api.validation.Environment;

@ApplicationScoped
public class ConfigurationTemplateRendererManagerImpl implements ConfigurationTemplateRendererManager {

  @Inject
  TemplateRenderer templateRenderer;

  private final Map<String, ConfigurationTemplateRenderer> configurationTemplateRenderers = new HashMap<>();

  @Inject
  Logger logger;

  @PostConstruct
  void setup() {
    ServiceLoader<ConfigurationTemplateRenderer> serviceLoader = ServiceLoader
            .load(ConfigurationTemplateRenderer.class);
    for (ConfigurationTemplateRenderer configurationTemplateRenderer : serviceLoader) {
      String environment = getConfigurationTemplateRendererEnvironment(configurationTemplateRenderer);
      if (configurationTemplateRenderers.containsKey(environment)) {
        throw new IllegalStateException("Multiple Configuration Template Renderer for the environment:" + environment);
      }
      logger.info("Adding ConfiguartionTemplateRender for {}",
              environment);
      configurationTemplateRenderers.put(environment,
              configurationTemplateRenderer);
    }
  }

  private String getConfigurationTemplateRendererEnvironment(
          ConfigurationTemplateRenderer configurationTemplateRenderer) {
    if (!configurationTemplateRenderer.getClass()
            .isAnnotationPresent(Environment.class)) {
      throw new IllegalArgumentException("The Configuration Template Renderer has no Environment specified: "
              + configurationTemplateRenderer.getClass()
                      .getName());
    }
    return configurationTemplateRenderer.getClass()
            .getAnnotation(Environment.class)
            .value();
  }

  @Override
  public Map<String, Object> render(Map<String, Object> configuration, Map<String, Object> arguments,
          String environment) {
    ConfigurationTemplateRenderer configurationTemplateRenderer = configurationTemplateRenderers.get(environment);
    if (configurationTemplateRenderer == null) {
      return configuration;
    }
    return configurationTemplateRenderer.render(configuration,
            arguments,
            templateRenderer);
  }
}
