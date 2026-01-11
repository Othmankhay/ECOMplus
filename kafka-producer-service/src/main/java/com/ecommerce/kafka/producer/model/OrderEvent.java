package com.ecommerce.kafka.producer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Order Event model for Kafka messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderId;
    private String customerId;
    private String productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double totalAmount;
    private String status;
    private LocalDateTime timestamp;
}
