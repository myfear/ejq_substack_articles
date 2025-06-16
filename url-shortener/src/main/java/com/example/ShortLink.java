package com.example;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class ShortLink extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String key;

    @Column(nullable = false, length = 2048)
    public String originalUrl;

    public static ShortLink findByKey(String key) {
        return find("key", key).firstResult();
    }
}