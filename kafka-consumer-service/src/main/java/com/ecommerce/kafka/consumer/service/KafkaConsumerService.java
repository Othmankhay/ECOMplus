package com.ecommerce.kafka.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final SimpMessagingTemplate messagingTemplate;

    private final List<Map<String, Object>> recentOrders = new ArrayList<>();
    private final List<Map<String, Object>> recentPageViews = new ArrayList<>();
    private final int MAX_RECENT_EVENTS = 100;

    private final AtomicLong orderCount = new AtomicLong(0);
    private final AtomicLong pageViewCount = new AtomicLong(0);
    private final Map<String, AtomicLong> ordersByStatus = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> pageViewsByPage = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${kafka.topic.orders}", groupId = "ecom-consumer-group")
    public void consumeOrderEvent(Map<String, Object> order) {
        log.info("Received order event: {}", order.get("orderId"));

        orderCount.incrementAndGet();
        String status = (String) order.getOrDefault("status", "UNKNOWN");
        ordersByStatus.computeIfAbsent(status, k -> new AtomicLong(0)).incrementAndGet();

        synchronized (recentOrders) {
            recentOrders.add(0, order);
            if (recentOrders.size() > MAX_RECENT_EVENTS) {
                recentOrders.remove(recentOrders.size() - 1);
            }
        }

        messagingTemplate.convertAndSend("/topic/orders", order);
        messagingTemplate.convertAndSend("/topic/stats", getStatistics());
    }

    @KafkaListener(topics = "${kafka.topic.page-views}", groupId = "ecom-consumer-group")
    public void consumePageViewEvent(Map<String, Object> pageView) {
        log.info("Received page view event: session={}", pageView.get("sessionId"));

        pageViewCount.incrementAndGet();
        String page = (String) pageView.getOrDefault("page", "/unknown");
        pageViewsByPage.computeIfAbsent(page, k -> new AtomicLong(0)).incrementAndGet();

        synchronized (recentPageViews) {
            recentPageViews.add(0, pageView);
            if (recentPageViews.size() > MAX_RECENT_EVENTS) {
                recentPageViews.remove(recentPageViews.size() - 1);
            }
        }

        messagingTemplate.convertAndSend("/topic/page-views", pageView);
        messagingTemplate.convertAndSend("/topic/stats", getStatistics());
    }

    @KafkaListener(topics = "${kafka.topic.analytics}", groupId = "ecom-consumer-group")
    public void consumeAnalyticsEvent(Map<String, Object> analytics) {
        log.info("Received analytics event");

        messagingTemplate.convertAndSend("/topic/analytics", analytics);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalOrders", orderCount.get());
        stats.put("totalPageViews", pageViewCount.get());
        stats.put("ordersByStatus", convertAtomicMap(ordersByStatus));
        stats.put("pageViewsByPage", convertAtomicMap(pageViewsByPage));
        return stats;
    }

    public List<Map<String, Object>> getRecentOrders() {
        synchronized (recentOrders) {
            return new ArrayList<>(recentOrders);
        }
    }

    public List<Map<String, Object>> getRecentPageViews() {
        synchronized (recentPageViews) {
            return new ArrayList<>(recentPageViews);
        }
    }

    private Map<String, Long> convertAtomicMap(Map<String, AtomicLong> atomicMap) {
        Map<String, Long> result = new ConcurrentHashMap<>();
        atomicMap.forEach((key, value) -> result.put(key, value.get()));
        return result;
    }
}
