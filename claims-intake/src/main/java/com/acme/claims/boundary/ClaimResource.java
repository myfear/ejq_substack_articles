package com.acme.claims.boundary;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.acme.claims.events.ClaimSubmitted;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/claims")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ClaimResource {

    @Channel("claims-submitted")
    Emitter<ClaimSubmitted> emitter;

    @POST
    public Response submit(ClaimSubmitted claim) {
        Log.infof("📥 [SUBMITTED] eventId=%s, claimId=%s, customerId=%s, amount=%.2f", 
                claim.eventId(), claim.claimId(), claim.customerId(), claim.amount());
        emitter.send(claim);
        return Response.accepted().entity(claim).build();
    }
}