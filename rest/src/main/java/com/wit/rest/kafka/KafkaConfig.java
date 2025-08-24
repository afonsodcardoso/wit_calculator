package com.wit.rest.kafka;

import com.wit.calculator.model.CalculatorRequest;
import com.wit.calculator.model.CalculatorResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}") private String bootstrap;
  @Value("${kafka.topics.reply}") private String replyTopic;

  @Bean
  public ProducerFactory<String, CalculatorRequest> producerFactory() {
    return new DefaultKafkaProducerFactory<>(Map.of(
      org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
      org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class,
      org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
    ));
  }

  @Bean
  public ConsumerFactory<String, CalculatorResponse> consumerFactory() {
    JsonDeserializer<CalculatorResponse> des = new JsonDeserializer<>(CalculatorResponse.class);
    des.addTrustedPackages("*");
    return new DefaultKafkaConsumerFactory<>(Map.of(
      org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
      ConsumerConfig.GROUP_ID_CONFIG, "rest-replies"
    ), new StringDeserializer(), des);
  }

  @Bean
  public ConcurrentMessageListenerContainer<String, CalculatorResponse> repliesContainer() {
    ContainerProperties props = new ContainerProperties(replyTopic);
    props.setGroupId("rest-replies");
    return new ConcurrentMessageListenerContainer<>(consumerFactory(), props);
  }

  @Bean
  public ReplyingKafkaTemplate<String, CalculatorRequest, CalculatorResponse> replyingKafkaTemplate() {
    return new ReplyingKafkaTemplate<>(producerFactory(), repliesContainer());
  }
  // Request-reply over Kafka with ReplyingKafkaTemplate is a built-in Spring Kafka pattern.
}
