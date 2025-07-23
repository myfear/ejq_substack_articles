package com.example;

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api/transactions")
public class FinancialResource {

    @Inject
    JsonWebToken jwt;

    @POST
    @RolesAllowed("user")
    public Response createTransaction(String transactionData) {
        String user = jwt.getName();
        // Process the transaction here...
        return Response.ok("Transaction processed for user: " + user).build();
    }
}