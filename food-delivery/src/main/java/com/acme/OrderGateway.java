package com.acme;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.acme.model.Order;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderGateway {

    @Channel("orders")
    Emitter<Order> orderEmitter;

    private volatile boolean tapOpen = false;

    public void openTap() {
        tapOpen = true;
    }

    public void closeTap() {
        tapOpen = false;
        orderEmitter.complete(); // important: closes the stream
    }

    public Uni<Void> submitOrder(Order order) {
        if (!tapOpen) {
            return Uni.createFrom().voidItem();
        }
        return Uni.createFrom().completionStage(orderEmitter.send(order));
    }
}
