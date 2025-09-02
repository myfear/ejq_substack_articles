package com.example;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import io.quarkus.logging.Log;
import io.smallrye.faulttolerance.api.ExponentialBackoff;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AiSummarizer {

    @Inject
    Assistant assistant;
    @Inject
    FetchService fetch;

    // Apply practical, layered safeguards:
    @Timeout(value = 10000) // fast abort on slow model calls
    @Retry(maxRetries = 2, delay = 200) // transient hiccups
    @ExponentialBackoff // multiplicative backoff (SmallRye)
    @CircuitBreaker(requestVolumeThreshold = 8, failureRatio = 0.5, delay = 10000, // ms
            successThreshold = 2)
    @Bulkhead(value = 8) // cap parallelism
    @RateLimit(value = 5) // 5 req / second (SmallRye)
    @Fallback(fallbackMethod = "cachedSummary")
    public String summarize(String url) throws Exception {
        Log.info("FetchService:");
        String text = fetch.fetch(url);
        // Keep the prompt compact; we rely on model defaults for length.
        return assistant.summarize(text);
    }

    // Fallback must match signature + return type
    String cachedSummary(String url) {
        // Minimal local fallback: deterministic stub or a tiny cached value.
        // In real life, use a proper cache or last-known-good store.
        return "Service is busy. Here is a safe fallback: "
                + "Unable to summarize right now for " + url + ". Try again shortly.";
    }
}