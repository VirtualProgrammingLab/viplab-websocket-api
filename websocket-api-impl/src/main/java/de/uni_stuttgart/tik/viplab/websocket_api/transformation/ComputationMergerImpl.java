package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.uni_stuttgart.tik.viplab.websocket_api.model.Computation;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate.File;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate.File.Part;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate.Parameter;
import de.uni_stuttgart.tik.viplab.websocket_api.validation.ConfigurationValidatorManager;
import de.uni_stuttgart.tik.viplab.websocket_api.validation.InputValidator;

@ApplicationScoped
public class ComputationMergerImpl implements ComputationMerger {

	@Inject
	private InputValidator inputValidator;

	@Inject
	private ConfigurationValidatorManager configurationValidatorManager;
	@Inject
	private ConfigurationTemplateRendererManager configurationTemplateRendererManager;

	private final MustacheFactory mf = new DefaultMustacheFactory();

	@Override
	public Computation merge(ComputationTemplate template, ComputationTask task) {
		Computation computation = new Computation();
		computation.identifier = UUID.randomUUID().toString();

		computation.environment = template.environment;

		Map<String, String> arguments = getArguments(template.parameters, task.arguments);
		computation.configuration = getConfiguration(computation.environment, template.configuration, arguments);

		computation.files = mergeFileList(template, task);

		// TODO validate
		return computation;
	}

	private Map<String, String> getArguments(Map<String, Parameter> parameters, Map<String, String> arguments) {
		parameters.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> {
			String parameterName = e.getKey();
			Parameter parameter = e.getValue();
			if (!arguments.containsKey(parameterName)) {
				throw new IllegalArgumentException("Argument missing: " + parameterName);
			}
			String argument = arguments.get(parameterName);

			if (!inputValidator.isValid(argument, parameter.check)) {
				throw new IllegalArgumentException("Argument not valid: " + parameterName);
			}

			return argument;
		}));

		return arguments;

	}

	/**
	 * Replace parameters in configuration with the given arguments and validate
	 * the configuration for the given environment.
	 * 
	 * @param environment
	 * @param configuration
	 * @param arguments
	 * @return
	 */
	private Map<String, Object> getConfiguration(String environment, Map<String, Object> configurationTemplate,
			Map<String, String> arguments) {
		Map<String, Object> configuration = configurationTemplateRendererManager.render(configurationTemplate,
				arguments, environment);

		if (!configurationValidatorManager.isValid(configuration, environment)) {
			throw new IllegalArgumentException(
					"The ComputationTemplate configuration is not valid for the environment of the ComputationTemplate.");
		}

		return configuration;
	}

	private List<File> mergeFileList(ComputationTemplate template, ComputationTask task) {
		Map<String, File> filesFromTemplate = createFileIndex(template.files);
		Map<String, File> filesFromTask = createFileIndex(task.files);
		List<File> files = new ArrayList<>();
		for (File fileFromTemplate : filesFromTemplate.values()) {
			if (filesFromTask.containsKey(fileFromTemplate.identifier)) {
				File fileFromTask = filesFromTask.get(fileFromTemplate.identifier);
				files.add(merge(fileFromTemplate, fileFromTask));
			} else {
				File file = new File();
				file.identifier = fileFromTemplate.identifier;
				file.path = fileFromTemplate.path;
				file.parts = fileFromTemplate.parts;
				files.add(file);
			}
		}
		for (File fileFromTask : filesFromTask.values()) {
			if (!filesFromTemplate.containsKey(fileFromTask.identifier)) {
				File file = new File();
				file.identifier = fileFromTask.identifier;
				/**
				 * The uniqueness of file paths can't be checked here because
				 * the target file system and structure is not known, see
				 * java.nio.file.Path#normalize. The path must be checked in the
				 * backend.
				 */
				file.path = fileFromTask.path;
				file.parts = fileFromTask.parts;
				files.add(file);
			}
		}
		return files;
	}

	private File merge(File fileFromTemplate, File fileFromTask) {
		File file = new File();
		Map<String, Part> partsFromTemplate = createPartIndex(fileFromTemplate.parts);
		Map<String, Part> partsFromTask = createPartIndex(fileFromTask.parts);

		List<Part> parts = new ArrayList<>();
		for (Part partFromTemplate : fileFromTemplate.parts) {
			if (partsFromTask.containsKey(partFromTemplate.identifier)) {
				Part partFromTask = partsFromTask.get(partFromTemplate.identifier);
				parts.add(merge(partFromTemplate, partFromTask));
			} else {
				Part part = new Part();
				part.identifier = partFromTemplate.identifier;
				part.content = partFromTemplate.content;
				parts.add(part);
			}
		}
		for (Part partFromTask : fileFromTask.parts) {
			if (!partsFromTemplate.containsKey(partFromTask.identifier)) {
				Part part = new Part();
				part.identifier = partFromTask.identifier;
				part.content = partFromTask.content;
				parts.add(part);
			}
		}
		file.parts = parts;
		return file;
	}

	private Part merge(Part partFromTemplate, Part partFromTask) {
		Part part = new Part();
		part.identifier = partFromTemplate.identifier;
		switch (partFromTemplate.access) {
		case Part.ACCESS_INVISIBLE:
		case Part.ACCESS_VISIBLE:
			part.content = partFromTemplate.content;
			break;
		case Part.ACCESS_MODIFIABLE:
			part.content = partFromTask.content;
			break;
		case Part.ACCESS_TEMPLATE:
			part.content = renderTemplate(partFromTemplate, partFromTask);
			break;
		default:
			throw new IllegalArgumentException(
					"ComputationTemplate has a part with unknown access: " + partFromTemplate.access);
		}
		return part;
	}

	private String renderTemplate(Part partFromTemplate, Part partFromTask) {
		String template = new String(Base64.getUrlDecoder().decode(partFromTemplate.content), StandardCharsets.UTF_8);
		String variablesJson = new String(Base64.getUrlDecoder().decode(partFromTask.content), StandardCharsets.UTF_8);
		JsonObject variables;
		try (JsonReader reader = Json.createReader(new StringReader(variablesJson))) {
			variables = reader.readObject();
		}

		HashMap<String, Object> scope = new HashMap<>();
		variables.forEach((name, valueJson) -> {
			String value = ((JsonString) valueJson).getString();
			scope.put(name, value);
		});

		Mustache mustache = mf.compile(new StringReader(template), "example");
		StringWriter writer = new StringWriter();
		mustache.execute(writer, scope);
		String renderedTemplate = writer.toString();

		return Base64.getUrlEncoder().encodeToString(renderedTemplate.getBytes(StandardCharsets.UTF_8));
	}

	private Map<String, File> createFileIndex(List<File> files) {
		return files.stream().collect(Collectors.toMap(file -> file.identifier, file -> file));
	}

	private Map<String, Part> createPartIndex(List<Part> parts) {
		return parts.stream().collect(Collectors.toMap(part -> part.identifier, part -> part));
	}

}
