package com.ecommerce.chatbot.service;

import com.ecommerce.chatbot.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "INVENTORY-SERVICE", fallback = InventoryServiceClientFallback.class)
public interface InventoryServiceClient {

    @GetMapping("/products")
    List<Product> getAllProducts();

    @GetMapping("/products/{id}")
    Product getProductById(@PathVariable("id") Long id);

    @GetMapping("/products/search")
    List<Product> searchProducts(@RequestParam("query") String query);
}
