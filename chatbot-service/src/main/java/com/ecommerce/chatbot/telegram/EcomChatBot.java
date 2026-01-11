package com.ecommerce.chatbot.telegram;

import com.ecommerce.chatbot.config.TelegramBotProperties;
import com.ecommerce.chatbot.model.Product;
import com.ecommerce.chatbot.rag.RagService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Telegram Bot implementation for ECOMplus Chatbot
 * Handles all incoming messages and commands
 */
@Component
@Slf4j
public class EcomChatBot extends TelegramLongPollingBot {

    private final TelegramBotProperties botProperties;
    private final RagService ragService;

    public EcomChatBot(TelegramBotProperties botProperties, RagService ragService) {
        super(botProperties.getToken());
        this.botProperties = botProperties;
        this.ragService = ragService;
    }

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            log.info("✅ Telegram Bot registered successfully: @{}", botProperties.getUsername());
        } catch (TelegramApiException e) {
            log.error("❌ Failed to register Telegram bot: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            User user = update.getMessage().getFrom();
            String userName = user.getFirstName();

            log.info("Received message from {} (chatId: {}): {}", userName, chatId, messageText);

            // Process message asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    String response = processMessage(messageText, userName);
                    sendResponse(chatId, response);
                } catch (Exception e) {
                    log.error("Error processing message: {}", e.getMessage(), e);
                    sendResponse(chatId, "Désolé, une erreur s'est produite. Veuillez réessayer. 🙏");
                }
            });
        }
    }

    /**
     * Process incoming message and generate response
     */
    private String processMessage(String message, String userName) {
        String lowerMessage = message.toLowerCase().trim();

        // Handle commands
        if (lowerMessage.startsWith("/")) {
            return handleCommand(lowerMessage, userName);
        }

        // Handle greetings
        if (isGreeting(lowerMessage)) {
            return ragService.getGreetingResponse(userName);
        }

        // Use RAG for other queries
        return ragService.processQuery(message);
    }

    /**
     * Handle bot commands
     */
    private String handleCommand(String command, String userName) {
        if (command.startsWith("/start")) {
            return ragService.getGreetingResponse(userName);
        }

        if (command.startsWith("/help")) {
            return ragService.getHelpMessage();
        }

        if (command.startsWith("/products")) {
            return getPopularProductsMessage();
        }

        if (command.startsWith("/search ")) {
            String query = command.substring(8).trim();
            if (query.isEmpty()) {
                return "Veuillez spécifier un terme de recherche. Exemple: /search smartphone";
            }
            return ragService.processQuery("Rechercher: " + query);
        }

        return "Commande non reconnue. Tapez /help pour voir les commandes disponibles.";
    }

    /**
     * Check if message is a greeting
     */
    private boolean isGreeting(String message) {
        String[] greetings = { "bonjour", "salut", "hello", "hi", "coucou", "hey", "bonsoir" };
        for (String greeting : greetings) {
            if (message.contains(greeting)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get popular products message
     */
    private String getPopularProductsMessage() {
        List<Product> products = ragService.getProductRecommendations("populaire meilleur", 5);

        if (products.isEmpty()) {
            return "📦 Aucun produit disponible pour le moment. Revenez bientôt!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🌟 **Produits Populaires** 🌟\n\n");

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            sb.append(String.format("%d. **%s**\n", i + 1, p.getName()));
            sb.append(String.format("   💰 Prix: %.2f €\n", p.getPrice() != null ? p.getPrice() : 0.0));
            sb.append(String.format("   📦 Stock: %d\n\n", p.getQuantity() != null ? p.getQuantity() : 0));
        }

        sb.append("Demandez-moi plus de détails sur n'importe quel produit! 😊");

        return sb.toString();
    }

    /**
     * Send response to Telegram chat
     */
    private void sendResponse(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");

        // Add quick reply keyboard
        message.setReplyMarkup(createQuickReplyKeyboard());

        try {
            execute(message);
            log.info("Response sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message: {}", e.getMessage());

            // Try without markdown if parsing failed
            try {
                message.setParseMode(null);
                execute(message);
            } catch (TelegramApiException ex) {
                log.error("Failed to send message even without markdown: {}", ex.getMessage());
            }
        }
    }

    /**
     * Create quick reply keyboard for common actions
     */
    private ReplyKeyboardMarkup createQuickReplyKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        // Row 1
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("🔍 Rechercher"));
        row1.add(new KeyboardButton("📦 Produits"));
        keyboard.add(row1);

        // Row 2
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("💰 Promotions"));
        row2.add(new KeyboardButton("❓ Aide"));
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
