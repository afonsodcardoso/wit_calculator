package com.wit.calculator.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class BigDecimalCalculator {

  private final int scale;
  private final RoundingMode rounding;

  public BigDecimalCalculator(
      @org.springframework.beans.factory.annotation.Value("${calculator.scale:16}") int scale,
      @org.springframework.beans.factory.annotation.Value("${calculator.rounding:HALF_UP}") RoundingMode rounding) {
    this.scale = scale;
    this.rounding = rounding;
  }

  public BigDecimal compute(String op, String a, String b) {
    BigDecimal A = new BigDecimal(a);
    BigDecimal B = new BigDecimal(b);
    return switch (op) {
      case "sum" -> A.add(B);
      case "subtraction" -> A.subtract(B);
      case "multiplication" -> A.multiply(B);
      case "division" -> {
        if (B.compareTo(BigDecimal.ZERO) == 0) throw new ArithmeticException("Division by zero");
        yield A.divide(B, scale, rounding); // define scale/rounding to avoid ArithmeticException on non-terminating decimals
      }
      default -> throw new IllegalArgumentException("Unsupported op: " + op);
    };
  }
}
