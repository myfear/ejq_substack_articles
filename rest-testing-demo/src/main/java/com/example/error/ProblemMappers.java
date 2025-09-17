package com.example.error;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class ProblemMappers implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable ex) {
        final int status = (ex instanceof NotFoundException) ? 404 : 400;
        final String title = (status == 404) ? "Not Found" : "Bad Request";
        var problem = new Problem("about:blank", title, status, ex.getMessage(), null);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(problem)
                .build();
    }
}