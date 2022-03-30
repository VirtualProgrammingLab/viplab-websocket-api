package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;

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

	private final MustacheFactory mf = new CustomMustacheFactory();
	
	@Override
	public String renderTemplate(String template, Map<String, String> parameters) {
		Mustache mustache = mf.compile(new StringReader(template), "example");
		StringWriter writer = new StringWriter();
		Log.debug("---------- Mustache Input: " + parameters + "----------");
		mustache.execute(writer, parameters);
		return writer.toString();
	}
	
	

}

class CustomMustacheFactory extends DefaultMustacheFactory {

	@Override
	/**
	 * Override encode to disable html encoding
	 */
	public void encode(String value, Writer writer) {
		try {
	        writer.append(value);
	    } catch (IOException e) {
	        throw new MustacheException("Failed to append value: " + value);
	    }
	}
	
}
