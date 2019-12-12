package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import org.junit.jupiter.api.Test;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;

class ECSMessagesConverterTest {

	private static ECSMessagesConverter sut = new ECSMessagesConverter();

	@Test
	void test() {
		ComputationTemplate computationTemplate = new ComputationTemplate();
		computationTemplate.environment = "Java";
		Exercise exercise = sut.convertComputationTemplateToExercise(computationTemplate);
		assertThat(exercise.config, hasKey("Java"));
	}

}
