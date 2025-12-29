package com.example.repository;

import java.util.List;

import com.example.entity.Product;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    public List<Product> findByPopularity(String category, String cursor, int limit) {

        if (cursor == null || cursor.isBlank()) {
            String base = category == null
                    ? "ORDER BY viewCount DESC, id ASC"
                    : "category = :category ORDER BY viewCount DESC, id ASC";

            return category == null
                    ? find(base).page(0, limit).list()
                    : find(base, Parameters.with("category", category))
                            .page(0, limit)
                            .list();
        }

        String[] parts = cursor.split(":");
        int cursorViews = Integer.parseInt(parts[0]);
        long cursorId = Long.parseLong(parts[1]);

        String where = category == null
                ? """
                        WHERE (viewCount < :views)
                           OR (viewCount = :views AND id > :id)
                        ORDER BY viewCount DESC, id ASC
                        """
                : """
                        WHERE category = :category
                          AND ((viewCount < :views)
                           OR (viewCount = :views AND id > :id))
                        ORDER BY viewCount DESC, id ASC
                        """;

        Parameters params = Parameters
                .with("views", cursorViews)
                .and("id", cursorId);

        if (category != null) {
            params.and("category", category);
        }

        return find(where, params).page(0, limit).list();
    }
}