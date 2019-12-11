package de.uni_stuttgart.tik.viplab.websocket;

import de.uni_stuttgart.tik.viplab.websocket_api.ecs.Result;
import de.uni_stuttgart.tik.viplab.websocket_api.ecs.Solution;

public class TestECSJSONProvider {
	public static Result getResult(Solution solution) {
		Result result = new Result();
		result.Solution = solution;
		return result;
	}
}
