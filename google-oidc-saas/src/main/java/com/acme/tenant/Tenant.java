package com.acme.tenant;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Tenant extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public String domain;

    public static Tenant getOrCreate(String domain) {
        Tenant t = find("domain", domain).firstResult();
        if (t == null) {
            t = new Tenant();
            t.domain = domain.toLowerCase();
            t.persist();
        }
        return t;
    }
}