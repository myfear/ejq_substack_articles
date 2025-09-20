package com.ibm.txc.museum.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Art extends PanacheEntity {
    @Column(nullable = false, unique = true)
    public String code;
    @Column(nullable = false)
    public String title;
    @Column(nullable = false)
    public String artist;
    @Column(nullable = false)
    public Integer year;
    public String imageUrl;
    public String description;
}