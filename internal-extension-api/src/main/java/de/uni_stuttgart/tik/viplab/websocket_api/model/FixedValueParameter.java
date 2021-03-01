package de.uni_stuttgart.tik.viplab.websocket_api.model;

import java.util.Collections;
import java.util.List;

public class FixedValueParameter extends Parameter {

  public boolean multiple = false;

  public List<Value> values = Collections.emptyList();

  public static class Value {
    public String value;
    public String text;
    public boolean selected;
    public boolean disabled;
  }
}
