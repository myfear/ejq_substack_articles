package com.example.service;

import java.util.List;
import java.util.Optional;

import com.example.domain.Customer;
import com.example.repository.CustomerRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CustomerService {

    @Inject
    CustomerRepository repository;

    public Customer createCustomer(Customer customer) {
        validate(customer);
        return repository.save(customer);
    }

    public Optional<Customer> getCustomer(Long id) {
        return repository.findById(id);
    }

    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    public void deleteCustomer(Long id) {
        repository.deleteById(id);
    }

    private void validate(Customer customer) {
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (customer.getEmail() == null || !customer.getEmail().contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
    }
}