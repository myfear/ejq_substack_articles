package com.example;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BusinessErrorMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        ProblemDetail problem = new ProblemDetail();
        problem.type = "about:blank";
        problem.title = "Business Error";
        problem.status = exception.getResponse().getStatus();
        problem.detail = exception.getMessage();

        return Response.status(problem.status)
                .entity(problem)
                .build();
    }
}