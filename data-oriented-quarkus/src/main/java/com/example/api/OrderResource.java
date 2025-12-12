package com.example.api;

import java.util.List;

import com.example.api.OrderResult.InvalidRequest;
import com.example.api.OrderResult.OutOfStock;
import com.example.api.OrderResult.ProductNotFound;
import com.example.api.OrderResult.Success;
import com.example.operations.OrderOperations;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @POST
    @Transactional
    public Response placeOrder(OrderRequest request) {
        return switch (OrderOperations.placeOrder(request)) {
            case Success success ->
                Response.status(Response.Status.CREATED).entity(success).build();

            case OutOfStock outOfStock ->
                Response.status(Response.Status.CONFLICT).entity(outOfStock).build();

            case ProductNotFound notFound ->
                Response.status(Response.Status.NOT_FOUND).entity(notFound).build();

            case InvalidRequest invalid ->
                Response.status(Response.Status.BAD_REQUEST).entity(invalid).build();
        };
    }

    @GET
    public List<OrderView> listOrders() {
        return OrderOperations.getAllOrders();
    }
}