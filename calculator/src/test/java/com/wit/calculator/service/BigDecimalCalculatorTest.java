package com.wit.calculator.service;

import org.junit.jupiter.api.Test;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.*;

class BigDecimalCalculatorTest {

  @Test
  void adds() {
    var svc = new BigDecimalCalculator(16, RoundingMode.HALF_UP);
    assertEquals("3", svc.compute("sum","1","2").toPlainString());
  }

  @Test
  void subs() {
    var svc = new BigDecimalCalculator(16, RoundingMode.HALF_UP);
    assertEquals("8", svc.compute("subtraction","10","2").toPlainString());
  }

  @Test
  void mults() {
    var svc = new BigDecimalCalculator(16, RoundingMode.HALF_UP);
    assertEquals("20", svc.compute("multiplication","10","2").toPlainString());
  }

  @Test
  void dividesWithScale() {
    var svc = new BigDecimalCalculator(4, RoundingMode.HALF_UP);
    assertEquals("3.3333", svc.compute("division","10","3").toPlainString());
  }
}
