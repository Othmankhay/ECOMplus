package com.ecommerce.kafka.streams.controller;

import com.ecommerce.kafka.streams.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Controller for Analytics Dashboard
 */
@Controller
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Dashboard Page
     */
    @GetMapping("/")
    public String dashboard(Model model) {
        return "dashboard";
    }

    /**
     * Get current statistics API
     */
    @GetMapping("/api/analytics/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(analyticsService.getStatistics());
    }

    /**
     * Get recent analytics events API
     */
    @GetMapping("/api/analytics/recent")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRecentAnalytics() {
        return ResponseEntity.ok(analyticsService.getRecentAnalytics());
    }

    /**
     * Health check and Streams state
     */
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> health() {
        KafkaStreams.State state = analyticsService.getStreamsState();
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "streamsState", state != null ? state.name() : "UNKNOWN",
                "service", "kafka-streams-service"));
    }
}
