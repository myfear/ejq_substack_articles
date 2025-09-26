package com.example.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.entity.Product;
import com.example.repository.ProductRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProductService {
    @Inject
    ProductRepository productRepository; // Read operations - using replica

    public List<Product> getAllProducts() {
        return productRepository.findAllForRead();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findByIdForRead(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryForRead(category);
    }

    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceRangeForRead(minPrice, maxPrice);
    }

    public long getProductCount() {
        return productRepository.countForRead();
    } // Write operations - using primary database

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProductStock(Long productId, Integer newStock) {
        return productRepository.updateStock(productId, newStock);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteProduct(id);
    }
}