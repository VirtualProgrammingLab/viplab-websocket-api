package de.uni_stuttgart.tik.viplab.websocket_api;

import java.util.HashMap;
import java.util.Map;

import de.uni_stuttgart.tik.viplab.websocket_api.ecs.Result;
import de.uni_stuttgart.tik.viplab.websocket_api.ecs.Solution;

public class TestECSJSONProvider {
	public static Map<String, Object> getResult(Solution solution) {
		Map<String, Object> wrapper = new HashMap<>();
		Result result = new Result();
		result.Solution = solution;
		wrapper.put("Result", result);
		return wrapper;
	}
}
