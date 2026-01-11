package com.ecommerce.chatbot.rag;

import com.ecommerce.chatbot.config.RagProperties;
import com.ecommerce.chatbot.model.Product;
import com.ecommerce.chatbot.service.InventoryServiceClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleVectorStore {

    private final InventoryServiceClient inventoryServiceClient;
    private final RagProperties ragProperties;

    // In-memory store: document ID -> (embedding vector, original text, product)
    private final Map<String, VectorDocument> documentStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        log.info("Initializing Vector Store...");
        refreshProductData();
    }

    @Scheduled(fixedRate = 300000)
    public void refreshProductData() {
        try {
            List<Product> products = inventoryServiceClient.getAllProducts();
            log.info("Fetched {} products from inventory service", products.size());

            for (Product product : products) {
                String docId = "product_" + product.getId();
                String text = product.toEmbeddingText();

                double[] embedding = createSimpleEmbedding(text);

                documentStore.put(docId, new VectorDocument(
                        docId,
                        text,
                        embedding,
                        product));
            }

            log.info("Vector store updated with {} documents", documentStore.size());
        } catch (Exception e) {
            log.error("Failed to refresh product data: {}", e.getMessage());
        }
    }

    public void addDocument(String id, String text, Product product) {
        double[] embedding = createSimpleEmbedding(text);
        documentStore.put(id, new VectorDocument(id, text, embedding, product));
    }

    public List<VectorDocument> similaritySearch(String query, int topK) {
        if (documentStore.isEmpty()) {
            log.warn("Vector store is empty");
            return Collections.emptyList();
        }

        double[] queryEmbedding = createSimpleEmbedding(query);

        return documentStore.values().stream()
                .map(doc -> new ScoredDocument(doc, cosineSimilarity(queryEmbedding, doc.getEmbedding())))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(topK)
                .map(scored -> scored.document)
                .collect(Collectors.toList());
    }

    public List<VectorDocument> getAllDocuments() {
        return new ArrayList<>(documentStore.values());
    }

    private double[] createSimpleEmbedding(String text) {
        String[] vocabulary = {
                "produit", "product", "prix", "price", "stock", "disponible", "available",
                "catégorie", "category", "description", "qualité", "quality", "nouveau", "new",
                "promotion", "sale", "discount", "remise", "livraison", "delivery", "gratuit", "free",
                "électronique", "electronics", "vêtement", "clothing", "maison", "home",
                "sport", "beauty", "beauté", "livre", "book", "jouet", "toy", "alimentaire", "food",
                "ordinateur", "computer", "téléphone", "phone", "tablette", "tablet",
                "accessoire", "accessory", "meilleur", "best", "populaire", "popular",
                "euro", "€", "cher", "expensive", "pas cher", "cheap", "abordable", "affordable"
        };

        double[] embedding = new double[vocabulary.length];
        String lowerText = text.toLowerCase();

        for (int i = 0; i < vocabulary.length; i++) {
            if (lowerText.contains(vocabulary[i])) {
                embedding[i] = 1.0;
            }
        }

        double norm = 0;
        for (double v : embedding) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);

        if (norm > 0) {
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] /= norm;
            }
        }

        return embedding;
    }

    private double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length)
            return 0;

        double dotProduct = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator > 0 ? dotProduct / denominator : 0;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class VectorDocument {
        private String id;
        private String text;
        private double[] embedding;
        private Product product;
    }

    private static class ScoredDocument {
        VectorDocument document;
        double score;

        ScoredDocument(VectorDocument document, double score) {
            this.document = document;
            this.score = score;
        }
    }
}
