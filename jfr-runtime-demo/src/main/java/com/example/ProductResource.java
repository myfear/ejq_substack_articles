package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource for managing products with simulated work delays.
 */
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    private static final Random random = new Random();
    private static final List<Product> products = new ArrayList<>();

    static {
        products.add(new Product(1L, "Laptop", 999.99));
        products.add(new Product(2L, "Mouse", 29.99));
        products.add(new Product(3L, "Keyboard", 79.99));
    }

    /**
     * Lists all products.
     */
    @GET
    public List<Product> listAll() {
        simulateWork(50, 150);
        return products;
    }

    /**
     * Gets a product by ID.
     */
    @GET
    @Path("/{id}")
    public Product getById(@PathParam("id") Long id) {
        simulateWork(20, 80);
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    /**
     * Creates a new product.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Product create(Product product) {
        simulateWork(100, 300);
        product.setId((long) (products.size() + 1));
        products.add(product);
        return product;
    }

    /**
     * Processes a batch of products.
     */
    @POST
    @Path("/batch")
    public Response processBatch(List<Product> batch) {
        batch.forEach(this::create);
        BusinessEvent.record("BATCH_IMPORT", "user123", batch.size());
        return Response.ok().build();
    }

    /**
     * Simulates work with random delay.
     */
    private void simulateWork(int min, int max) {
        try {
            Thread.sleep(random.nextInt(max - min) + min);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static class Product {
        public Long id;
        public String name;
        public Double price;

        public Product() {
        }

        public Product(Long id, String name, Double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}