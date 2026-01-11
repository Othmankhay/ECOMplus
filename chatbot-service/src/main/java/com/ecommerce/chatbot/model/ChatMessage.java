package com.ecommerce.chatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a chat conversation message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private Long telegramChatId;
    private String userId;
    private String userMessage;
    private String botResponse;
    private LocalDateTime timestamp;
    private MessageType type;

    public enum MessageType {
        USER_QUERY,
        PRODUCT_SEARCH,
        ORDER_STATUS,
        GENERAL_INFO,
        GREETING,
        UNKNOWN
    }
}
