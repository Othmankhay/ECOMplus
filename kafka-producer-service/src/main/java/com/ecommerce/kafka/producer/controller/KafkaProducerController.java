package com.ecommerce.kafka.producer.controller;

import com.ecommerce.kafka.producer.model.OrderEvent;
import com.ecommerce.kafka.producer.model.PageViewEvent;
import com.ecommerce.kafka.producer.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class KafkaProducerController {

    private final KafkaProducerService producerService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "kafka-producer-service");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> sendOrder(@RequestBody OrderEvent orderEvent) {
        log.info("Received order event request: {}", orderEvent);

        if (orderEvent.getOrderId() == null) {
            orderEvent.setOrderId(UUID.randomUUID().toString());
        }
        if (orderEvent.getTimestamp() == null) {
            orderEvent.setTimestamp(LocalDateTime.now());
        }
        if (orderEvent.getStatus() == null) {
            orderEvent.setStatus("CREATED");
        }

        producerService.sendOrderEvent(orderEvent);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Order event sent to Kafka");
        response.put("orderId", orderEvent.getOrderId());
        response.put("timestamp", orderEvent.getTimestamp());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/page-views")
    public ResponseEntity<Map<String, Object>> sendPageView(@RequestBody PageViewEvent pageViewEvent) {
        log.info("Received page view event request: {}", pageViewEvent);

        if (pageViewEvent.getSessionId() == null) {
            pageViewEvent.setSessionId(UUID.randomUUID().toString());
        }
        if (pageViewEvent.getTimestamp() == null) {
            pageViewEvent.setTimestamp(LocalDateTime.now());
        }

        producerService.sendPageViewEvent(pageViewEvent);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Page view event sent to Kafka");
        response.put("sessionId", pageViewEvent.getSessionId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders/generate/{count}")
    public ResponseEntity<Map<String, Object>> generateOrders(@PathVariable int count) {
        log.info("Generating {} sample order events", count);

        String[] products = { "Laptop", "Smartphone", "Tablet", "Headphones", "Watch", "Camera" };
        String[] statuses = { "CREATED", "PROCESSING", "SHIPPED", "DELIVERED" };

        for (int i = 0; i < count; i++) {
            OrderEvent event = OrderEvent.builder()
                    .orderId(UUID.randomUUID().toString())
                    .customerId("CUST-" + (int) (Math.random() * 1000))
                    .productId("PROD-" + (int) (Math.random() * 100))
                    .productName(products[(int) (Math.random() * products.length)])
                    .quantity((int) (Math.random() * 5) + 1)
                    .price(Math.round(Math.random() * 500 * 100.0) / 100.0)
                    .status(statuses[(int) (Math.random() * statuses.length)])
                    .timestamp(LocalDateTime.now())
                    .build();

            event.setTotalAmount(event.getPrice() * event.getQuantity());
            producerService.sendOrderEvent(event);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", count + " order events generated and sent");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/page-views/generate/{count}")
    public ResponseEntity<Map<String, Object>> generatePageViews(@PathVariable int count) {
        log.info("Generating {} sample page view events", count);

        String[] pages = { "/home", "/products", "/product-detail", "/cart", "/checkout", "/profile" };
        String[] categories = { "Electronics", "Clothing", "Home", "Sports", "Books" };
        String[] actions = { "VIEW", "CLICK", "ADD_TO_CART", "PURCHASE" };

        for (int i = 0; i < count; i++) {
            PageViewEvent event = PageViewEvent.builder()
                    .sessionId("SESSION-" + (int) (Math.random() * 100))
                    .userId("USER-" + (int) (Math.random() * 500))
                    .page(pages[(int) (Math.random() * pages.length)])
                    .productId("PROD-" + (int) (Math.random() * 100))
                    .category(categories[(int) (Math.random() * categories.length)])
                    .action(actions[(int) (Math.random() * actions.length)])
                    .duration((long) (Math.random() * 60000))
                    .timestamp(LocalDateTime.now())
                    .build();

            producerService.sendPageViewEvent(event);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", count + " page view events generated and sent");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/send/{topic}")
    public ResponseEntity<Map<String, Object>> sendToTopic(
            @PathVariable String topic,
            @RequestParam(required = false) String key,
            @RequestBody Object message) {

        String messageKey = key != null ? key : UUID.randomUUID().toString();
        producerService.sendMessage(topic, messageKey, message);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Message sent to topic: " + topic);
        response.put("key", messageKey);

        return ResponseEntity.ok(response);
    }
}
