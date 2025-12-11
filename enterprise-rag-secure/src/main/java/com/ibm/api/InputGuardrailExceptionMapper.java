package com.ibm.api;

import dev.langchain4j.guardrail.InputGuardrailException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import io.quarkus.logging.Log;

/**
 * Exception mapper for InputGuardrailException.
 * Maps validation failures from InputValidationGuardrail to structured JSON responses.
 */
@Provider
public class InputGuardrailExceptionMapper implements ExceptionMapper<InputGuardrailException> {

    @Override
    public Response toResponse(InputGuardrailException exception) {
        Log.warn("InputGuardrailException caught: " + exception.getMessage());
        
        // Extract the validation error message from the exception
        String errorMessage = exception.getMessage();
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorMessage = "Input validation failed. Please ensure your question is related to CloudX Enterprise Platform sales enablement.";
        }
        
        // Return the error message in the same BotResponse format for consistency
        BotResponse errorResponse = new BotResponse(errorMessage);
        
        // Return 400 Bad Request with the structured response
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .type("application/json")
                .build();
    }
}