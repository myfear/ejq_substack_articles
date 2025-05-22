package com.example;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuditService {

 private static final Logger LOG = Logger.getLogger(AuditService.class);

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void logAuditEvent(String event) {
        LOG.info("AUDIT LOG (New Transaction): " + event);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void logAuditEventInSameTransaction(String event) {
        LOG.info("AUDIT LOG (Same Transaction): " + event);
    }
}
