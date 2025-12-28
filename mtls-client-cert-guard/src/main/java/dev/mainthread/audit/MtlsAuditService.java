package dev.mainthread.audit;

import java.time.Instant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MtlsAuditService {

    @Transactional
    public void recordAccepted(String fingerprint, String principal) {
        MtlsAuditEntry entry = new MtlsAuditEntry();
        entry.fingerprint = fingerprint;
        entry.principal = principal;
        entry.acceptedAt = Instant.now();
        entry.persist();
    }
}