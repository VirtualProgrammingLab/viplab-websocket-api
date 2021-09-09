package de.uni_stuttgart.tik.viplab.websocket_api.amqp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class MemoryConfigSource implements ConfigSource {

  private static Map<String, String> entries = new HashMap<>();

  public MemoryConfigSource() {
  }

  @Override
  public Map<String, String> getProperties() {

    return entries;
  }

  @Override
  public String getValue(String propertyName) {
    return entries.get(propertyName);
  }

  @Override
  public String getName() {
    return "Dummy";
  }

  @Override
  public int getOrdinal() {
    return 900;
  }

  public static void setMapEntry(String key, String value) {
    entries.put(key,
            value);
  }

  @Override
  public Set<String> getPropertyNames() {
    return entries.keySet();
  }
}
