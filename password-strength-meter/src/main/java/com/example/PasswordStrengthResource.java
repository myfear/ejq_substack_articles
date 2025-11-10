package com.example;

import java.util.Map;

import com.example.service.PasswordStrengthService;
import com.example.util.PasswordGenerator;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/password")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PasswordStrengthResource {

    @Inject
    PasswordStrengthService service;

    @POST
    @Path("/evaluate")
    public Map<String, Object> evaluate(Map<String, String> body) {
        String password = body.get("password");
        return service.evaluate(password);
    }

    @GET    
    @Path("/generate")
    public Map<String, String> generate(@QueryParam("len") @DefaultValue("12") int len) {
        return Map.of("password", PasswordGenerator.generate(len));
    }
}