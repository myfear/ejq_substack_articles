package com.example;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class JacksonConfigTest {

    @Inject
    ObjectMapper mapper;

    @Test
    public void testDateFormat() throws Exception {
        Product product = new Product();
        product.setCreatedAt(LocalDateTime.of(2024, 10, 15, 14, 30));
        String json = mapper.writeValueAsString(product);
        assertTrue(json.contains("2024-10-15"));
        assertFalse(json.contains("[2024,"));
    }

    @Test
    public void testNullValuesExcluded() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        String json = mapper.writeValueAsString(product);
        assertFalse(json.contains("null"));
    }

    @Test
    public void testUnknownPropertiesIgnored() throws Exception {
        String json = """
                {"id":1,"name":"Laptop","extra":"ignored"}
                """;
        Product product = mapper.readValue(json, Product.class);
        assertEquals(1L, product.getId());
        assertEquals("Laptop", product.getName());
        assertNull(product.getDescription());
    }
}