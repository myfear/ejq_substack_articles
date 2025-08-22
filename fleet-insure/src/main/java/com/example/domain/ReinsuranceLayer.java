package com.example.domain;

import java.math.BigDecimal;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "re_layer")
public class ReinsuranceLayer extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Long id;

    @Column(nullable = false, unique = true)
    public String name;

    // Layer band edges on total premium
    @Column(nullable = false, precision = 18, scale = 2)
    public BigDecimal lowerBound;

    @Column(nullable = false, precision = 18, scale = 2)
    public BigDecimal upperBound; // inclusive upper or cap. Use large number for "infinity".
}