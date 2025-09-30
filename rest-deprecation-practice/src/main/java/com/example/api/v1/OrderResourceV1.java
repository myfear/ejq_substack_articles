package com.example.api.v1;

import org.jboss.logging.Logger;

import com.example.api.v1.dto.OrderRequestV1;
import com.example.api.v1.dto.OrderResponseV1;

import io.micrometer.core.annotation.Counted;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/orders")
public class OrderResourceV1 {

    private static final Logger LOG = Logger.getLogger(OrderResourceV1.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Counted(value = "deprecated_orders_v1_requests_total", description = "Number of calls to deprecated/api/v1/orders endpoint")

    public Response createOrder(@Valid OrderRequestV1 request,
            @Context HttpHeaders headers) {

        String clientId = headers.getHeaderString("X-Client-Id");
        LOG.warnf("Deprecated v1 order endpoint called by client: %s", clientId);

        OrderResponseV1 response = new OrderResponseV1();
        response.orderId = "ORD-123";
        response.product = request.product;
        response.quantity = request.quantity;

        return Response.ok(response)
                .header("Deprecation", "true")
                .header("Sunset", "Wed, 30 Sep 2026 00:00:00 GMT")
                .build();
    }
}
