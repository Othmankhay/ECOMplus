package com.ecommerce.chatbot.rag;

import com.ecommerce.chatbot.config.RagProperties;
import com.ecommerce.chatbot.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RagService {

    private final SimpleVectorStore vectorStore;
    private final RagProperties ragProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String openaiModel;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();

    private static final String SYSTEM_PROMPT = """
            Tu es un assistant e-commerce intelligent et amical pour la plateforme ECOMplus.
            Tu aides les clients à trouver des produits, répondre à leurs questions sur les prix,
            la disponibilité et les caractéristiques des produits.

            Instructions:
            - Réponds toujours en français de manière naturelle et professionnelle
            - Utilise les informations des produits fournis dans le contexte
            - Si tu ne trouves pas l'information, dis-le poliment
            - Suggère des produits similaires quand c'est pertinent
            - Sois concis mais informatif
            - Utilise des emojis pour rendre la conversation plus conviviale

            Contexte des produits disponibles:
            %s
            """;

    public String processQuery(String userQuery) {
        log.info("Processing RAG query: {}", userQuery);

        try {
            List<SimpleVectorStore.VectorDocument> relevantDocs = vectorStore.similaritySearch(userQuery,
                    ragProperties.getTopK());

            log.info("Found {} relevant documents", relevantDocs.size());

            String context = buildContext(relevantDocs);
            String response = callOpenAI(userQuery, context);

            return response;

        } catch (Exception e) {
            log.error("Error processing RAG query: {}", e.getMessage(), e);
            return "Désolé, je rencontre des difficultés techniques. " +
                    "Veuillez réessayer dans quelques instants. 🙏";
        }
    }

    private String buildContext(List<SimpleVectorStore.VectorDocument> documents) {
        if (documents.isEmpty()) {
            return "Aucun produit trouvé dans la base de données.";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            SimpleVectorStore.VectorDocument doc = documents.get(i);
            context.append(String.format("%d. %s\n", i + 1, doc.getText()));
        }

        return context.toString();
    }

    private String callOpenAI(String userQuery, String context) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            log.warn("OpenAI API key not configured, using fallback response");
            return generateFallbackResponse(userQuery, context);
        }

        try {
            String systemPrompt = String.format(SYSTEM_PROMPT, context);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openaiModel);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userQuery));
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);

            String response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            return jsonResponse
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage(), e);
            return generateFallbackResponse(userQuery, context);
        }
    }

    private String generateFallbackResponse(String query, String context) {
        if (context.contains("Aucun produit")) {
            return "Je n'ai pas trouvé de produits correspondant à votre recherche. " +
                    "Essayez avec d'autres mots-clés ou consultez notre catalogue complet. 📦";
        }

        return String.format("""
                Voici les produits que j'ai trouvés pour votre recherche:

                %s

                N'hésitez pas à me demander plus de détails sur un produit! 😊
                """, context);
    }

    public List<Product> getProductRecommendations(String query, int limit) {
        List<SimpleVectorStore.VectorDocument> docs = vectorStore.similaritySearch(query, limit);

        return docs.stream()
                .map(SimpleVectorStore.VectorDocument::getProduct)
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    public String getGreetingResponse(String userName) {
        return String.format("""
                Bonjour %s! 👋

                Je suis l'assistant ECOMplus, votre guide shopping personnel! 🛍️

                Je peux vous aider à:
                🔍 Rechercher des produits
                💰 Comparer les prix
                📦 Vérifier la disponibilité
                ❓ Répondre à vos questions

                Comment puis-je vous aider aujourd'hui?
                """, userName != null ? userName : "");
    }

    public String getHelpMessage() {
        return """
                🤖 **Guide d'utilisation du Chatbot ECOMplus**

                Voici ce que je peux faire pour vous:

                📱 **Commandes disponibles:**
                • /start - Démarrer une conversation
                • /help - Afficher cette aide
                • /products - Voir les produits populaires
                • /search [terme] - Rechercher un produit

                💬 **Vous pouvez aussi me poser des questions comme:**
                • "Quels sont vos smartphones disponibles?"
                • "Avez-vous des promotions?"
                • "Quel est le prix du [produit]?"
                • "Recommandez-moi un cadeau"

                N'hésitez pas à me parler naturellement! 😊
                """;
    }
}
