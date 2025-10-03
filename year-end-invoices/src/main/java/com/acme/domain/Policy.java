package com.acme.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {
        @Index(name = "idx_policy_active_to", columnList = "validTo"),
        @Index(name = "idx_policy_region", columnList = "bundesland")
})
public class Policy extends PanacheEntity {

    @ManyToOne(optional = false)
    public Customer customer;
    @ManyToOne(optional = false)
    public Vehicle vehicle;

    public String coverage;
    public String bundesland;
    public LocalDate validFrom;
    public LocalDate validTo;
    public BigDecimal baseAnnualPremium;
    public boolean cancelled;

}
