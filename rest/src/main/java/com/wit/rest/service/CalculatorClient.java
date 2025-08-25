package com.wit.rest.service;

import com.wit.calculator.model.CalculatorRequest;
import com.wit.calculator.model.CalculatorResponse;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyMessageFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//(hides Kafka from the controller)
@Service
public class CalculatorClient {

  private static final Logger log = LoggerFactory.getLogger(CalculatorClient.class);

  private final ReplyingKafkaTemplate<String, CalculatorRequest, CalculatorResponse> rkt;
  private final String requestTopic;
  private final String replyTopic;

  public CalculatorClient(
      ReplyingKafkaTemplate<String, CalculatorRequest, CalculatorResponse> rkt,
      @Value("${kafka.topics.request}") String requestTopic,
      @Value("${kafka.topics.reply}") String replyTopic) {
    this.rkt = rkt;
    this.requestTopic = requestTopic;
    this.replyTopic = replyTopic;
  }

  public CalculatorResponse calculate(String op, String a, String b, String requestId) throws Exception {
    CalculatorRequest payload = new CalculatorRequest(op, a, b);

    // Build message with headers:
    var msg = MessageBuilder.withPayload(payload)
        .setHeader(KafkaHeaders.TOPIC, requestTopic)
        .setHeader(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes(StandardCharsets.UTF_8))
        // correlation id used by ReplyingKafkaTemplate
        .setHeader(KafkaHeaders.CORRELATION_ID, requestId.getBytes(StandardCharsets.UTF_8))
        // explicit header for consumer convenience
        .setHeader("X-Request-Id", requestId)
        .build();

    // Log send
    String currentMdc = MDC.get("requestId");
    log.info("Sending request to topic={} op={} a={} b={} requestId={}", requestTopic, op, a, b, requestId);

    RequestReplyMessageFuture<String, CalculatorRequest> future = rkt.sendAndReceive(msg);

    Message<?> replyMessage = future.get(10, TimeUnit.SECONDS);

    // Log reply
    if (replyMessage != null) {
      log.info("Received reply for requestId={} payloadClass={}", requestId, replyMessage.getPayload().getClass().getSimpleName());
    } else {
      log.warn("No reply received for requestId={}", requestId);
    }

    if (replyMessage == null) {
      throw new IllegalStateException("No reply received for requestId=" + requestId);
    }
    return (CalculatorResponse) replyMessage.getPayload();
  }

  public static String newRequestId() { return UUID.randomUUID().toString(); }
}
