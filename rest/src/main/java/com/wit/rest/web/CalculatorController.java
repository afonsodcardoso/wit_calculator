package com.wit.rest.web;

import com.wit.calculator.model.CalculatorResponse;
import com.wit.rest.service.CalculatorClient;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/")
public class CalculatorController {

  private final CalculatorClient client;
  public CalculatorController(CalculatorClient client) { this.client = client; }

  @GetMapping({"sum","subtraction","multiplication","division"})
  public ResponseEntity<?> compute(
      @RequestParam String a,
      @RequestParam String b,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String op = request.getRequestURI().substring(1); // path without leading '/'

    // Read requestId from MDC (populated by RequestIdFilter). If missing, fall back to generating one.
    String requestId = MDC.get("requestId");
    if (requestId == null || requestId.isBlank()) {
      requestId = CalculatorClient.newRequestId();
      // ensure response contains it even if filter didn't run for some reason
      response.setHeader(RequestIdFilter.HEADER, requestId);
    }

    CalculatorResponse r = client.calculate(op, a, b, requestId);
    if (r.ok()) return ResponseEntity.ok().body(java.util.Map.of("result", r.result()));
    return ResponseEntity.badRequest().body(java.util.Map.of("error", r.error()));
  }
}
