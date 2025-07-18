package com.example;

import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TechnicalErrorMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        Log.errorf("Unexpected technical error: %s", exception.getMessage());

        ProblemDetail problem = new ProblemDetail();
        problem.type = "about:blank";
        problem.title = "Internal Server Error";
        problem.status = 500;
        problem.detail = "An unexpected internal error occurred. Please try again later.";

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(problem)
                .build();
    }
}