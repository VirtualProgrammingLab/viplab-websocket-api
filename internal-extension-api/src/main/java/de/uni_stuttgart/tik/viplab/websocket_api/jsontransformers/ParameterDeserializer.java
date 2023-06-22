package de.uni_stuttgart.tik.viplab.websocket_api.jsontransformers;

import java.lang.reflect.Type;

import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;

import de.uni_stuttgart.tik.viplab.websocket_api.model.AnyValueParameter;
import de.uni_stuttgart.tik.viplab.websocket_api.model.FixedValueParameter;
import de.uni_stuttgart.tik.viplab.websocket_api.model.Parameter;

public class ParameterDeserializer implements JsonbDeserializer<Parameter> {

    private static final Jsonb jsonb = JsonbBuilder.create();

    @Override
    public Parameter deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        JsonObject jsonObj = parser.getObject();
		String jsonString = jsonObj.toString();
		String mode = jsonObj.getString("mode");

		switch (mode) {
			case "any":
				return jsonb.fromJson(jsonString, AnyValueParameter.class);
			case "fixed":
				return jsonb.fromJson(jsonString, FixedValueParameter.class);
			default:
				throw new JsonbException("Unknown mode: " + mode);
		}
    }

    
    
}