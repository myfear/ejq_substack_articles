package com.acme.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(indexes = @Index(name = "idx_invoice_year", columnList = "year"))
public class Invoice extends PanacheEntity {

    @ManyToOne(optional = false)
    public Policy policy;

    public int year;
    public BigDecimal netAmount;
    public BigDecimal insuranceTax;
    public BigDecimal grossAmount;

    public String pdfPath;
    public LocalDate dueDate;
    public String paymentFrequency;
}
