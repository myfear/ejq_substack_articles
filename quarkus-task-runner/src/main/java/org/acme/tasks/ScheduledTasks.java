package org.acme.tasks;

import java.time.LocalDateTime;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScheduledTasks {

    private int counter = 0;

    @Scheduled(every = "10s", identity = "heartbeat-task")
    void heartbeat() {
        counter++;
        Log.infof("[%s] Heartbeat check #%d running on thread: %s",
            LocalDateTime.now(), counter, Thread.currentThread().getName());
    }

    @Scheduled(cron = "0 15 10 * * ?") // Fires at 10:15 AM every day
    void dailyReport() {
        Log.infof("[%s] Generating daily report...", LocalDateTime.now());
    }
}