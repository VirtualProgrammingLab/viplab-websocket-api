package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import java.net.URI;
import java.util.List;

import de.uni_stuttgart.tik.viplab.websocket_api.ecs.Exercise.Element;

public class Solution {

	public String postTime;
	public String ID;
	public EvaluationService evaluationService;
	public String comment;
	public URI exercise;
	public ExerciseModifications exerciseModifications;

	public static class EvaluationService {
		public String jobID;
		public String jobSender;
	}
	
	public static class ExerciseModifications {
		public List<Element> elements;
	}
}
