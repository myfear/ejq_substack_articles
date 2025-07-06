package org.acme;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.runtime.Startup;

@ApplicationScoped
@Startup
public class DeadlockSimulator {

    private final Object a = new Object();
    private final Object b = new Object();

    @PostConstruct
    void init() {
        new Thread(() -> {
            synchronized (a) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                synchronized (b) {
                }
            }
        }, "deadlock-1").start();

        new Thread(() -> {
            synchronized (b) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                synchronized (a) {
                }
            }
        }, "deadlock-2").start();
    }
}