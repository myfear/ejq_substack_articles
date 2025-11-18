package org.acme.spy;

import java.util.Map;

import org.acme.spy.dto.DecryptRequest;
import org.acme.spy.dto.EncryptRequest;
import org.acme.spy.dto.EncryptedMessage;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/message")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SpyMessageResource {

    @Inject
    AgentRegistry registry;
    @Inject
    PgpCryptoService crypto;

    @POST
    @Path("/encrypt")
    public Response encrypt(EncryptRequest req) {
        Agent recipient = registry.find(req.recipient);

        if (recipient == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Unknown agent: " + req.recipient)
                    .build();
        }

        String cipher = crypto.encrypt(
                req.plainText,
                recipient.getPublicKeyArmored());

        EncryptedMessage msg = new EncryptedMessage();
        msg.recipient = req.recipient;
        msg.cipherText = cipher;

        return Response.ok(msg).build();
    }

    @POST
    @Path("/decrypt")
    public Response decrypt(DecryptRequest req) {

        if (req == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request body is required.")
                    .build();
        }

        if (req.cipherText == null || req.cipherText.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing cipherText.")
                    .build();
        }

        if (req.privateKeyArmored == null || req.privateKeyArmored.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing private key.")
                    .build();
        }

        if (req.passphrase == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing passphrase.")
                    .build();
        }

        try {
            String plain = crypto.decrypt(
                    req.cipherText,
                    req.privateKeyArmored,
                    req.passphrase.toCharArray());

            return Response.ok(
                    Map.of(
                            "status", "DECRYPTED",
                            "message", plain,
                            "coolnessLevel", "Jason Bourne",
                            "martiniCount", 3))
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Decryption failed: " + e.getMessage())
                    .build();
        }
    }
}