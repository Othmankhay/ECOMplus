package com.ecommerce.kafka.streams.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaStreamsConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(StreamsConfig.STATE_DIR_CONFIG, "./kafka-streams-state");
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KStream<String, String> orderAnalyticsStream(StreamsBuilder builder) {
        log.info("Building order analytics stream...");

        KStream<String, String> ordersStream = builder.stream("ecom-orders",
                Consumed.with(Serdes.String(), Serdes.String()));

        ordersStream
                .groupBy((key, value) -> extractStatus(value))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .count(Materialized.as("orders-by-status-store"))
                .toStream()
                .map((windowedKey, count) -> KeyValue.pair(
                        windowedKey.key(),
                        String.format("{\"status\":\"%s\",\"count\":%d,\"window\":\"%s\"}",
                                windowedKey.key(), count, windowedKey.window().startTime())))
                .to("ecom-analytics-output", Produced.with(Serdes.String(), Serdes.String()));

        return ordersStream;
    }

    @Bean
    public KStream<String, String> pageViewAnalyticsStream(StreamsBuilder builder) {
        log.info("Building page view analytics stream...");

        KStream<String, String> pageViewsStream = builder.stream("page-views",
                Consumed.with(Serdes.String(), Serdes.String()));

        pageViewsStream
                .groupBy((key, value) -> extractPage(value))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(30)))
                .count(Materialized.as("page-views-by-page-store"))
                .toStream()
                .map((windowedKey, count) -> KeyValue.pair(
                        windowedKey.key(),
                        String.format("{\"page\":\"%s\",\"count\":%d,\"window\":\"%s\"}",
                                windowedKey.key(), count, windowedKey.window().startTime())))
                .to("ecom-analytics-output", Produced.with(Serdes.String(), Serdes.String()));

        pageViewsStream
                .groupBy((key, value) -> extractAction(value))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .count(Materialized.as("page-views-by-action-store"))
                .toStream()
                .map((windowedKey, count) -> KeyValue.pair(
                        "action-" + windowedKey.key(),
                        String.format("{\"action\":\"%s\",\"count\":%d,\"window\":\"%s\"}",
                                windowedKey.key(), count, windowedKey.window().startTime())))
                .to("ecom-analytics-output", Produced.with(Serdes.String(), Serdes.String()));

        return pageViewsStream;
    }

    private String extractStatus(String json) {
        try {
            int startIndex = json.indexOf("\"status\":\"") + 10;
            if (startIndex > 9) {
                int endIndex = json.indexOf("\"", startIndex);
                return json.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            log.warn("Failed to extract status from: {}", json);
        }
        return "UNKNOWN";
    }

    private String extractPage(String json) {
        try {
            int startIndex = json.indexOf("\"page\":\"") + 8;
            if (startIndex > 7) {
                int endIndex = json.indexOf("\"", startIndex);
                return json.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            log.warn("Failed to extract page from: {}", json);
        }
        return "/unknown";
    }

    private String extractAction(String json) {
        try {
            int startIndex = json.indexOf("\"action\":\"") + 10;
            if (startIndex > 9) {
                int endIndex = json.indexOf("\"", startIndex);
                return json.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            log.warn("Failed to extract action from: {}", json);
        }
        return "VIEW";
    }
}
