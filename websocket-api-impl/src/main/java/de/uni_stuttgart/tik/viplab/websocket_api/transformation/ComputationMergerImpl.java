package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.uni_stuttgart.tik.viplab.websocket_api.model.Computation;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate.File;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate.File.Part;

@ApplicationScoped
public class ComputationMergerImpl implements ComputationMerger {

	private final MustacheFactory mf = new DefaultMustacheFactory();

	@Override
	public Computation merge(ComputationTemplate template, ComputationTask task) {
		Computation computation = new Computation();
		computation.identifier = UUID.randomUUID().toString();

		computation.environment = template.environment;
		// TODO replace parameters in configuration
		computation.configuration = template.configuration;

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
				file.path = fileFromTask.path;
				file.parts = fileFromTask.parts;
				files.add(file);
			}
		}
		computation.files = files;

		// TODO validate
		return computation;
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
		JsonObject variables = Json.createReader(new StringReader(variablesJson)).readObject();

		HashMap<String, Object> scope = new HashMap<String, Object>();
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
