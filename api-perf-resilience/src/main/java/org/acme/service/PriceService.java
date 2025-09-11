package org.acme.service;

import java.util.Random;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PriceService {

    private static final Logger LOG = Logger.getLogger(PriceService.class);
    private final Random rnd = new Random();

    // Simulate unstable downstream dependency
    private String callRemote(String productId) throws InterruptedException {
        LOG.infof("Calling remote service for product %s", productId);
        // random latency 100..800 ms
        long delay = 100 + rnd.nextInt(700);
        Thread.sleep(delay);

        // 30% chance of failure
        if (rnd.nextDouble() < 0.3) {
            LOG.warnf("Remote service failed for product %s", productId);
            throw new RuntimeException("Downstream error");
        }
        String price = switch (productId) {
            case "1" -> "9.99";
            case "2" -> "399.00";
            default -> "0.00";
        };
        LOG.infof("Remote service returned price %s for product %s", price, productId);
        return price;
    }

    @Timeout(500) // ms: bound the latency
    @Retry(maxRetries = 2, delay = 200) // quick retries for transient failures
    @CircuitBreaker(requestVolumeThreshold = 6, // sliding window size
            failureRatio = 0.5, // open if >50% fail
            delay = 2000 // ms open interval
    )
    @Fallback(fallbackMethod = "fallbackPrice")
    public String price(String productId) throws InterruptedException {
        LOG.infof("Attempting to get price for product %s", productId);
        try {
            String result = callRemote(productId);
            LOG.infof("Successfully got price %s for product %s", result, productId);
            return result;
        } catch (Exception e) {
            LOG.warnf("Failed to get price for product %s: %s", productId, e.getMessage());
            throw e;
        }
    }

    public String fallbackPrice(String productId) {
        LOG.warnf("Using fallback price for product %s - circuit breaker likely open", productId);
        // conservative default; in real apps return last-known-good or tiered default
        return "0.00";
    }
}