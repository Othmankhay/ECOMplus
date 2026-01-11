package com.ecommerce.chatbot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for RAG (Retrieval Augmented Generation)
 */
@Configuration
@ConfigurationProperties(prefix = "chatbot.rag")
@Getter
@Setter
public class RagProperties {

    /**
     * Size of text chunks for embedding
     */
    private int chunkSize = 500;

    /**
     * Overlap between consecutive chunks
     */
    private int chunkOverlap = 50;

    /**
     * Number of top similar documents to retrieve
     */
    private int topK = 5;
}
