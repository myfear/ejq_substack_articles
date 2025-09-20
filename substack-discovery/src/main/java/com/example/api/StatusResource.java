package com.example.api;

import com.example.StartupService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/status")
public class StatusResource {

    @Inject
    StartupService startupService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public StatusResponse getStatus() {
        return new StatusResponse(
            startupService.isProcessing(),
            startupService.getProcessedCount(),
            startupService.getTotalCount()
        );
    }

    public static class StatusResponse {
        public boolean processing;
        public int processed;
        public int total;
        public double progress;

        public StatusResponse(boolean processing, int processed, int total) {
            this.processing = processing;
            this.processed = processed;
            this.total = total;
            this.progress = total > 0 ? (double) processed / total * 100 : 0;
        }
    }
}
