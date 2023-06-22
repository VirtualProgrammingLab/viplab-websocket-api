package de.uni_stuttgart.tik.viplab.websocket_api.amqp;

import java.lang.reflect.Type;


import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult.Artifact;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult.File;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult.Notifications;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult.S3File;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;

public class ArtifactDeserializer implements JsonbDeserializer<Artifact> {
	private static final Jsonb jsonb = JsonbBuilder.create();
	@Override
	public Artifact deserialize(jakarta.json.stream.JsonParser jsonParser, DeserializationContext deserializationContext, Type t) {
		JsonObject jsonObj = jsonParser.getObject();
		String jsonString = jsonObj.toString();
		String type = jsonObj.getString("type");

		switch (type) {
		case "file":
			return jsonb.fromJson(jsonString, File.class);
		case "notifications":
			return jsonb.fromJson(jsonString, Notifications.class);
		case "s3file":
		  return jsonb.fromJson(jsonString, S3File.class);
		default:
			throw new JsonbException("Unknown type: " + type);
		}
	}


}
