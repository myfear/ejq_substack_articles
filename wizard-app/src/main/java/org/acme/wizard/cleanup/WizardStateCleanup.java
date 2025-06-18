package org.acme.wizard.cleanup;

import java.time.Instant;

import org.acme.wizard.store.WizardStateRepository;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WizardStateCleanup {

    @Inject
    WizardStateRepository wizardStateRepository;

    // Run every 30 minutes to clean up expired wizard states
    @Scheduled(every = "30m")
    @WithTransaction
    public Uni<Void> deleteExpiredWizardStates() {
        Instant now = Instant.now();
        // Panache delete method takes a query string and parameters
        return wizardStateRepository.delete("expiresAt <= ?1", now)
            .invoke(deletedCount -> {
                if (deletedCount > 0) {
                    Log.infof("Cleaned up %d expired wizard states.", deletedCount);
                }
            })
            .replaceWithVoid();
    }
}