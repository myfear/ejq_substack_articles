package org.acme.api;

import org.acme.service.PriceService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/prices")
@Produces(MediaType.TEXT_PLAIN)
public class PriceResource {

    @Inject
    PriceService svc;

    @GET
    @Path("{id}")
    public String get(@PathParam("id") String id) throws InterruptedException {
        return svc.price(id);
    }
}