package com.mainthread.apikey.keys;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "api_keys", uniqueConstraints = {
        @UniqueConstraint(name = "uk_api_keys_key_id", columnNames = "keyId")
})
public class ApiKeyEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Long id;

    /**
     * Public identifier used in logs and admin operations.
     * Not secret, but unique and stable across rotations if you choose to keep it.
     */
    @Column(nullable = false, updatable = false, length = 64)
    public String keyId;

    /**
     * Hash of the secret part (never store plaintext).
     */
    @Column(nullable = false, length = 256)
    public String keyHash;

    @Column(nullable = false)
    public boolean active = true;

    @Column(nullable = false, updatable = false)
    public Instant createdAt = Instant.now();

    @Column(nullable = false)
    public Instant lastUsedAt = Instant.EPOCH;

    /**
     * Feature-level access expressed as roles.
     * Example: "catalog:read", "catalog:write", "pricing:read".
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_key_features", joinColumns = @JoinColumn(name = "api_key_fk"))
    @Column(name = "feature", nullable = false, length = 80)
    public Set<String> features = new HashSet<>();
}