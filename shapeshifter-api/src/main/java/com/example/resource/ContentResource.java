package com.example.resource;

import java.util.List;

import com.example.entity.Content;
import com.example.json.Views;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/content")
@Produces(MediaType.APPLICATION_JSON)
public class ContentResource {

    @Inject
    ObjectMapper objectMapper;

    @GET
    public Response listContent(@QueryParam("role") String role) {
        List<Content> contentList = Content.listAll();
        Class<?> viewClass = determineView(role);
        return serializeWithView(contentList, viewClass);
    }

    @GET
    @Path("/{id}")
    public Response getContent(@PathParam("id") Long id,
            @QueryParam("role") String role) {
        Content content = Content.findById(id);
        if (content == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Content not found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        Class<?> viewClass = determineView(role);
        return serializeWithView(content, viewClass);
    }

    private Response serializeWithView(Object data, Class<?> viewClass) {
        try {
            String json = objectMapper
                    .writerWithView(viewClass)
                    .writeValueAsString(data);
            return Response.ok(json, MediaType.APPLICATION_JSON).build();
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Failed to serialize content\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    private Class<?> determineView(String role) {
        if (role == null) {
            return Views.Public.class;
        }

        return switch (role.toLowerCase()) {
            case "subscriber" -> Views.Subscriber.class;
            case "admin" -> Views.Admin.class;
            default -> Views.Public.class;
        };
    }

}