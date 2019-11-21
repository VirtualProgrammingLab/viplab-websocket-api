package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import java.net.URI;

import javax.enterprise.context.Dependent;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;

@Dependent
public class ECSMessagesConverter {

	public Exercise convertComputationTemplateToExercise(ComputationTemplate template) {
		return new Exercise();
	}

	public Solution convertComputationTaskToSolution(ComputationTask task, URI exerciseURL) {
		Solution solution = new Solution();
		solution.exercise = exerciseURL;

		return solution;
	}

}
