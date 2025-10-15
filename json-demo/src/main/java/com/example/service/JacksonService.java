package com.example.service;

import java.util.Map;

import com.example.model.ProductJ;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JacksonService {
    @Inject
    ObjectMapper mapper;

    public String toJson(ProductJ product) throws Exception {
        return mapper.writeValueAsString(product);
    }

    public ProductJ fromJson(String json) throws Exception {
        return mapper.readValue(json, ProductJ.class);
    }

    public Map<String, Object> toMap(ProductJ product) {
        return mapper.convertValue(product, new TypeReference<Map<String, Object>>() {
        });
    }
}