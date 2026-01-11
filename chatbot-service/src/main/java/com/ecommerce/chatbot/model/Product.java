package com.ecommerce.chatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a product from the inventory service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private String category;

    /**
     * Convert product to text for embedding
     */
    public String toEmbeddingText() {
        return String.format(
                "Produit: %s. Description: %s. Catégorie: %s. Prix: %.2f €. Stock: %d unités.",
                name != null ? name : "N/A",
                description != null ? description : "Aucune description",
                category != null ? category : "Non catégorisé",
                price != null ? price : 0.0,
                quantity != null ? quantity : 0);
    }
}
