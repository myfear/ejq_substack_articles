package com.acme.tenant;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class AppUser extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String email;

    public String name;
    public String picture;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    public Tenant tenant;

    public static AppUser upsert(String email, String name, String picture, Tenant tenant) {
        AppUser u = find("email", email.toLowerCase()).firstResult();
        if (u == null) {
            u = new AppUser();
            u.email = email.toLowerCase();
        }
        u.name = name;
        u.picture = picture;
        u.tenant = tenant;
        u.persist();
        return u;
    }
}