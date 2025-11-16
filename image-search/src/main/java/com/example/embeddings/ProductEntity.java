package com.example.embeddings;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class ProductEntity extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    @JsonIgnore
    public byte[] imageData;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public String metadata;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
