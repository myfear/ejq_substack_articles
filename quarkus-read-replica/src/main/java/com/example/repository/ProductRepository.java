package com.example.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.entity.Product;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    @PersistenceContext(unitName = "replica")
    EntityManager replicaEntityManager;

    // Read operations using replica database
    public List<Product> findAllForRead() {
        return replicaEntityManager.createQuery("SELECT p FROM Product p", Product.class).getResultList();
    }

    public Optional<Product> findByIdForRead(Long id) {
        return findByIdOptional(id);
    }

    public List<Product> findByCategoryForRead(String category) {
        return find("category", category).list();
    }

    public List<Product> findByPriceRangeForRead(BigDecimal minPrice, BigDecimal maxPrice) {
        return find("price BETWEEN ?1 AND ?2", minPrice, maxPrice).list();
    }

    public long countForRead() {
        return count();
    }

    // Write operations using Panache (primary database)
    @Transactional
    public Product save(Product product) {
        if (product.id == null) {
            persist(product);
        } else {
            product = getEntityManager().merge(product);
        }
        return product;
    }

    @Transactional
    public void deleteProduct(Long id) {
        deleteById(id);
    }

    @Transactional
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
