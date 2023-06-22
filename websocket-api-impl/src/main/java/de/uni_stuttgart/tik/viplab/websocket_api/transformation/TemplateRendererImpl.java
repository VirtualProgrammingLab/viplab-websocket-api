package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.io.IOException;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import io.quarkus.logging.Log;

/**
 * This TemplateRenderer uses the mustache library to replace parameter in the
 * template string.
 * 
 * @author Leon
 *
 */
@ApplicationScoped
public class TemplateRendererImpl implements TemplateRenderer {

	@Override
	public String renderTemplate(String template, Map<String, Object> parameters) {
		
		Handlebars handlebars = new Handlebars();
		Template compiledTemplate;
		try {
			compiledTemplate = handlebars.compileInline(template);
			Log.debug("---------- Handlebars Template: " + template + "----------");
			Log.debug("---------- Handlebars Input: " + parameters + "----------");
			String templateString = compiledTemplate.apply(parameters);
			Log.debug("---------- Handlebars Result: " + templateString + "----------");
			return templateString;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
		
	}
	
	

}