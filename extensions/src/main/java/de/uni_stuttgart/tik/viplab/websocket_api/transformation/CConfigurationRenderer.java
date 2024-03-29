package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.util.Map;

import com.google.auto.service.AutoService;

import de.uni_stuttgart.tik.viplab.websocket_api.validation.Environment;

@Environment("C")
@AutoService(ConfigurationTemplateRenderer.class)
public class CConfigurationRenderer implements ConfigurationTemplateRenderer {

  @Override
  public Map<String, Object> render(Map<String, Object> configuration, Map<String, Object> arguments,
          TemplateRenderer templateRenderer) {
    ConfigurationUtil configurationUtil = new ConfigurationUtil(arguments, templateRenderer);
    // example
    configurationUtil.renderPropertyTemplate(configuration,
            "running.commandLineArguments");

    return configuration;
  }

}
