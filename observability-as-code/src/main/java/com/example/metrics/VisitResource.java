package com.example.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/visit")
public class VisitResource {

    private final MeterRegistry registry;

    public VisitResource(MeterRegistry registry) {
        this.registry = registry;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String visit() {
        registry.counter("app.visits.total").increment();
        return "Visit recorded!";
    }
}