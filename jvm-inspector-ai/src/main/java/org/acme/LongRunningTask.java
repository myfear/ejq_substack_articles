package org.acme;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.runtime.Startup;

@ApplicationScoped
@Startup
public class LongRunningTask {
    @PostConstruct
    void run() {
        new Thread(() -> {
            try {
                Thread.sleep(300_000);
            } catch (InterruptedException ignored) {}
        }, "sleeping-thread").start();
    }
}
