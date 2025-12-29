package com.example.dto;

public record ProductDTO(
        Long id,
        String name,
        String category,
        Double price,
        Integer viewCount,
        String createdAt,
        String cursor) {
}
