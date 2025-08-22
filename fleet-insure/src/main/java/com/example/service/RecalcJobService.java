package com.example.service;

import java.time.OffsetDateTime;
import java.util.List;

import com.example.domain.RecalcJob;

import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RecalcJobService {

    @Inject
    PremiumCalculator calc;

    // In a real system use a queue. For the tutorial, a simple poller is enough.

    @Scheduled(every = "10s")
    @Transactional
    void runJobs() {
        List<RecalcJob> queued = RecalcJob.find("status", RecalcJob.Status.QUEUED).list();
        for (var job : queued) {
            job.status = RecalcJob.Status.RUNNING;
            try {
                // Call calc in a new tx for isolation
                Panache.getEntityManager().flush();
                // Use CDI to obtain PremiumCalculator
                // PremiumCalculator calc =
                // io.quarkus.arc.Arc.container().instance(PremiumCalculator.class).get();
                calc.recalc(job.policyId, job.trigger + " [async]", java.time.LocalDate.now());
                job.status = RecalcJob.Status.DONE;
                job.message = "Completed";
            } catch (Exception e) {
                job.status = RecalcJob.Status.FAILED;
                job.message = e.getMessage();
            }
            job.finishedAt = OffsetDateTime.now();
        }
    }
}