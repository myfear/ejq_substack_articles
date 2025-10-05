package org.acme.order;

import org.acme.validation.ValidationGroups;

import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    @POST
    public Response placeOrder(@Valid @ConvertGroup(to = ValidationGroups.OnCreate.class) Order order) {
        return Response.ok(order).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateOrder(@PathParam("id") Long id,
            @Valid @ConvertGroup(to = ValidationGroups.OnUpdate.class) Order order) {
        return Response.ok(order).build();
    }
}