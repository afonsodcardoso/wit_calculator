package com.wit.calculator.model;

public record CalculatorResponse(String result, String error) { // We keep numbers as String to avoid float/double precision issues -> the calculator will parse to BigDecimal
  public boolean ok() { return error == null; }
}
