package com.example;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/memory")
public class MemoryResource {

    public record MemoryStats(
            String status,
            long jvmMaxHeapMB,
            long jvmUsedHeapMB,
            String hostContainerOS,
            List<String> jvmArguments
    ) {}

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public MemoryStats getMemoryStats() {
        return gatherMemoryStats();
    }

    private MemoryStats gatherMemoryStats() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);

        List<String> jvmArgs = ManagementFactory
                .getRuntimeMXBean()
                .getInputArguments();
        Set<String> uniqueJvmArgs = new LinkedHashSet<>(jvmArgs);
        List<String> jvmArgumentsList = uniqueJvmArgs.stream().toList();

        return new MemoryStats(
                "JAZ Status Report",
                heapMax,
                heapUsed,
                System.getProperty("os.name"),
                jvmArgumentsList
        );
    }
}