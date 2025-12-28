package dev.mainthread.audit;

import java.time.Instant;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "mtls_audit", indexes = {
        @Index(name = "idx_mtls_audit_fingerprint", columnList = "fingerprint")
})
public class MtlsAuditEntry extends PanacheEntity {

    @Column(nullable = false, length = 128)
    public String fingerprint;

    @Column(nullable = false, length = 512)
    public String principal;

    @Column(nullable = false)
    public Instant acceptedAt;
}