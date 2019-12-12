package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;

import de.uni_stuttgart.tik.viplab.websocket_api.ecs.Exercise.Element;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate.File;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate.File.Part;

@Dependent
public class ECSMessagesConverter {

	public Exercise convertComputationTemplateToExercise(ComputationTemplate template) {
		try {
			Exercise exercise = new Exercise();
			exercise.identifier = UUID.randomUUID().toString();
			exercise.postTime = ZonedDateTime.now(ZoneId.of("UTC")).toString();
			exercise.elements = template.files.stream().flatMap(this::fileToElements).collect(Collectors.toList());
			exercise.elementMap = template.files.stream()
					.collect(Collectors.toMap(file -> file.identifier, file -> URI.create("file://" + file.path)));
			exercise.config = Collections.singletonMap(template.environment, getConfiguration(template));
			return exercise;
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(
					"The given ComputationTemplate is not valid and can't be converted to a Numlab Exercise.", e);
		}
	}

	private Stream<Element> fileToElements(File file) {

		return file.parts.stream().map(part -> {
			Element element = new Element();
			element.group = file.path;
			element.visible = part.access.equals("visible") || part.access.equals("modifiable");
			element.modifiable = part.access.equals("modifiable");
			element.identifier = part.identifier;
			element.value = part.content;
			element.MIMEtype = "text/plain";
			return element;
		});
	}

	public Solution convertComputationTaskToSolution(ComputationTask task, URI exerciseURL) {
		try {
			Solution solution = new Solution();
			solution.exercise = exerciseURL;
			solution.ID = UUID.randomUUID().toString();

			return solution;
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(
					"The given ComputationTemplate is not valid and can't be converted to a Numlab Exercise.", e);
		}
	}

	private Map<String, Object> getConfiguration(ComputationTemplate template) {
		switch (template.environment) {
		case "Octave":
			return octaveConfig(template);
		case "Matlab":
			return matlabConfig(template);
		case "C":
			return cConfig(template);
		case "C++":
			return cPlusPlusConfig(template);
		case "Java":
			return javaConfig(template);
		case "DuMuX":
			return duMuXConfig(template);
		default:
			throw new IllegalArgumentException("The environment " + template.environment + " is not supported.");
		}
	}

	private Map<String, Object> octaveConfig(ComputationTemplate template) {
		return matlabConfig(template);
	}

	private Map<String, Object> matlabConfig(ComputationTemplate template) {
		if (template.files.size() != 1) {
			throw new IllegalArgumentException("The language configuration only allow one file.");
		}
		List<Part> parts = template.files.get(0).parts;
		HashMap<String, Object> config = new HashMap<>();
		HashMap<String, Object> merging = new HashMap<>();
		merging.put("sources", parts.stream().map(part -> part.identifier).collect(Collectors.toList()));
		config.put("merging", merging);
		HashMap<String, Object> checking = new HashMap<>();
		checking.put("sources", parts.stream().filter(part -> part.access.equals("modifiable"))
				.map(part -> part.identifier).collect(Collectors.toList()));
		checking.put("allowedCalls", template.configuration.get("allowedCalls"));
		config.put("checking", checking);
		HashMap<String, Object> interpreting = new HashMap<>();
		interpreting.put("timelimitInSeconds", template.configuration.get("timelimitInSeconds"));
		interpreting.put("stopAfterPhase", "interpreting");
		config.put("interpreting", interpreting);
		return config;
	}

	private Map<String, Object> javaConfig(ComputationTemplate template) {
		HashMap<String, Object> config = new HashMap<>();
		config.put("merging", getMerging(template));
		HashMap<String, Object> compiling = new HashMap<>();
		compiling.put("flags", template.configuration.get("compiling.flags"));
		config.put("compiling", compiling);
		HashMap<String, Object> checking = new HashMap<>();
		checking.put("sources",
				template.files.stream().flatMap(file -> file.parts.stream())
						.filter(part -> part.access.equals("modifiable")).map(part -> part.identifier)
						.collect(Collectors.toList()));
		checking.put("forbiddenCalls", template.configuration.get("forbiddenCalls"));
		checking.put("allowedCalls", template.configuration.get("allowedCalls"));
		config.put("checking", checking);
		HashMap<String, Object> running = new HashMap<>();
		running.put("commandLineArguments", template.configuration.get("commandLineArguments"));
		running.put("timelimitInSeconds", template.configuration.get("timelimitInSeconds"));
		running.put("flags", template.configuration.get("running.flags"));
		running.put("mainClass", template.configuration.get("mainClass"));
		config.put("running", running);
		return config;
	}

	private Map<String, Object> cConfig(ComputationTemplate template) {
		HashMap<String, Object> config = new HashMap<>();
		config.put("merging", getMerging(template));
		HashMap<String, Object> compiling = new HashMap<>();
		compiling.put("compiler", template.configuration.get("compiler"));
		compiling.put("flags", template.configuration.get("compiling.flags"));
		config.put("compiling", compiling);
		HashMap<String, Object> checking = new HashMap<>();
		checking.put("sources",
				template.files.stream().flatMap(file -> file.parts.stream())
						.filter(part -> part.access.equals("modifiable")).map(part -> part.identifier)
						.collect(Collectors.toList()));
		checking.put("forbiddenCalls", template.configuration.get("forbiddenCalls"));
		config.put("checking", checking);
		HashMap<String, Object> linking = new HashMap<>();
		linking.put("flags", template.configuration.get("linking.flags"));
		config.put("linking", linking);
		HashMap<String, Object> running = new HashMap<>();
		running.put("commandLineArguments", template.configuration.get("commandLineArguments"));
		running.put("timelimitInSeconds", template.configuration.get("timelimitInSeconds"));
		config.put("running", running);
		return config;
	}

	private Map<String, Object> cPlusPlusConfig(ComputationTemplate template) {
		HashMap<String, Object> config = new HashMap<>();
		config.put("merging", getMerging(template));
		HashMap<String, Object> compiling = new HashMap<>();
		compiling.put("compiler", template.configuration.get("compiler"));
		compiling.put("flags", template.configuration.get("compiling.flags"));
		config.put("compiling", compiling);
		HashMap<String, Object> linking = new HashMap<>();
		linking.put("flags", template.configuration.get("linking.flags"));
		config.put("linking", linking);
		HashMap<String, Object> running = new HashMap<>();
		running.put("commandLineArguments", template.configuration.get("commandLineArguments"));
		running.put("timelimitInSeconds", template.configuration.get("timelimitInSeconds"));
		config.put("running", running);
		return config;
	}

	private List<Map<String, Object>> getMerging(ComputationTemplate template) {
		List<Map<String, Object>> merging = new ArrayList<>();
		for (File file : template.files) {
			Map<String, Object> merge = new HashMap<>();
			merge.put("sources", file.parts.stream().map(part -> part.identifier).collect(Collectors.toList()));
			merge.put("mergeID", file.identifier);
		}
		return merging;
	}

	private Map<String, Object> duMuXConfig(ComputationTemplate template) {
		HashMap<String, Object> config = new HashMap<>();
		HashMap<String, Object> running = new HashMap<>();
		running.put("executable", template.configuration.get("executable"));
		running.put("commandLineArguments", template.configuration.get("commandLineArguments"));
		running.put("timelimitInSeconds", template.configuration.get("timelimitInSeconds"));
		running.put("observe_stderr", template.configuration.get("observe_stderr"));
		config.put("running", running);
		return config;
	}

}
