package com.example.chirper;

import java.net.URI;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class ChirperResource {

    @Inject
    Template index;
    @Inject
    Template profile;
    @Inject
    ChirpService chirpService;
    @Inject
    UserService userService;

    @GET
    public TemplateInstance home() {
        return index.data("chirps", chirpService.getAllChirps());
    }

    @POST
    @Path("/chirp")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postChirp(@FormParam("username") String username,
            @FormParam("content") String content) {
        var user = userService.getOrCreateUser(username);
        chirpService.createChirp(user, content);
        return Response.seeOther(URI.create("/")).build();
    }

    @POST
    @Path("/like/{chirpId}")
    public Response like(@PathParam("chirpId") Long chirpId) {
        chirpService.likeChirp(chirpId);
        return Response.seeOther(URI.create("/")).build();
    }

    @GET
    @Path("/profile/{username}")
    public TemplateInstance profile(@PathParam("username") String username) {
        var user = userService.findByUsername(username);
        if (user == null)
            throw new WebApplicationException(404);
        return profile.data("user", user)
                .data("chirps", chirpService.getChirpsByUser(user));
    }
}
