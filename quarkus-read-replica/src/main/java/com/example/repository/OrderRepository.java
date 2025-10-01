package com.example.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.example.entity.Order;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {

    public List<Order> findByStatus(String status) {
        return find("status", status).list();
    }

    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return find("createdAt BETWEEN ?1 AND ?2", startDate, endDate).list();
    }

    public Order save(Order order) {
        if (order.id == null) {
            persist(order);
        } else {
            order = getEntityManager().merge(order);
        }
        return order;
    }

    public Order updateStatus(Long orderId, String newStatus) {
        Order order = findById(orderId);
        if (order != null) {
            order.status = newStatus;
            order.preUpdate();
            return order;
        }
        return null;
    }
}