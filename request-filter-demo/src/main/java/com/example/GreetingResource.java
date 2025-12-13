package com.example;

import com.example.filter.Audited;
import com.example.filter.Sanitized;
import com.example.filter.Traced;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/hello")
public class GreetingResource {

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Audited
    @Traced
    @Path("/create")
    public Response create(String json) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(json);
        return Response.ok(objectMapper.createObjectNode()
                .put("message", "Received")
                .set("data", jsonNode)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Sanitized
    @Traced
    @Path("/register")
    public Response register(String json) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(json);
        return Response.ok(objectMapper.createObjectNode()
                .put("message", "User created")
                .set("data", jsonNode)).build();
    }

}