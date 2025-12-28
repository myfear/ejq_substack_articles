package com.acme.claims.boundary;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.acme.claims.events.ClaimSubmitted;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Path("/claims")
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ClaimResource {

    @Channel("claims-submitted-out")
    Emitter<ClaimSubmitted> emitter;

    @POST
    public void submit(ClaimSubmitted claim) {
        emitter.send(claim);
    }
}