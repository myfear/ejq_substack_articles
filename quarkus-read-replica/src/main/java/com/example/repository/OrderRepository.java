package com.example.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.entity.Order;
import com.example.tenant.ReadWrite;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {

    // Read operations using Panache (primary database)
    public List<Order> findAllForRead() {
        return findAll().list();
    }

    public Optional<Order> findByIdForRead(Long id) {
        return findByIdOptional(id);
    }

    public List<Order> findByStatusForRead(String status) {
        return find("status", status).list();
    }

    public List<Order> findByDateRangeForRead(LocalDateTime startDate, LocalDateTime endDate) {
        return find("createdAt BETWEEN ?1 AND ?2", startDate, endDate).list();
    }

    // Write operations using Panache (primary database)
    @ReadWrite
    @Transactional
    public Order save(Order order) {
        if (order.id == null) {
            persist(order);
        } else {
            order = getEntityManager().merge(order);
        }
        return order;
    }

    @ReadWrite
    @Transactional
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
