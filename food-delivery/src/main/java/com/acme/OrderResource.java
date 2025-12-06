package com.acme;

import com.acme.model.Order;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/orders")
public class OrderResource {

    @Inject
    OrderGateway gateway;

    @POST
    public void newOrder(Order order) {
        gateway.submitOrder(order)
                .subscribe().with(unused -> {
                }, Throwable::printStackTrace);
    }

    @POST
    @Path("/open")
    public void open() {
        gateway.openTap();
    }

    @POST
    @Path("/close")
    public void close() {
        gateway.closeTap();
    }
}