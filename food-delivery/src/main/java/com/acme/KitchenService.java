package com.acme;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.acme.model.KitchenTicket;
import com.acme.model.Order;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KitchenService {

    @Incoming("orders")
    @Outgoing("kitchen")
    public KitchenTicket prepare(Order order) {
        Log.infof("Kitchen preparing: %s", order.item());
        return new KitchenTicket(order.id(), order.item(), "PREPARED", order.price());
    }
}