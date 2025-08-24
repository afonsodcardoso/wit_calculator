package com.wit.calculator.kafka;

import com.wit.calculator.model.CalculatorRequest;
import com.wit.calculator.model.CalculatorResponse;
import com.wit.calculator.service.BigDecimalCalculator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class CalculatorListener {
  private static final Logger log = LoggerFactory.getLogger(CalculatorListener.class);

  private final BigDecimalCalculator calculator;
  public CalculatorListener(BigDecimalCalculator calculator) { this.calculator = calculator; }

  // Clean annotation using properties from application.properties
  @KafkaListener(topics = "${kafka.topics.request}", groupId = "${spring.kafka.consumer.group-id:calculator-group}")
  @SendTo // reply to the topic indicated by the request’s reply header
  public CalculatorResponse handle(ConsumerRecord<String, CalculatorRequest> rec) {
    String requestId = null;
    String correlationId = null;

    try {
      // Prefer explicit X-Request-Id header; fallback to CORRELATION_ID
      Header custom = rec.headers().lastHeader("X-Request-Id");
      if (custom != null) requestId = new String(custom.value(), StandardCharsets.UTF_8);

      Header corr = rec.headers().lastHeader(KafkaHeaders.CORRELATION_ID);
      if (corr != null) correlationId = new String(corr.value(), StandardCharsets.UTF_8);

      // If no requestId but correlation id present, reuse correlation id as request id
      if ((requestId == null || requestId.isBlank()) && (correlationId != null && !correlationId.isBlank())) {
        requestId = correlationId;
      }

      if (requestId != null) MDC.put("requestId", requestId);
      if (correlationId != null) MDC.put("correlationId", correlationId);

      CalculatorRequest req = rec.value();
      log.info("CalculatorListener received request op={} a={} b={} requestId={}", req.op(), req.a(), req.b(), requestId);

      BigDecimal result = calculator.compute(req.op(), req.a(), req.b());

      log.info("CalculatorListener computed result={} requestId={}", result.toPlainString(), requestId);
      return new CalculatorResponse(result.toPlainString(), null);
    } catch (Exception e) {
      log.error("CalculatorListener error computing request", e);
      return new CalculatorResponse(null, e.getMessage());
    } finally {
      MDC.remove("requestId");
      MDC.remove("correlationId");
    }
  }
}
