package com.ecommerce.chatbot.controller;

import com.ecommerce.chatbot.model.Product;
import com.ecommerce.chatbot.rag.RagService;
import com.ecommerce.chatbot.rag.SimpleVectorStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final RagService ragService;
    private final SimpleVectorStore vectorStore;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "chatbot-service");
        health.put("vectorStoreSize", vectorStore.getAllDocuments().size());
        return ResponseEntity.ok(health);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("Received chat request: {}", request.message());

        String response = ragService.processQuery(request.message());

        return ResponseEntity.ok(new ChatResponse(response, true));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Product>> getRecommendations(
            @RequestParam(defaultValue = "populaire") String query,
            @RequestParam(defaultValue = "5") int limit) {

        List<Product> recommendations = ragService.getProductRecommendations(query, limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String query) {
        log.info("Search request: {}", query);

        String response = ragService.processQuery(query);
        List<Product> products = ragService.getProductRecommendations(query, 5);

        Map<String, Object> result = new HashMap<>();
        result.put("response", response);
        result.put("products", products);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshData() {
        log.info("Refreshing vector store data...");
        vectorStore.refreshProductData();

        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Vector store refreshed with " + vectorStore.getAllDocuments().size() + " documents");

        return ResponseEntity.ok(result);
    }

    public record ChatRequest(String message, String userId) {
    }

    public record ChatResponse(String response, boolean success) {
    }
}
