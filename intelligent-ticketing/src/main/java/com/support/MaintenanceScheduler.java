package com.support;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MaintenanceScheduler {

    @Scheduled(cron = "0 0 4 * * ?") // Run every day at 4 AM
    void archiveOldTickets() {
        Log.info("Running scheduled job: Archiving old resolved tickets...");
        // Add logic here to find tickets with status RESOLVED or CLOSED
        // older than 90 days and archive them.
    }
}
