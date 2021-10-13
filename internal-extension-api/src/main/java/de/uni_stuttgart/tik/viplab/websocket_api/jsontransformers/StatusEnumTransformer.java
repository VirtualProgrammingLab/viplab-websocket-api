package de.uni_stuttgart.tik.viplab.websocket_api.jsontransformers;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult.STATUS;

public class StatusEnumTransformer implements JsonbAdapter<STATUS, JsonValue> {

  @Override
  public STATUS adaptFromJson(JsonValue obj) throws Exception {
    String jsonstring = null;
    if (JsonValue.ValueType.STRING == obj.getValueType()) {
      jsonstring = ((JsonString) obj).getString();
    }
    for (STATUS status : STATUS.values()) {
      if (jsonstring.equals(status.statusString())) {
        return status;
      }
    }
    return null;
  }

  @Override
  public JsonValue adaptToJson(STATUS obj) throws Exception {
    return Json.createValue(obj.statusString());
  }
}
