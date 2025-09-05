package com.acme.order;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderService service;

    public record CreateOrderRequest(String item, int qty) {
    }

    @POST
    public Response create(CreateOrderRequest req) {
        if (req == null || req.item() == null || req.item().isBlank() || req.qty() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("invalid order").build();
        }
        return Response.ok(service.create(req.item(), req.qty())).build();
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") String id) {
        Order o = service.get(id);
        if (o == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(o).build();
    }

    @POST
    @Path("{id}/complete")
    public Response complete(@PathParam("id") String id) {
        Order o = service.complete(id);
        if (o == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(o).build();
    }
}
