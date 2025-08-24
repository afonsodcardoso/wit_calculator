package com.wit.rest.web;

import com.wit.calculator.model.CalculatorResponse;
import com.wit.rest.service.CalculatorClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalculatorController.class)
class CalculatorControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private CalculatorClient client;

  @Test
  void sumEndpoint() throws Exception {
    // Use matchers for ALL parameters; use eq(...) for literal values.
    when(client.calculate(eq("sum"), eq("1"), eq("2"), anyString()))
        .thenReturn(new CalculatorResponse("3", null));

    mvc.perform(get("/sum").param("a", "1").param("b", "2"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Request-Id"))
        .andExpect(jsonPath("$.result").value("3"));
  }

  @Test
  void subtractionEndpoint() throws Exception {
    when(client.calculate(eq("subtraction"), eq("5"), eq("3"), anyString()))
        .thenReturn(new CalculatorResponse("2", null));

    mvc.perform(get("/subtraction").param("a", "5").param("b", "3"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Request-Id"))
        .andExpect(jsonPath("$.result").value("2"));
  }

  @Test
  void multiplicationEndpoint() throws Exception {
    when(client.calculate(eq("multiplication"), eq("4"), eq("3"), anyString()))
        .thenReturn(new CalculatorResponse("12", null));

    mvc.perform(get("/multiplication").param("a", "4").param("b", "3"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Request-Id"))
        .andExpect(jsonPath("$.result").value("12"));
  }

  @Test
  void divisionEndpoint() throws Exception {
    when(client.calculate(eq("division"), eq("10"), eq("2"), anyString()))
        .thenReturn(new CalculatorResponse("5", null));

    mvc.perform(get("/division").param("a", "10").param("b", "2"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Request-Id"))
        .andExpect(jsonPath("$.result").value("5"));
  }

  @Test
  void divisionByZeroError() throws Exception {
    when(client.calculate(eq("division"), eq("10"), eq("0"), anyString()))
        .thenReturn(new CalculatorResponse(null, "Division by zero"));

    mvc.perform(get("/division").param("a", "10").param("b", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(header().exists("X-Request-Id"))
        .andExpect(jsonPath("$.error").value("Division by zero"));
  }
}
