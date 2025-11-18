package org.acme.spy;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/agent")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AgentResource {

    @Inject
    SecretAgentKeyService keyService;
    @Inject
    AgentRegistry registry;

    public static class EnrollRequest {
        public String codeName;
        public String email;
        public String passphrase;
    }

    @POST
    @Path("/enroll")
    public Response enroll(EnrollRequest req) {
        AgentEnrollmentResponse keys = keyService.generateAgentKeys(
                req.codeName,
                req.email,
                req.passphrase.toCharArray());

        // Store public key only
        registry.enroll(new Agent(req.codeName, req.email, keys.publicKeyArmored));

        // Return full keypair only once
        return Response.status(Response.Status.CREATED)
                .entity(keys)
                .build();
    }
}
