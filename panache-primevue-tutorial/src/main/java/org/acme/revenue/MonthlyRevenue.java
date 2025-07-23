package org.acme.revenue;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class MonthlyRevenue extends PanacheEntity {
    public String period; // e.g. "2025-01"
    public double revenue;
}