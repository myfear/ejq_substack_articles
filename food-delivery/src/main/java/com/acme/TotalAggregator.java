package com.acme;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.acme.model.KitchenTicket;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TotalAggregator {

    @Incoming("kitchen")
    @Outgoing("totals")
    public Multi<Double> process(Multi<KitchenTicket> tickets) {

        return tickets
                .collect().asList()
                .toMulti()
                .map(list -> list.stream().mapToDouble(KitchenTicket::price).sum());
    }
}