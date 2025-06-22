package com.secretagent;

import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MaintenanceScheduler {

    private static final Logger LOG = Logger.getLogger(MaintenanceScheduler.class);

    @Inject
    ApiKeyService apiKeyService;

    @Scheduled(every = "1h") // Every hour, like a Swiss watch
    void cleanupExpiredKeys() {
        LOG.info("🧹 Starting expired key cleanup mission...");
        apiKeyService.deactivateExpiredKeys();
        LOG.info("Expired key cleanup completed!");
    }

    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    void dailyReport() {
        LOG.info("📊 Daily intelligence report:");
        var activeKeys = apiKeyService.getAllActiveKeys();
        var totalUsage = activeKeys.stream().mapToLong(key -> key.usageCount).sum();
        LOG.infof("Active keys: %d, Total usage: %d", activeKeys.size(), totalUsage);
    }
}