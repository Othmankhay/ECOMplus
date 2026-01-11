package com.ecommerce.kafka.supplier.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Supplier Service
 * Reacts to orders to initiate fulfillment/supply chain
 */
@Service
@Slf4j
public class SupplierService {

    @KafkaListener(topics = "${kafka.topic.orders}", groupId = "supplier-group")
    public void processOrder(Map<String, Object> order) {
        String orderId = (String) order.get("orderId");
        String productId = (String) order.get("productId");
        Integer quantity = (Integer) order.get("quantity");

        log.info("📦 Received order for supply processing: OrderID={}, ProductID={}, Quantity={}",
                orderId, productId, quantity);

        // Simulate supply chain logic
        if (quantity > 10) {
            log.warn("⚠️ Large order detected! Initiating stock replenishment for ProductID={}", productId);
        } else {
            log.info("✅ Order processed for shipping.");
        }
    }
}
