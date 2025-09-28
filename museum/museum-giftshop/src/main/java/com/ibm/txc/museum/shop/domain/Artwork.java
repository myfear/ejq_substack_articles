package com.ibm.txc.museum.shop.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "artwork", indexes = {
        @Index(name = "idx_artwork_name", columnList = "name", unique = true)
})
public class Artwork extends PanacheEntity {
    @Column(nullable = false, unique = true)
    public String name;
    @Column(nullable = false, unique = true)
    public String code;

    public static Artwork findOrCreateByName(String name) {
        Artwork a = find("name = ?1", name).firstResult();
        if (a == null) {
            a = new Artwork();
            a.name = name;
            a.persist();
        }
        return a;
    }
}
