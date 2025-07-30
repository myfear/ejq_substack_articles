package com.example.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;

@Entity
@NamedEntityGraph(name = "Car.withBrandAndDealership", attributeNodes = {
        @NamedAttributeNode("brand"),
        @NamedAttributeNode("dealership")
})
public class Car extends PanacheEntity {

    public String model;

    @ManyToOne(fetch = FetchType.LAZY)
    public Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    public Dealership dealership;

    public Integer productionYear;
    public String color;
    public BigDecimal price;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "car_features", joinColumns = @JoinColumn(name = "car_id"))
    @Column(name = "feature")
    public Set<String> features = new HashSet<>();
}