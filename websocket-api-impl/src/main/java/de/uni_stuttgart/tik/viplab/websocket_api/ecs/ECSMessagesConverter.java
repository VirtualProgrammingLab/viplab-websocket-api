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
		Exercise exercise = new Exercise();
		exercise.identifier = UUID.randomUUID().toString();
		exercise.postTime = ZonedDateTime.now(ZoneId.of("UTC")).toString();
		exercise.elements = template.files.stream().flatMap(this::fileToElements).collect(Collectors.toList());
		exercise.config = Collections.singletonMap("C++", cPlusPlusConfig());
		// TODO config
		return exercise;
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
		Solution solution = new Solution();
		solution.exercise = exerciseURL;
		solution.ID = UUID.randomUUID().toString();

		return solution;
	}
	
	private Map<String, Object> cPlusPlusConfig(ComputationTemplate template) {
		HashMap<String, Object> config = new HashMap<>();
		return config;
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
		checking.put("sources", parts.stream().filter(part -> part.access.equals("modifiable")).map(part -> part.identifier).collect(Collectors.toList()));
		checking.put("allowedCalls", template.configuration.get("allowedCalls"));
		config.put("checking", checking);
		HashMap<String, Object> interpreting = new HashMap<>();
		interpreting.put("timelimitInSeconds", template.configuration.get("timelimitInSeconds"));
		config.put("interpreting", interpreting);
		//TODO
		return config;
	}

}
