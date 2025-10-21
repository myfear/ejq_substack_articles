package com.example;

import java.time.LocalDateTime;

import com.example.model.Product;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/products")
public class ProductResource {

    @GET
    public Product getProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setDescription(null);
        product.setCreatedAt(LocalDateTime.of(2024, 10, 15, 14, 30));
        return product;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Product createProduct(Product product) {
        return product;
    }

}