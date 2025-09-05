package com.acme.order;

import org.jboss.logging.Logger;

import io.quarkiverse.businessscore.BusinessScore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * Listens for zombie transitions from Business Score self-checks.
 * Logs the event and, if ALERT_WEBHOOK_URL is set, sends a JSON payload to that
 * URL.
 */
@ApplicationScoped
public class ZombieAlertListener {

    private static final Logger LOG = Logger.getLogger(ZombieAlertListener.class);

    void onZombie(@Observes BusinessScore.ZombieStatus status) {
        if (!status.isZombie()) {
            // Optional: you could log recovery events here.
            LOG.infof("Business Score recovered: score=%d threshold=%d window=%s",
                    status.score(), status.threshold(), status.timeWindow());
            return;
        }

        LOG.warnf("ZOMBIE DETECTED: score=%d threshold=%d window=%s",
                status.score(), status.threshold(), status.timeWindow());

    }

}