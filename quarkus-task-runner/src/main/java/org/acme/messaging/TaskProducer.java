package org.acme.messaging;

import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TaskProducer {

    @Outgoing("task-requests")
    public Multi<String> generate() {
        // Every 15 seconds, send a new task request
        return Multi.createFrom().ticks().every(Duration.ofSeconds(15))
                .map(tick -> "Task payload " + tick);
    }
}