package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public class GreetingResource {

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus!";
    }

    @GET
    @Path("/compute")
    @Produces(MediaType.APPLICATION_JSON)
    public ComputeResult compute(@QueryParam("iterations") Integer iterations) {
        int iter = iterations != null ? iterations : 1000;

        // Simulate realistic computational work
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < iter; i++) {
            numbers.add(i);
        }

        // Perform transformations
        List<Integer> processed = numbers.stream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * n)
                .collect(Collectors.toList());

        long sum = processed.stream()
                .mapToLong(Integer::longValue)
                .sum();

        return new ComputeResult(processed.size(), sum);
    }

    public static class ComputeResult {
        public int count;
        public long sum;

        public ComputeResult(int count, long sum) {
            this.count = count;
            this.sum = sum;
        }
    }
}