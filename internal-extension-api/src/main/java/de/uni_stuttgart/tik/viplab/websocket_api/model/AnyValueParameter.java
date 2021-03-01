package de.uni_stuttgart.tik.viplab.websocket_api.model;

public class AnyValueParameter extends Parameter {

  enum Validator {
    pattern, range, steprange
  }

  public Object value;

  public double min;
  public double max;
  public double step;
  public Validator validation;
  public String pattern;

}
