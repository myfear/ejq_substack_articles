package com.example.orders.control;

import java.util.List;

import com.example.orders.entity.OrderEntity;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderService implements PanacheRepository<OrderEntity> {

    @Transactional
    public OrderEntity create(String customer, String item, int quantity) {
        OrderEntity e = new OrderEntity(customer, item, quantity);
        persist(e);
        return e;
    }

    public java.util.Optional<OrderEntity> findById(String id) {
        return find("id", id).firstResultOptional();
    }

    public List<OrderEntity> list() {
        return listAll();
    }
}