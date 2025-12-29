package com.example.resource;

import java.time.Instant;
import java.util.List;

import com.example.dto.PageResponse;
import com.example.dto.ProductDTO;
import com.example.entity.Product;
import com.example.repository.ProductRepository;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    ProductRepository repository;

    @GET
    public PageResponse<ProductDTO> list(
            @QueryParam("category") String category,
            @QueryParam("cursor") String cursor,
            @QueryParam("limit") @DefaultValue("20") int limit) {

        limit = Math.min(limit, 100);

        List<Product> results = repository.findByPopularity(category, cursor, limit + 1);

        boolean hasMore = results.size() > limit;
        if (hasMore) {
            results = results.subList(0, limit);
        }

        List<ProductDTO> data = results.stream()
                .map(this::toDTO)
                .toList();

        String nextCursor = hasMore && !results.isEmpty()
                ? encodeCursor(results.get(results.size() - 1))
                : null;

        return new PageResponse<>(data, nextCursor, hasMore, data.size());
    }

    private ProductDTO toDTO(Product p) {
        return new ProductDTO(
                p.id,
                p.name,
                p.category,
                p.price,
                p.viewCount,
                p.createdAt.toString(),
                encodeCursor(p));
    }

    private String encodeCursor(Product p) {
        return p.viewCount + ":" + p.id;
    }

    @POST
    @Path("/seed")
    @Transactional
    public String seed(@QueryParam("count") @DefaultValue("10000") int count) {

        repository.deleteAll();

        for (int i = 0; i < count; i++) {
            Product p = new Product();
            p.name = "Product " + i;
            p.category = i % 2 == 0 ? "Electronics" : "Books";
            p.price = 10.0 + (i % 100);
            p.viewCount = (int) (Math.random() * 10_000);
            p.createdAt = Instant.now().minusSeconds(i * 60);
            p.persist();
        }

        return "Seeded " + count + " products";
    }
}