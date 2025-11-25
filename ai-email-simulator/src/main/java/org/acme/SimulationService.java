package org.acme;

import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Service responsible for orchestrating the email simulation process.
 * <p>
 * This service runs on a scheduled basis and coordinates the generation of emails,
 * processing of those emails, and management of todo tasks. It demonstrates an
 * automated workflow where AI-generated emails are processed and tasks are tracked.
 * </p>
 */
@ApplicationScoped
public class SimulationService {

    private static final Logger log = Logger.getLogger(SimulationService.class);

    /**
     * Service for generating simulated email content.
     */
    @Inject
    EmailGeneratorService generator;
    
    /**
     * Service for processing and responding to emails using AI.
     */
    @Inject
    EmailProcessor processor;
    
    /**
     * Service for managing and retrieving todo tasks.
     */
    @Inject
    TodoService todoService;

    /**
     * Executes the email simulation workflow on a scheduled basis.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Generates a simulated email about a given topic</li>
     *   <li>Processes the email and generates an AI response</li>
     *   <li>Retrieves and logs the current todo list</li>
     * </ol>
     * </p>
     * <p>
     * The simulation runs every 15 seconds with an initial delay of 5 seconds
     * before the first execution.
     * </p>
     */
    @Scheduled(every = "15s", delayed = "5s")
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