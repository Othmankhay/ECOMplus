package com.ecommerce.chatbot.service;

import com.ecommerce.chatbot.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class InventoryServiceClientFallback implements InventoryServiceClient {

    @Override
    public List<Product> getAllProducts() {
        log.warn("Fallback: Inventory service unavailable - returning empty product list");
        return new ArrayList<>();
    }

    @Override
    public Product getProductById(Long id) {
        log.warn("Fallback: Inventory service unavailable - returning null for product id: {}", id);
        return null;
    }

    @Override
    public List<Product> searchProducts(String query) {
        log.warn("Fallback: Inventory service unavailable - cannot search for: {}", query);
        return new ArrayList<>();
    }
}
