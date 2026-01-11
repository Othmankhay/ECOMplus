package com.ecommerce.kafka.producer.service;

import com.ecommerce.kafka.producer.model.OrderEvent;
import com.ecommerce.kafka.producer.model.PageViewEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.orders}")
    private String ordersTopic;

    @Value("${kafka.topic.page-views}")
    private String pageViewsTopic;

    @Value("${kafka.topic.analytics}")
    private String analyticsTopic;

    public CompletableFuture<SendResult<String, Object>> sendOrderEvent(OrderEvent event) {
        log.info("Sending order event to topic {}: {}", ordersTopic, event.getOrderId());

        return kafkaTemplate.send(ordersTopic, event.getOrderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Order event sent successfully: partition={}, offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send order event: {}", ex.getMessage());
                    }
                });
    }

    public CompletableFuture<SendResult<String, Object>> sendPageViewEvent(PageViewEvent event) {
        log.info("Sending page view event to topic {}: {}", pageViewsTopic, event.getSessionId());

        return kafkaTemplate.send(pageViewsTopic, event.getSessionId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Page view event sent successfully: partition={}, offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send page view event: {}", ex.getMessage());
                    }
                });
    }

    public CompletableFuture<SendResult<String, Object>> sendAnalyticsEvent(String key, Object event) {
        log.info("Sending analytics event to topic {}", analyticsTopic);

        return kafkaTemplate.send(analyticsTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Analytics event sent successfully");
                    } else {
                        log.error("Failed to send analytics event: {}", ex.getMessage());
                    }
                });
    }

    public CompletableFuture<SendResult<String, Object>> sendMessage(String topic, String key, Object message) {
        log.info("Sending message to topic {}", topic);
        return kafkaTemplate.send(topic, key, message);
    }
}
