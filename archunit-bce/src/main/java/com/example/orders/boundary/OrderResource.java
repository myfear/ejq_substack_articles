package com.example.orders.boundary;

import java.net.URI;
import java.util.List;

import com.example.orders.control.OrderService;
import com.example.orders.entity.OrderEntity;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
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

    public record CreateOrder(String customer, String item, int quantity) {
    }

    @POST
    public Response create(CreateOrder req) {
        if (req == null || req.customer() == null || req.item() == null) {
            throw new BadRequestException("customer and item required");
        }
        OrderEntity created = service.create(req.customer(), req.item(), req.quantity());
        return Response.created(URI.create("/orders/" + created.getId()))
                .entity(created)
                .build();
    }

    @GET
    public List<OrderEntity> list() {
        return service.list();
    }

    @GET
    @Path("{id}")
    public OrderEntity get(@PathParam("id") String id) {
        return service.findById(id).orElseThrow(NotFoundException::new);
    }
}