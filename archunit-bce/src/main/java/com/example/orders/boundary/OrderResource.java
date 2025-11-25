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

/**
 * REST resource for managing orders.
 * 
 * <p>This boundary layer class provides HTTP endpoints for order operations.
 * It follows the BCE (Boundary-Control-Entity) pattern, delegating business
 * logic to the control layer ({@link OrderService}).
 * 
 * <p>All endpoints consume and produce JSON.
 * 
 * @see OrderService
 * @see OrderEntity
 */
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderService service;

    /**
     * Data transfer object for creating a new order.
     * 
     * @param customer the customer name (required)
     * @param item the item name (required)
     * @param quantity the quantity of items (required)
     */
    public record CreateOrder(String customer, String item, int quantity) {
    }

    /**
     * Creates a new order.
     * 
     * <p>Validates the request and delegates to the control layer to create the order.
     * Returns a 201 Created response with the location header set to the new order's URI.
     * 
     * @param req the order creation request containing customer, item, and quantity
     * @return HTTP 201 Created response with the created order entity
     * @throws BadRequestException if the request is null or missing required fields
     */
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

    /**
     * Retrieves all orders.
     * 
     * @return a list of all orders
     */
    @GET
    public List<OrderEntity> list() {
        return service.list();
    }

    /**
     * Retrieves a specific order by its ID.
     * 
     * @param id the order ID
     * @return the order entity
     * @throws NotFoundException if no order exists with the given ID
     */
    @GET
    @Path("{id}")
    public OrderEntity get(@PathParam("id") String id) {
        return service.findById(id).orElseThrow(NotFoundException::new);
    }
}