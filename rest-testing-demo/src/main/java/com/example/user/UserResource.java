package com.example.user;

import com.example.error.NotFoundException;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/users")
public class UserResource {

    @Inject
    UserRepo repo;
    @Context
    Request request;

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User get(@PathParam("id") long id) {
        return repo.find(id).orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getText(@PathParam("id") long id) {
        var u = get(id);
        return "%s <%s> [%s]".formatted(u.name(), u.email(), u.role());
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("id") long id, @Valid UserCreate in, @Context UriInfo uri) {
        var saved = repo.save(id, in);
        var etag = new EntityTag(Integer.toHexString(saved.hashCode()));
        return Response.created(uri.getAbsolutePath())
                .tag(etag)
                .entity(saved)
                .build();
    }

    @GET
    @Path("/{id}/etag")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWithEtag(@PathParam("id") long id) {
        var u = get(id);
        var etag = new EntityTag(Integer.toHexString(u.hashCode()));
        var pre = request.evaluatePreconditions(etag);
        if (pre != null)
            return pre.build(); // 304 Not Modified
        return Response.ok(u).tag(etag).build();
    }
}