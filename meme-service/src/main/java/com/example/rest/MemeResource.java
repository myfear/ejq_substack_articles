package com.example.rest;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.model.Meme;
import com.example.service.MemeService;
import com.example.service.MemeStore;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/memes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MemeResource {

    @Inject
    MemeService service;
    @Inject
    MemeStore store;

    @POST
    public Response create(@Valid Meme m, @Context UriInfo uri) {
        Meme saved = service.create(m);
        URI location = uri.getAbsolutePathBuilder().path(saved.getId().toString()).build();
        return Response.created(location).entity(saved).build();
    }

    @GET
    public List<Meme> list(@QueryParam("tag") String tag) {
        return store.list(Optional.ofNullable(tag));
    }

    @GET
    @Path("/random")
    public Response random(@QueryParam("tag") String tag) {
        return store.random(Optional.ofNullable(tag))
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return store.find(id)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }
}