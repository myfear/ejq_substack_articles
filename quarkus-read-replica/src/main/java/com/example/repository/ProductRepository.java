package com.example.repository;

import java.math.BigDecimal;
import java.util.List;

import com.example.entity.Product;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    public List<Product> findByCategory(String category) {
        return find("category", category).list();
    }

    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return find("price BETWEEN ?1 AND ?2", minPrice, maxPrice).list();
    }

    public long countForRead() {
        return count();
    }

    public Product save(Product product) {
        if (product.id == null) {
            persist(product);
        } else {
            product = getEntityManager().merge(product);
        }
        return product;
    }

    public void deleteProduct(Long id) {
        deleteById(id);
    }

    public Product updateStock(Long productId, Integer newStock) {
        Product product = findById(productId);
        if (product != null) {
            product.stockQuantity = newStock;
            product.preUpdate();
            return product;
        }
        return null;
    }
}