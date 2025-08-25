package com.example;

import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;

@Entity
public class Transaction extends PanacheEntity {
    // A Set is better than a List here since item order doesn't matter
    // and items are unique within a transaction.
    @ElementCollection(fetch = FetchType.EAGER)
    public Set<String> items;
}
