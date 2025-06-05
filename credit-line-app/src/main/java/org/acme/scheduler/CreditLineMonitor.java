package org.acme.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.acme.model.CreditLine;
import org.acme.model.CreditLineState;
import org.acme.service.CreditLineService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreditLineMonitor {

    private static final Logger LOG = Logger.getLogger(CreditLineMonitor.class);

    @Inject
    CreditLineService creditLineService;

    // Configurable time frame for checks
    // Default to 1 hour if not specified in application.properties
    @ConfigProperty(name = "creditline.check.timeout.minutes", defaultValue = "60")
    long checkTimeoutMinutes;

    /**
     * Checks for credit lines stuck in PENDING_APPROVAL and attempts to re-process
     * approval.
     * Runs every 5 minutes.
     */
    @Scheduled(every = "5m")
    @Transactional
    public void checkPendingApprovals() {
        LOG.debug("Running scheduled check for pending approvals...");
        LocalDateTime timeoutThreshold = LocalDateTime.now().minus(checkTimeoutMinutes, ChronoUnit.MINUTES);

        List<CreditLine> pendingApprovals = CreditLine.find("state = ?1 and creationTimestamp <= ?2",
                CreditLineState.PENDING_APPROVAL, timeoutThreshold).list();

        if (!pendingApprovals.isEmpty()) {
            LOG.warnf("Found %d credit lines stuck in PENDING_APPROVAL state older than %d minutes.",
                    pendingApprovals.size(), checkTimeoutMinutes);
        }

        for (CreditLine creditLine : pendingApprovals) {
            LOG.infof("Attempting to re-process approval for Credit Line ID: %d (Customer: %s)",
                    creditLine.id, creditLine.customerName);
            creditLineService.processApproval(creditLine.id);
        }
    }

    /**
     * Checks for credit lines stuck in APPROVED state (meaning email wasn't sent)
     * and attempts to resend the email.
     * Runs every 10 minutes.
     */
    @Scheduled(every = "10m")
    @Transactional
    public void checkApprovedWithoutEmail() {
        LOG.debug("Running scheduled check for approved credit lines without email...");
        LocalDateTime timeoutThreshold = LocalDateTime.now().minus(checkTimeoutMinutes, ChronoUnit.MINUTES);

        List<CreditLine> approvedWithoutEmail = CreditLine.find("state = ?1 and approvalTimestamp <= ?2",
                CreditLineState.APPROVED, timeoutThreshold).list();

        if (!approvedWithoutEmail.isEmpty()) {
            LOG.warnf("Found %d credit lines stuck in APPROVED state (email not sent) older than %d minutes.",
                    approvedWithoutEmail.size(), checkTimeoutMinutes);
        }

        for (CreditLine creditLine : approvedWithoutEmail) {
            LOG.infof("Attempting to re-send welcome email for Credit Line ID: %d (Customer: %s)",
                    creditLine.id, creditLine.customerName);
            creditLineService.sendWelcomeEmail(creditLine).subscribe().with(
                    success -> LOG.debugf("Email re-sent for Credit Line ID: %d", creditLine.id),
                    failure -> LOG.errorf(failure, "Failed to re-send email for Credit Line ID: %d", creditLine.id));
        }
    }
}
