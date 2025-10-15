package com.example.service;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@ApplicationScoped
public class JsonpService {
    public String buildProductJson(Long id, String name, Double price, Integer stock) {
        JsonObject product = Json.createObjectBuilder()
                .add("id", id)
                .add("name", name)
                .add("price", BigDecimal.valueOf(price))
                .add("stock_quantity", stock)
                .add("metadata", Json.createObjectBuilder()
                        .add("featured", true)
                        .add("tags", Json.createArrayBuilder()
                                .add("electronics").add("computers")))
                .build();
        return product.toString();
    }

    public Map<String, Object> parseProductJson(String jsonString) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject obj = reader.readObject();
        Map<String, Object> result = new HashMap<>();
        result.put("id", obj.getJsonNumber("id").longValue());
        result.put("name", obj.getString("name"));
        result.put("price", obj.getJsonNumber("price").doubleValue());
        return result;
    }

    public String applyDiscount(String json, double percent) {
        JsonObject product = Json.createReader(new StringReader(json)).readObject();
        double newPrice = product.getJsonNumber("price").doubleValue() * (1 - percent / 100);
        JsonObject updated = Json.createObjectBuilder(product)
                .add("price", newPrice)
                .add("discount_applied", percent)
                .build();
        return updated.toString();
    }
}