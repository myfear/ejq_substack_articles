package org.acme;

import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SimulationService {

    private static final Logger log = Logger.getLogger(SimulationService.class);

    @Inject
    EmailGeneratorService generator;
    @Inject
    EmailProcessor processor;
    @Inject
    TodoService todoService;

    // This method will run every 10 seconds
    @Scheduled(every = "15s", delayed = "5s") // optional delay before first run
    public void runSimulation() {
        log.info("--- Email Simulation Start ---");

        String email = generator.generateEmail("Next Quarkus Release");
        log.infof("Generated Email:\n%s", email);

        String reply = processor.processEmail(email);
        log.infof("AI Response: %s", reply);

        log.infof("Todo List: %s", todoService.getTasks());
        log.info("--- Email Simulation End ---");
    }
}