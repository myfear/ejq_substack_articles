package org.acme.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProcessingService {

    public CompletionStage<String> processData(String data) {
        return CompletableFuture.supplyAsync(() -> {
            Log.infof("Starting to process '%s' on thread %s", data, Thread.currentThread().getName());
            try {
                // Simulate a long-running task
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Log.infof("Finished processing '%s'", data);
            return "Processed: " + data;
        });
    }
}