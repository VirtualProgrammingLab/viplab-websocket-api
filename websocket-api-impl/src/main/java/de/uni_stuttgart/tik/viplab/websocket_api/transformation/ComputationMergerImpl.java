package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.slf4j.Logger;

import de.uni_stuttgart.tik.viplab.websocket_api.model.Computation;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate.File;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate.File.Part;
import de.uni_stuttgart.tik.viplab.websocket_api.model.FixedValueParameter;
import de.uni_stuttgart.tik.viplab.websocket_api.model.FixedValueParameter.Option;
import de.uni_stuttgart.tik.viplab.websocket_api.model.Parameter;
import de.uni_stuttgart.tik.viplab.websocket_api.validation.ConfigurationValidatorManager;
import de.uni_stuttgart.tik.viplab.websocket_api.validation.ParameterValidator;
import io.quarkus.logging.Log;

@ApplicationScoped
public class ComputationMergerImpl implements ComputationMerger {

  @Inject
  ParameterValidator parameterValidator;
  @Inject
  ConfigurationValidatorManager configurationValidatorManager;
  @Inject
  ConfigurationTemplateRendererManager configurationTemplateRendererManager;
  @Inject
  TemplateRenderer templateRenderer;
  @Inject
  Logger logger;

  @Override
  public Computation merge(ComputationTemplate template, ComputationTask task) {
    Computation computation = new Computation();
    computation.identifier = UUID.randomUUID()
            .toString();

    computation.environment = template.environment;

    Map<String, String> arguments = getArguments(template.parameters,
            task.arguments);
    computation.configuration = getConfiguration(computation.environment,
            template.configuration,
            arguments);

    computation.files = mergeFileList(template,
            task);

    // TODO validate
    return computation;
  }

  private Map<String, String> getArguments(List<FixedValueParameter> parameters, Map<String, String> arguments) {
    return parameters.stream()
            .collect(Collectors.toMap(Parameter::getIdentifier,
                    param -> {
                      String parameterName = param.getIdentifier();
                      String argument = null;
                      if (null == arguments || !arguments.containsKey(parameterName)) {
                        // In case no arguments where supplied in the task, check if
                        // the template provides a selected value, otherwise throw exception

                        for (Option option : param.options) {
                          if (option.selected) {
                            argument = option.value;
                            break;
                          }
                        }
                        if (null == argument) {
                          throw new IllegalArgumentException("Argument missing and no default: " + parameterName);
                        }
                      } else {
                        argument = arguments.get(parameterName);
                      }

                      if (!parameterValidator.isValid(argument,
                              param)) {
                        throw new IllegalArgumentException("Argument not valid: " + parameterName);
                      }
                      return argument;
                    }));
  }

  /**
   * Replace parameters in configuration with the given arguments and validate the configuration for the given
   * environment.
   * 
   * @param environment
   * @param configuration
   * @param arguments
   * @return
   */
  private Map<String, Object> getConfiguration(String environment, Map<String, Object> configurationTemplate,
          Map<String, String> arguments) {
    Map<String, Object> configuration = configurationTemplateRendererManager.render(configurationTemplate,
            arguments,
            environment);

    if (!configurationValidatorManager.isValid(configuration,
            environment)) {
      throw new IllegalArgumentException(
              "The ComputationTemplate configuration is not valid for the environment of the ComputationTemplate.");
    }

    return configuration;
  }

  private List<File> mergeFileList(ComputationTemplate template, ComputationTask task) {
    Map<String, ComputationTask.File> filesFromTask = createFileIndex(task.files);
    List<File> files = new ArrayList<>();
    for (File fileFromTemplate : template.files) {
      if (filesFromTask.containsKey(fileFromTemplate.identifier)) {
        ComputationTask.File fileFromTask = filesFromTask.get(fileFromTemplate.identifier);
        files.add(merge(fileFromTemplate,
                fileFromTask));
      } else {
        File file = new File();
        file.identifier = fileFromTemplate.identifier;
        file.path = fileFromTemplate.path;
        file.parts = fileFromTemplate.parts;
        files.add(file);
      }
    }
    return files;
  }

