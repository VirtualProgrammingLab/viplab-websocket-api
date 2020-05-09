package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * This TemplateRenderer uses the mustache library to replace parameter in the
 * template string.
 * 
 * @author Leon
 *
 */
@ApplicationScoped
public class TemplateRendererImpl implements TemplateRenderer {

	private final MustacheFactory mf = new DefaultMustacheFactory();

	@Override
	public String renderTemplate(String template, Map<String, String> parameters) {
		Mustache mustache = mf.compile(new StringReader(template), "example");
		StringWriter writer = new StringWriter();
		mustache.execute(writer, parameters);
		return writer.toString();
	}

}
