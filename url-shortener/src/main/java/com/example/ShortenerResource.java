package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/api")
public class ShortenerResource {

    @Inject
    ShortenerService service;

    @POST
    @Path("/shorten")
    public Response shorten(String url) {
        ShortLink link = service.createShortLink(url);
        return Response.ok(link.key).build();
    }

    @GET
    @Path("/{key}")
    public Response redirect(@PathParam("key") String key) {
        return service.getOriginalUrl(key)
                .map(url -> Response.status(Response.Status.FOUND).location(URI.create(url)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}