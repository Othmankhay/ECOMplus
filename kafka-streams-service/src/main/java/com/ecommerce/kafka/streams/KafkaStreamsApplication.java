package com.ecommerce.kafka.streams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafkaStreams
@EnableScheduling
public class KafkaStreamsApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamsApplication.class, args);
    }
}
