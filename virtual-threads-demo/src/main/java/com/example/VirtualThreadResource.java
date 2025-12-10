package com.example;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/demo")
public class VirtualThreadResource {

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    @RunOnVirtualThread
    public String hello() {
        return "Hello from: " + Thread.currentThread();
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> info() {
        var t = Thread.currentThread();
        return Map.of(
                "thread", t.toString(),
                "is_virtual", t.isVirtual(),
                "active_threads", Thread.activeCount());
    }

    @Inject
    DatabaseSimulator db;

    @GET
    @Path("/query")
    @Produces(MediaType.TEXT_PLAIN)
    @RunOnVirtualThread
    public String query() {
        long start = System.currentTimeMillis();
        String result = db.queryDatabase("SELECT * FROM users");
        long duration = System.currentTimeMillis() - start;

        return "Thread: " + Thread.currentThread() +
                "\nResult: " + result +
                "\nDuration: " + duration + "ms";
    }

    @GET
    @Path("/multiple-queries")
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public List<String> multipleQueries() {
        return java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> db.queryDatabase("Query " + i))
                .toList();
    }

    @GET
    @Path("/parallel/{count}")
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public Map<String, Object> parallel(@PathParam("count") int count) {
        long start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            var futures = java.util.stream.IntStream.range(0, count)
                    .mapToObj(i -> executor.submit(() -> db.slowQuery(1000)))
                    .toList();

            var results = futures.stream()
                    .map(f -> {
                        try {
                            return f.get();
                        } catch (Exception e) {
                            return "Error: " + e.getMessage();
                        }
                    })
                    .toList();

            long duration = System.currentTimeMillis() - start;
            return Map.of("count", count, "duration_ms", duration, "results", results);
        }
    }

    @GET
    @Path("/load-test/{n}")
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public Map<String, Object> load(@PathParam("n") int n) {
        long start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            var tasks = java.util.stream.IntStream.range(0, n)
                    .mapToObj(i -> executor.submit(() -> {
                        db.slowQuery(100);
                        return i;
                    })).toList();

            long done = tasks.stream().filter(f -> {
                try {
                    f.get();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }).count();

            long duration = System.currentTimeMillis() - start;

            return Map.of(
                    "requested", n,
                    "completed", done,
                    "duration_ms", duration,
                    "rps", (n * 1000.0) / duration);
        }
    }

}