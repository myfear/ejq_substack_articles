package com.example.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "validation");
        List<Map<String, String>> violations = new ArrayList<>();
        for (ConstraintViolation<?> v : e.getConstraintViolations()) {
            violations.add(Map.of(
                    "path", v.getPropertyPath().toString(),
                    "message", v.getMessage()));
        }
        body.put("violations", violations);
        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }
}