  private File merge(File fileFromTemplate, ComputationTask.File fileFromTask) {
    File file = new File();
    file.identifier = fileFromTemplate.identifier;
    file.path = fileFromTemplate.path;

    Map<String, ComputationTask.File.Part> partsFromTask = createPartIndex(fileFromTask.parts);
    List<Part> parts = new ArrayList<>();
    for (Part partFromTemplate : fileFromTemplate.parts) {
      if (partsFromTask.containsKey(partFromTemplate.identifier)) {
        ComputationTask.File.Part partFromTask = partsFromTask.get(partFromTemplate.identifier);
        parts.add(merge(partFromTemplate,
                partFromTask));
      } else {
        Part part = new Part();
        part.identifier = partFromTemplate.identifier;
        part.access = partFromTemplate.access;
        part.content = partFromTemplate.content;
        parts.add(part);
      }
    }
    file.parts = parts;
    return file;
  }

  private Part merge(Part partFromTemplate, ComputationTask.File.Part partFromTask) {
    Part part = new Part();
    part.identifier = partFromTemplate.identifier;
    part.access = partFromTemplate.access;
    switch (partFromTemplate.access) {
      case Part.ACCESS_INVISIBLE:
      case Part.ACCESS_VISIBLE:
        throw new IllegalArgumentException(
                "ComputationTask is not allowed to override a part with access: " + partFromTemplate.access);
      case Part.ACCESS_MODIFIABLE:
        part.content = partFromTask.content;
        break;
      case Part.ACCESS_TEMPLATE:
        part.content = renderTemplate(partFromTemplate,
                partFromTask);
        break;
      default:
        throw new IllegalArgumentException(
                "ComputationTemplate has a part with unknown access: " + partFromTemplate.access);
    }
    return part;
  }

  private String renderTemplate(Part partFromTemplate, ComputationTask.File.Part partFromTask) {
    String template = new String(Base64.getUrlDecoder()
            .decode(partFromTemplate.content), StandardCharsets.UTF_8);
    String variablesJson = new String(Base64.getUrlDecoder()
            .decode(partFromTask.content), StandardCharsets.UTF_8);
    JsonObject variables;
    try (JsonReader reader = Json.createReader(new StringReader(variablesJson))) {
      variables = reader.readObject();
    }

    HashMap<String, String> parameters = new HashMap<>();
    partFromTemplate.parameters.forEach(parameter -> {
      String value = ((JsonString) variables.get(parameter.getIdentifier())).getString();
      if (!parameterValidator.isValid(value,
              parameter)) {
        throw new IllegalArgumentException("Argument not valid: " + parameter.getIdentifier());
      }
      if (value.startsWith("base64:")) {
    	  value = value.replaceFirst("^base64:", "");
    	  value = new String(Base64.getUrlDecoder()
    	            .decode(value), StandardCharsets.UTF_8);
    	  Log.debug("----------" + value + "----------");
      }
      parameters.put(parameter.getIdentifier(),
              value);
    });

    String renderedTemplate = templateRenderer.renderTemplate(template,
            parameters);
    Log.debug(renderedTemplate);
    Log.debug("----------" + Base64.getUrlEncoder().encodeToString(renderedTemplate.getBytes(StandardCharsets.UTF_8)) + "----------");
    return Base64.getUrlEncoder()
            .encodeToString(renderedTemplate.getBytes(StandardCharsets.UTF_8));
  }

  private Map<String, ComputationTask.File> createFileIndex(List<ComputationTask.File> files) {
    return files.stream()
            .collect(Collectors.toMap(file -> file.identifier,
                    file -> file));
  }

  private Map<String, ComputationTask.File.Part> createPartIndex(List<ComputationTask.File.Part> parts) {
    return parts.stream()
            .collect(Collectors.toMap(part -> part.identifier,
                    part -> part));
  }

}
