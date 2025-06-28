package org.acme.resources;

import org.acme.services.ProcessingService;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.NonBlocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/async")
public class AsyncResource {

    @Inject
    ProcessingService processingService;

    @GET
    @Path("/process")
    @Produces(MediaType.TEXT_PLAIN)
    @NonBlocking
    public String triggerAsyncProcessing() {
        String taskData = "user-data-" + System.currentTimeMillis();
        Log.infof("Endpoint called. Triggering background task for: %s" + taskData);
        processingService.processData(taskData); // Fire-and-forget
        return "Task for '" + taskData + "' has been submitted for processing in the background!";
    }
}