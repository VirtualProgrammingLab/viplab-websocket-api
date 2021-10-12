package de.uni_stuttgart.tik.viplab.websocket_api.model;

import java.util.Collections;
import java.util.List;

public class FixedValueParameter extends Parameter {

  enum Validator {
    onlyone, minone, any
  }

  public Metadata metadata;

  public Validator validation;

  public List<Option> options = Collections.emptyList();

  public static class Option {
    public String value;
    public String text;
    public boolean selected;
    public boolean disabled;
  }

  public static class Metadata {
    enum GuiType {
      checkbox, radio, dropdown, toggle, slider
    }

    public GuiType guiType;
    public String name;
  }
}
