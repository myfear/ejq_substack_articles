package com.example.model;

import java.time.OffsetDateTime;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "post")
public class PostEntity extends PanacheEntity {
    
    @Column(length = 1000)  // AT URIs can be long
    public String uri;
    
    @Column(length = 10000)  // Bluesky posts can be up to 300 characters, but allow for longer content
    public String text;
    
    public OffsetDateTime createdAt;
    public int hourOfDay;
    
    @Column(length = 1000)  // Multiple hashtags separated by commas
    public String hashtags;
    
    @Column(length = 2000)  // Multiple URLs separated by commas
    public String links;
    
    @Column(length = 500)   // Framework names separated by commas
    public String frameworks;
    
    @Column(length = 50)    // Language codes are short
    public String language;
    
    public OffsetDateTime indexedAt;
}
