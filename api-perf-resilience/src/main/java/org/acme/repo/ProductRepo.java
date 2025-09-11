package org.acme.repo;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.acme.domain.Product;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductRepo {
    private final Map<String, Product> data = new ConcurrentHashMap<>();

    public ProductRepo() {
        var now = Instant.now();
        data.put("1", new Product("1", "Coffee Beans", 42, now));
        data.put("2", new Product("2", "Espresso Machine", 5, now));
    }

    public Product find(String id) {
        return data.get(id);
    }

    public Product updateStock(String id, int stock) {
        var existing = data.get(id);
        if (existing == null)
            return null;
        var updated = new Product(id, existing.name(), stock, Instant.now());
        data.put(id, updated);
        return updated;
    }
}
