package com.example.docgen;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Represents a product in the inventory.")
public class Product {
    @Schema(description = "Unique identifier of the product", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    public String id;
    @Schema(description = "Name of the product", example = "Wireless Headphones")
    public String name;
    @Schema(description = "Price of the product", example = "199.99")
    public float price;
    @Schema(description = "Detailed description of the product", nullable = true, example = "High-fidelity audio with noise-cancelling features.")
    public String description;

    public Product() {
    }

    public Product(String id, String name, float price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    // Getters and Setters (omitted for brevity, but needed for Jackson
    // deserialization)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                '}';
    }
}