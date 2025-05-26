package com.example.pii;

import java.util.UUID;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class MyCustomExceptionHandler implements ExceptionMapper<MyCustomApplicationException> {

    @Inject
    ErrorExplanationService ai;

    private static final Logger LOG = Logger.getLogger(MyCustomExceptionHandler.class);

    @Override
    public Response toResponse(MyCustomApplicationException ex) {
        var errorId = UUID.randomUUID().toString();
        var original = ex.getMessage();

        LOG.errorf(ex, "Error ID [%s]: %s", errorId, original);

        String friendly;
        try {
            friendly = ai.explainError(original, errorId);
        } catch (Exception e) {
            LOG.error("AI failed to generate explanation", e);
            friendly = "An unexpected error occurred. Reference ID: " + errorId;
        }

        return Response.status(500).entity(new ErrorResponse(errorId, friendly))
                .type(MediaType.APPLICATION_JSON).build();
    }

    public record ErrorResponse(String errorId, String userMessage) {
    }
}
