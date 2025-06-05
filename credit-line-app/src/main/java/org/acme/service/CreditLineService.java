package org.acme.service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.acme.model.CreditLine;
import org.acme.model.CreditLineState;
import org.jboss.logging.Logger;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@ApplicationScoped
public class CreditLineService {

    private static final Logger LOG = Logger.getLogger(CreditLineService.class);

    @Inject
    ReactiveMailer mailer;

    @Inject
    Template welcomeEmail; // Injected Qute template

    private final Random random = new Random();

    @Transactional
    public CreditLine initiateCreditLine(String customerName, String customerEmail, double requestedAmount) {
        CreditLine creditLine = new CreditLine(customerName, customerEmail, requestedAmount);
        creditLine.state = CreditLineState.PENDING_APPROVAL;
        creditLine.persist();
        LOG.infof("Credit line initiated for customer: %s with amount: %.2f. State: %s",
                customerName, requestedAmount, creditLine.state);
        return creditLine;
    }

    @Transactional
    public void processApproval(Long creditLineId) {
        CreditLine creditLine = CreditLine.findById(creditLineId);
        if (creditLine == null) {
            LOG.warnf("Credit Line with ID %d not found for approval processing.", creditLineId);
            return;
        }

        if (creditLine.state != CreditLineState.PENDING_APPROVAL) {
            LOG.warnf("Credit Line ID %d is not in PENDING_APPROVAL state. Current state: %s",
                    creditLineId, creditLine.state);
            return;
        }

        // Simulate backend approval
        boolean approved = random.nextBoolean(); // 50/50 chance of approval

        if (approved) {
            creditLine.state = CreditLineState.APPROVED;
            creditLine.approvalTimestamp = LocalDateTime.now();
            creditLine.lastUpdatedTimestamp = LocalDateTime.now();
            creditLine.persist();
            LOG.infof("Credit Line ID %d approved. State: %s", creditLineId, creditLine.state);

            // Now send the email, and update state in a non-blocking way if needed
            sendWelcomeEmail(creditLine).subscribe().with(
                    unused -> {
                        // Run the blocking state update on a worker thread
                        Uni.createFrom().voidItem()
                                .runSubscriptionOn(
                                        io.smallrye.mutiny.infrastructure.Infrastructure.getDefaultWorkerPool())
                                .subscribe().with(
                                        ignored -> updateCreditLineState(creditLine.id, CreditLineState.EMAIL_SENT));
                    },
                    failure -> {
                        Uni.createFrom().voidItem()
                                .runSubscriptionOn(
                                        io.smallrye.mutiny.infrastructure.Infrastructure.getDefaultWorkerPool())
                                .subscribe().with(
                                        ignored -> updateCreditLineState(creditLine.id, CreditLineState.ERROR));
                        LOG.errorf(failure, "Failed to send welcome email for Credit Line ID %d", creditLine.id);
                    });
        } else {
            creditLine.state = CreditLineState.REJECTED;
            creditLine.lastUpdatedTimestamp = LocalDateTime.now();
            creditLine.persist();
            LOG.warnf("Credit Line ID %d rejected. State: %s", creditLineId, creditLine.state);
        }
    }

    public Uni<Void> sendWelcomeEmail(CreditLine creditLine) {
        DecimalFormat df = new DecimalFormat("###.##");
        String formattedAmount = df.format(creditLine.requestedAmount);

        TemplateInstance emailContent = welcomeEmail
                .data("customerName", creditLine.customerName)
                .data("requestedAmount", formattedAmount);

        return mailer
                .send(Mail.withHtml(creditLine.customerEmail, "Welcome to Our Credit Line Service!",
                        emailContent.render()));
    }

    @Transactional
    public Optional<CreditLine> findById(Long id) {
        return Optional.ofNullable(CreditLine.findById(id));
    }

    // Since you are calling updateCreditLineState from a new thread, ensure it
    // starts a new transaction:
    @Transactional(TxType.REQUIRES_NEW)
    public void updateCreditLineState(Long id, CreditLineState newState) {
        CreditLine creditLine = CreditLine.findById(id);
        if (creditLine == null) {
            LOG.warnf("Credit Line with ID %d not found for state update.", id);
            return;
        }

        // Only update if state is actually changing
        if (creditLine.state != newState) {
            creditLine.state = newState;
            creditLine.lastUpdatedTimestamp = LocalDateTime.now();
            if (newState == CreditLineState.EMAIL_SENT) {
                creditLine.emailSentTimestamp = LocalDateTime.now();
            }
            creditLine.persist();
            LOG.infof("Credit Line ID %d state updated to %s", id, newState);
        } else {
            LOG.debugf("Credit Line ID %d already in state %s, no update performed.", id, newState);
        }
    }
}