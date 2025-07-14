package com.support;

import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.support.SupportTicket.TicketStatus;

import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TicketProcessingService {

    @Inject
    TicketClassifier ticketClassifier;
    @Inject
    SolutionRecommendationService solutionRecommendationService;
    @Inject
    TicketDashboardWebSocket dashboardWebSocket;

    /**
     * This method consumes messages from the "ticket-processing-in" channel (our
     * Kafka topic).
     * 
     * @Blocking ensures this runs on a worker thread, as AI calls can be slow.
     */
    @Incoming("ticket-processing-out")
    @Transactional
    @Blocking
    @ActivateRequestContext
    public void processTicket(Long ticketId) {
        Log.infof("Received ticket ID %d for processing.", ticketId);

        // 1. Fetch the ticket from the DB
        SupportTicket ticket = SupportTicket.findById(ticketId);
        if (ticket == null) {
            Log.errorf("Ticket with ID %d not found!", ticketId);
            return;
        }

        // 2. AI Classification Step
        updateStatusAndBroadcast(ticket, TicketStatus.AI_CLASSIFICATION, "Classifying ticket...");
        TicketClassification classification = ticketClassifier.classify(ticket.subject, ticket.description);
        Log.infof("Ticket %d classified as: %s", ticketId, classification);
        // In a full app, you would persist this classification data.

        // 3. Solution Lookup Step
        updateStatusAndBroadcast(ticket, TicketStatus.SOLUTION_LOOKUP, "Searching knowledge base...");
        List<KnowledgeBaseArticle> suggestions = solutionRecommendationService.findSimilarSolutions(ticket.description);
        if (suggestions.isEmpty()) {
            Log.infof("No relevant solutions found for ticket %d.", ticketId);
        } else {
            Log.infof("Found %d potential solutions for ticket %d.", suggestions.size(), ticketId);
            // Here you could auto-respond or attach suggestions for the agent.
        }

        // 4. Final Step: Assign to agent
        updateStatusAndBroadcast(ticket, TicketStatus.AGENT_ASSIGNED, "Ticket assigned to an agent queue.");
        Log.infof("Ticket %d processing complete. Final status: %s", ticketId, ticket.status);
    }

    private void updateStatusAndBroadcast(SupportTicket ticket, TicketStatus newStatus, String logMessage) {
        ticket.status = newStatus;
        ticket.persistAndFlush(); // Ensure change is committed before broadcasting
        Log.info(logMessage);
        dashboardWebSocket.broadcast(String.format("Ticket #%d: %s", ticket.id, logMessage));
    }
}
