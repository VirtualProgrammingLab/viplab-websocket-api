package de.uni_stuttgart.tik.viplab.websocket_api.model;

import javax.json.bind.annotation.JsonbProperty;

public class AnyValueParameter extends Parameter {

  public enum Validator {
    pattern, range, none
  }

  public Metadata metadata;
  @JsonbProperty(value = "default")
  public Object[] defaults;

  public Double min;
  public Double max;
  public Double step;
  public Integer maxlength;
  public Validator validation;
  public String pattern;

  public static class Metadata {
    enum GuiType {
      editor, input_field, slider
    }

    enum Type {
      number, text
    }

    public GuiType guiType;
    public Type type;
    public String name;
    public boolean vertical;
  }
}
