package com.example.service;

import java.time.LocalDate;
import java.util.List;

import com.example.model.Product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

@ApplicationScoped
public class JsonbService {
    private final Jsonb jsonb;

    public JsonbService() {
        JsonbConfig config = new JsonbConfig().withFormatting(true);
        this.jsonb = JsonbBuilder.create(config);
    }

    public String serializeProduct(Product product) {
        return jsonb.toJson(product);
    }

    public Product deserializeProduct(String json) {
        return jsonb.fromJson(json, Product.class);
    }

    public Product sampleProduct() {
        return new Product(1L, "Laptop", 999.99, 50, LocalDate.of(2024, 1, 15));
    }

    public String serializeList(List<Product> products) {
        return jsonb.toJson(products);
    }
}