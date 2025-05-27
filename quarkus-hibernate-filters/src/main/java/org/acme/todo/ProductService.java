package org.acme.todo;

import java.util.List;

import org.hibernate.Session;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProductService {

    @Inject EntityManager entityManager;

    @Transactional
    public void addProduct(Product product) {
        product.persist();
    }

    public List<Product> getActiveProducts() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("deletedProductFilter").setParameter("isDeleted", false);
        List<Product> list = Product.listAll();
        session.disableFilter("deletedProductFilter");
        return list;
    }

    public List<Product> getDeletedProducts() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("deletedProductFilter").setParameter("isDeleted", true);
        List<Product> list = Product.listAll();
        session.disableFilter("deletedProductFilter");
        return list;
    }

    public List<Product> getAllProductsIncludingDeleted() {
        return Product.listAll();
    }

    @Transactional
    public boolean softDeleteProduct(Long id) {
        Product p = Product.findById(id);
        if (p != null) {
            p.deleted = true;
            p.persist();
            return true;
        }
        return false;
    }

    @Transactional
    public boolean restoreProduct(Long id) {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("deletedProductFilter").setParameter("isDeleted", true);
        Product p = Product.findById(id);
        session.disableFilter("deletedProductFilter");
        if (p != null && p.deleted) {
            p.deleted = false;
            p.persist();
            return true;
        }
        return false;
    }

    @Transactional
    public Product findById(Long id, boolean includeDeleted) {
        Session session = entityManager.unwrap(Session.class);
        if (!includeDeleted)
            session.enableFilter("deletedProductFilter").setParameter("isDeleted", false);
        Product product = Product.findById(id);
        if (!includeDeleted)
            session.disableFilter("deletedProductFilter");
        return product;
    }
}