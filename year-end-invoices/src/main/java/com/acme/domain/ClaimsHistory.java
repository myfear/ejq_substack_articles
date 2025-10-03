package com.acme.domain;

import java.time.LocalDate;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class ClaimsHistory extends PanacheEntity {
    @ManyToOne(optional = false)
    public Customer customer;

    public int yearsNoClaim; // simplified input to compute SF-Klasse
    public int claimsCountLastYear;

    public LocalDate updatedAt;

}
