package com.example.api.v2;

import java.util.UUID;

import com.example.api.v2.dto.OrderRequestV2;
import com.example.api.v2.dto.OrderResponseV2;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v2/orders")
public class OrderResourceV2 {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrder(@Valid OrderRequestV2 request) {

        OrderResponseV2 response = new OrderResponseV2();
        response.orderId = UUID.randomUUID().toString();
        response.product = request.product;
        response.quantity = request.quantity;
        response.customerId = request.customerId;
        response.status = "NEW";

        return Response.ok(response).build();
    }
}
