package de.uni_stuttgart.tik.viplab.websocket_api;

import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestJSONMessageProvider {
	public static JSONObject getComputationTemplate(String environment) {
		try {
			JSONObject computationTemplate = new JSONObject();

			computationTemplate.put("identifier", UUID.randomUUID().toString());
			computationTemplate.put("environment", environment);

			JSONArray files = new JSONArray();

			computationTemplate.put("files", files);

			return computationTemplate;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public static JSONObject getComputationTask(JSONObject computationTemplate) {
		try {
			JSONObject computationTask = new JSONObject();
			computationTask.put("identifier", UUID.randomUUID().toString());
			computationTask.put("template", computationTemplate.get("identifier"));
			return computationTask;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param computationTemplateBase64
	 *            the Base64 encoded computation template
	 * @param computationTask
	 *            the computation task
	 * @return
	 */
	public static JSONObject getCreateComputationMessage(String computationTemplateBase64, JSONObject computationTask) {
		try {
			JSONObject createComputationMessage = new JSONObject();
			createComputationMessage.put("type", "create-computation");
			JSONObject composition = new JSONObject();
			composition.put("template", computationTemplateBase64);
			composition.put("task", computationTask);
			createComputationMessage.put("content", composition);
			return createComputationMessage;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param jwt
	 *            the jwt string
	 * @return
	 */
	public static JSONObject getAuthenticationMessage(String jwt) {
		try {
			JSONObject authenticateMessage = new JSONObject();
			authenticateMessage.put("type", "authenticate");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("jwt", jwt);
			authenticateMessage.put("content", jsonObject);
			return authenticateMessage;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
