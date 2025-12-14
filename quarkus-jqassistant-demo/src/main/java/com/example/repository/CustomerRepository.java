package com.example.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.example.domain.Customer;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerRepository {

    private final ConcurrentHashMap<Long, Customer> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            customer.setId(idGenerator.getAndIncrement());
        }
        store.put(customer.getId(), customer);
        return customer;
    }

    public Optional<Customer> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Customer> findAll() {
        return new ArrayList<>(store.values());
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}