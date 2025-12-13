package com.acme;

import com.acme.mnist.MnistService;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

@Path("/api/mnist")
public class MnistResource {

    @Inject
    MnistService service;

    public static class Request {
        public float[] pixels; // Efficient mapping
    }

    public static class BenchmarkResult {
        public int iterations;
        public int warmupIterations;
        public double averageMs;
        public double medianMs;
        public double minMs;
        public double maxMs;
        public double stdDevMs;
        public double p95Ms;
        public double p99Ms;
        public double throughputPerSec;
        public int predictedDigit;
        public float confidence;
        
        public BenchmarkResult(int iterations, int warmupIterations, List<Double> times,
                              int digit, float confidence) {
            this.iterations = iterations;
            this.warmupIterations = warmupIterations;
            this.predictedDigit = digit;
            this.confidence = confidence;
            
            // Calculate statistics
            this.averageMs = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            this.minMs = times.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            this.maxMs = times.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            
            // Median
            List<Double> sorted = new ArrayList<>(times);
            sorted.sort(Double::compareTo);
            int middle = sorted.size() / 2;
            this.medianMs = sorted.size() % 2 == 0
                ? (sorted.get(middle - 1) + sorted.get(middle)) / 2.0
                : sorted.get(middle);
            
            // Standard deviation
            double variance = times.stream()
                .mapToDouble(t -> Math.pow(t - averageMs, 2))
                .average()
                .orElse(0.0);
            this.stdDevMs = Math.sqrt(variance);
            
            // P95 and P99
            int p95Index = (int) Math.ceil(sorted.size() * 0.95) - 1;
            int p99Index = (int) Math.ceil(sorted.size() * 0.99) - 1;
            this.p95Ms = sorted.get(Math.max(0, Math.min(p95Index, sorted.size() - 1)));
            this.p99Ms = sorted.get(Math.max(0, Math.min(p99Index, sorted.size() - 1)));
            
            // Throughput
            this.throughputPerSec = 1000.0 / averageMs;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MnistService.Prediction classify(Request req) {
        Log.infof("Classifying request: %s", req.toString());
        if (req.pixels == null || req.pixels.length != 784) {
            throw new IllegalArgumentException("Input must be 784 float pixels");
        }
        return service.predict(req.pixels);
    }

    @POST
    @Path("/benchmark")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public BenchmarkResult benchmark(Request req,
                                    @QueryParam("iterations") Integer iterations,
                                    @QueryParam("warmup") Integer warmup) {
        if (req.pixels == null || req.pixels.length != 784) {
            throw new IllegalArgumentException("Input must be 784 float pixels");
        }
        
        int numIterations = iterations != null ? iterations : 100;
        int numWarmup = warmup != null ? warmup : 10;
        
        Log.infof("Running benchmark: %d iterations with %d warmup", numIterations, numWarmup);
        
        // Warmup
        for (int i = 0; i < numWarmup; i++) {
            service.predict(req.pixels);
        }
        
        // Benchmark
        List<Double> times = new ArrayList<>();
        int digit = 0;
        float confidence = 0.0f;
        
        for (int i = 0; i < numIterations; i++) {
            long start = System.nanoTime();
            MnistService.Prediction prediction = service.predict(req.pixels);
            long duration = System.nanoTime() - start;
            
            times.add(duration / 1_000_000.0); // Convert to ms
            digit = prediction.digit();
            confidence = prediction.confidence();
        }
        
        return new BenchmarkResult(numIterations, numWarmup, times, digit, confidence);
    }

    @GET
    @Path("/benchmark/info")
    @Produces(MediaType.APPLICATION_JSON)
    public String benchmarkInfo() {
        return """
            {
              "endpoint": "/api/mnist/benchmark",
              "method": "POST",
              "description": "Run server-side benchmark with multiple iterations",
              "parameters": {
                "iterations": "Number of benchmark iterations (default: 100)",
                "warmup": "Number of warmup iterations (default: 10)"
              },
              "example": "POST /api/mnist/benchmark?iterations=100&warmup=10",
              "body": {
                "pixels": "Array of 784 float values (28x28 image)"
              }
            }
            """;
    }
}
