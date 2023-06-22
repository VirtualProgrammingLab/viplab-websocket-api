package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.uni_stuttgart.tik.viplab.websocket_api.jsontransformers.ParameterDeserializer;
import de.uni_stuttgart.tik.viplab.websocket_api.model.Computation;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import io.quarkus.test.junit.QuarkusTest;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

@QuarkusTest
class ComputationMergerImplTest {

	@Inject
	ComputationMerger sut;

	private Jsonb jsonb = JsonbBuilder.create();

	@ParameterizedTest
	@MethodSource("exampleJsonProvider")
	void testMerge(String taskJson, String templateJson, String computationJson) {
		
		JsonbConfig config = new JsonbConfig().withDeserializers(new ParameterDeserializer());
		jsonb = JsonbBuilder.create(config);
		ComputationTemplate template = jsonb.fromJson(templateJson, ComputationTemplate.class);
		
		ComputationTask task = jsonb.fromJson(taskJson, ComputationTask.class);

		Computation actualComputation = sut.merge(template, task);

		assertNotNull(actualComputation.identifier);
		actualComputation.identifier = "5032ad45-5d16-45b5-8bf7-44fc60b12edf";
		String actualSolutionJson = jsonb.toJson(actualComputation);
		assertThat(actualSolutionJson, SameJSONAs.sameJSONAs(computationJson));
	}

	private static String loadFile(String fileName) {
		try {
			return new String(ComputationMergerImplTest.class.getResourceAsStream(fileName).readAllBytes(),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalArgumentException(fileName, e);
		}
	}

	private static Stream<Arguments> exampleJsonProvider() {
		return Stream.of("minimal", "simple", "complex", "params").map(fileName -> {
			return Arguments.of(loadFile(fileName + ".computation-task.json"),
					loadFile(fileName + ".computation-template.json"), loadFile(fileName + ".computation.json"));
		});
	}
}
