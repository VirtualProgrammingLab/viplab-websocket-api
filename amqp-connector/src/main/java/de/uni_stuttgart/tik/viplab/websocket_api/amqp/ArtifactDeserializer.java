package de.uni_stuttgart.tik.viplab.websocket_api.amqp;

import java.lang.reflect.Type;

import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult.Artifact;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult.File;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult.Notifications;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult.S3File;;

public class ArtifactDeserializer implements JsonbDeserializer<Artifact> {

	private static final Jsonb jsonb = JsonbBuilder.create();

	@Override
	public Artifact deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
		JsonObject jsonObj = parser.getObject();
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
