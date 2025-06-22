package com.secretagent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "api_keys")
public class ApiKey extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String keyValue;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String owner;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    @Column
    public LocalDateTime lastUsed;

    @Column(nullable = false)
    public Boolean active = true;

    @Column(nullable = false)
    public Long usageCount = 0L;

    @Column
    public LocalDateTime expiresAt;

    @Column
    public String permissions; // JSON string for simplicity

    // Custom finder methods - because every agent needs their tools
    public static Optional<ApiKey> findByKeyValue(String keyValue) {
        return find("keyValue", keyValue).firstResultOptional();
    }

    public static List<ApiKey> findByOwner(String owner) {
        return find("owner", owner).list();
    }

    public static List<ApiKey> findActiveKeys() {
        return find("active", true).list();
    }

    public static List<ApiKey> findExpiredKeys() {
        return find("expiresAt < ?1 and active = true", LocalDateTime.now()).list();
    }

    // The secret sauce - increment usage
    public void recordUsage() {
        this.usageCount++;
        this.lastUsed = LocalDateTime.now();
        this.persist();
    }

    // Check if this key is still valid
    public boolean isValid() {
        return active && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
}