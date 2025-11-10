package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/identicon")
public class IdenticonResource {

    @Inject
    IdenticonService identiconService;

    @GET
    @Produces("image/png")
    public Response get(@QueryParam("input") String input) {
        byte[] img = identiconService.generate(input == null ? "default" : input);
        return Response.ok(img).type(MediaType.valueOf("image/png")).build();
    }
}