package com.ecommerce.kafka.streams.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Analytics Service
 * Manages real-time analytics data and broadcasts to WebSocket clients
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final SimpMessagingTemplate messagingTemplate;
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    // Real-time counters
    private final Map<String, AtomicLong> ordersByStatus = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> pageViewsByPage = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> actionCounts = new ConcurrentHashMap<>();
    private final AtomicLong totalOrders = new AtomicLong(0);
    private final AtomicLong totalPageViews = new AtomicLong(0);
    private final AtomicLong totalRevenue = new AtomicLong(0);

    // Recent analytics events
    private final List<Map<String, Object>> recentAnalytics = Collections.synchronizedList(new ArrayList<>());
    private final int MAX_ANALYTICS = 50;

    /**
     * Listen to analytics output from Kafka Streams
     */
    @KafkaListener(topics = "ecom-analytics-output", groupId = "analytics-consumer")
    public void consumeAnalytics(String analyticsData) {
        log.debug("Received analytics: {}", analyticsData);

        try {
            Map<String, Object> data = parseJson(analyticsData);
            data.put("receivedAt", Instant.now().toString());

            // Store recent analytics
            synchronized (recentAnalytics) {
                recentAnalytics.add(0, data);
                if (recentAnalytics.size() > MAX_ANALYTICS) {
                    recentAnalytics.remove(recentAnalytics.size() - 1);
                }
            }

            // Broadcast to WebSocket clients
            messagingTemplate.convertAndSend("/topic/analytics", data);

        } catch (Exception e) {
            log.error("Error processing analytics: {}", e.getMessage());
        }
    }

    /**
     * Broadcast current statistics every 2 seconds
     */
    @Scheduled(fixedRate = 2000)
    public void broadcastStatistics() {
        Map<String, Object> stats = getStatistics();
        messagingTemplate.convertAndSend("/topic/stats", stats);
    }

    /**
     * Get current statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", totalOrders.get());
        stats.put("totalPageViews", totalPageViews.get());
        stats.put("totalRevenue", totalRevenue.get() / 100.0); // Convert cents to dollars
        stats.put("ordersByStatus", convertToMap(ordersByStatus));
        stats.put("pageViewsByPage", convertToMap(pageViewsByPage));
        stats.put("actionCounts", convertToMap(actionCounts));
        stats.put("timestamp", Instant.now().toString());
        return stats;
    }

    /**
     * Get recent analytics events
     */
    public List<Map<String, Object>> getRecentAnalytics() {
        synchronized (recentAnalytics) {
            return new ArrayList<>(recentAnalytics);
        }
    }

    /**
     * Update order statistics
     */
    public void updateOrderStats(String status, double amount) {
        totalOrders.incrementAndGet();
        totalRevenue.addAndGet((long) (amount * 100));
        ordersByStatus.computeIfAbsent(status, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * Update page view statistics
     */
    public void updatePageViewStats(String page, String action) {
        totalPageViews.incrementAndGet();
        pageViewsByPage.computeIfAbsent(page, k -> new AtomicLong(0)).incrementAndGet();
        actionCounts.computeIfAbsent(action, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * Get Kafka Streams state (if available)
     */
    public KafkaStreams.State getStreamsState() {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        return kafkaStreams != null ? kafkaStreams.state() : null;
    }

    private Map<String, Long> convertToMap(Map<String, AtomicLong> atomicMap) {
        Map<String, Long> result = new HashMap<>();
        atomicMap.forEach((key, value) -> result.put(key, value.get()));
        return result;
    }

    private Map<String, Object> parseJson(String json) {
        Map<String, Object> result = new HashMap<>();
        // Simple JSON parsing
        json = json.replace("{", "").replace("}", "").replace("\"", "");
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                try {
                    result.put(key, Long.parseLong(value));
                } catch (NumberFormatException e) {
                    result.put(key, value);
                }
            }
        }
        return result;
    }
}
