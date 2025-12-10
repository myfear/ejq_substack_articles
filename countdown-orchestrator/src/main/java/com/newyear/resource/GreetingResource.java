package com.newyear.resource;

import java.util.List;

import com.newyear.dto.GreetingRequest;
import com.newyear.entity.ScheduledGreeting;
import com.newyear.service.GreetingSchedulerService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for scheduling and listing greetings.
 */
@Path("/api/greetings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GreetingResource {

    @Inject
    GreetingSchedulerService scheduler;

    @POST
    public Response scheduleGreeting(GreetingRequest request) {
        if (request == null || request.recipientTimezone == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("recipientTimezone is required")
                    .build();
        }

        ScheduledGreeting greeting = scheduler.scheduleGreeting(request);
        return Response.status(Response.Status.CREATED)
                .entity(greeting)
                .build();
    }

    @GET
    public List<ScheduledGreeting> listGreetings() {
        return ScheduledGreeting.listAll();
    }
}