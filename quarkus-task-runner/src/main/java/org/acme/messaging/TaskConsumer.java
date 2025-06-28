package org.acme.messaging;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TaskConsumer {

    @Incoming("task-requests")
    public CompletionStage<Void> process(String task) {
        return CompletableFuture.runAsync(() -> {
            Log.infof("Reactive consumer received task: '%s'. Processing on thread %s.", task,
                    Thread.currentThread().getName());
            // Simulate processing
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Log.infof("Finished processing reactive task: '%s'", task);
        });
    }
}