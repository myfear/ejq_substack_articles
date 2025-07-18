package com.example;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/messages")
public class MessageResource {

    private static final Logger LOG = Logger.getLogger(MessageResource.class);

    @Inject
    CryptoService cryptoService;

    @POST
    @Path("/secret")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SecretMessage handleSecret(SecretMessage message) {
        LOG.infof("Received and decrypted message: '%s'", message.message);
        return message;
    }

    @GET
    @Path("/encrypt/{text}")
    @Produces(MediaType.TEXT_PLAIN)
    public String encryptForTesting(@PathParam("text") String text) throws Exception {
        return cryptoService.encrypt(text);
    }
}