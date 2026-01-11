package com.ecommerce.kafka.producer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Page View Event for analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageViewEvent {
    private String sessionId;
    private String userId;
    private String page;
    private String productId;
    private String category;
    private String action; // VIEW, CLICK, ADD_TO_CART, PURCHASE
    private String userAgent;
    private String ipAddress;
    private Long duration; // in milliseconds
    private LocalDateTime timestamp;
}
