package com.support;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/tickets")
public class TicketResource {

    // Emitter to send new ticket IDs to our Kafka topic
    @Inject
    @Channel("ticket-processing-out")
    Emitter<Long> ticketEmitter;

    @POST
    @Transactional
    public Response createTicket(SupportTicket ticket) {
        ticket.persist();
        ticketEmitter.send(ticket.id);
        return Response.status(Response.Status.CREATED).entity(ticket).build();
    }
}
