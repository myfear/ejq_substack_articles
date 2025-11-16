package com.example.embeddings;

public record ProductMatch(
        com.example.embeddings.ProductEntity product,
        double similarityScore) {
}
