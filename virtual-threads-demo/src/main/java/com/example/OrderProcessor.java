package com.example;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderProcessor {

    @Incoming("orders")
    @RunOnVirtualThread
    public void process(String orderJson) {
        System.out.println("Order processed by: " + Thread.currentThread());
    }
}