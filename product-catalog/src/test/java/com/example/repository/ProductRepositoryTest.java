package com.example.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.entity.Product;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ProductRepositoryTest {

    @Inject
    ProductRepository productRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        productRepository.deleteAll();

        for (int i = 1; i <= 10; i++) {
            Product product = new Product();
            product.name = "Product " + i;
            product.description = "Description for product " + i;
            product.category = "electronics";
            product.price = 100.0 + i;
            product.viewCount = i * 10; // 10, 20, ..., 100
            product.createdAt = Instant.now();
            productRepository.persist(product);
        }
    }

    @Test
    public void testPagination() {
        // Expected order is viewCount DESC, so: 100, 90, 80, ...

        // Page 1: Top 3 items (values 100, 90, 80)
        List<Product> page1 = productRepository.findByPopularity("electronics", null, 3);
        assertEquals(3, page1.size());
        assertEquals(100, page1.get(0).viewCount);
        assertEquals(90, page1.get(1).viewCount);
        assertEquals(80, page1.get(2).viewCount);

        // Get cursor from last item
        Product lastItemPage1 = page1.get(2);
        String cursor = lastItemPage1.viewCount + ":" + lastItemPage1.id;

        // Page 2: Next 3 items (values 70, 60, 50)
        List<Product> page2 = productRepository.findByPopularity("electronics", cursor, 3);
        assertEquals(3, page2.size());
        assertEquals(70, page2.get(0).viewCount);
        assertEquals(60, page2.get(1).viewCount);
        assertEquals(50, page2.get(2).viewCount);

        // Verify we can go deeper
        Product lastItemPage2 = page2.get(2);
        cursor = lastItemPage2.viewCount + ":" + lastItemPage2.id;

        List<Product> page3 = productRepository.findByPopularity("electronics", cursor, 4);
        assertEquals(4, page3.size());
        assertEquals(40, page3.get(0).viewCount);
        assertEquals(30, page3.get(1).viewCount);
        assertEquals(20, page3.get(2).viewCount);
        assertEquals(10, page3.get(3).viewCount);
    }
}
