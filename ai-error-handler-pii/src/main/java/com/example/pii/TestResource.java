package com.example.pii;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Test REST resource for demonstrating error handling and PII redaction capabilities.
 * <p>
 * This resource provides endpoints for testing the application's error handling mechanism,
 * particularly focusing on scenarios where error messages may contain Personally Identifiable
 * Information (PII) such as names and email addresses.
 * </p>
 */
@Path("/test")
public class TestResource {
    
    /**
     * Triggers an error condition to test error handling and PII redaction.
     * <p>
     * This endpoint allows testing of the custom exception handler and PII redaction
     * mechanisms by optionally throwing an exception that contains user information.
     * When the fail parameter is true, an exception with PII data will be thrown,
     * allowing verification that sensitive information is properly redacted in error responses.
     * </p>
     *
     * @param fail if {@code true}, triggers an exception; if {@code false}, returns success
     * @param name the user name to include in the error message (may contain PII)
     * @param email the user email to include in the error message (contains PII)
     * @return a {@link Response} containing either a success message or an error response
     *         with redacted PII
     * @throws MyCustomApplicationException when fail parameter is true, containing a message
     *         with the provided name and email
     */
    @GET
    @Path("/error")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerError(@QueryParam("fail") boolean fail,
            @QueryParam("name") String name,
            @QueryParam("email") String email) {
        if (fail) {
            throw new MyCustomApplicationException(
                    "Operation failed for user %s (contact: %s). Internal check failed.".formatted(name, email));
        }
        return Response.ok("Operation successful!").build();
    }
}