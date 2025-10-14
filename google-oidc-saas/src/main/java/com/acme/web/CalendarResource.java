package com.acme.web;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.acme.google.CalendarClient;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/calendar")
public class CalendarResource {

    @Inject
    @RestClient
    CalendarClient calendar;

    @GET
    @Path("/upcoming")
    @Produces(MediaType.APPLICATION_JSON)
    public String upcoming() {

        String timeMin = OffsetDateTime.now().minusMinutes(1).toString();
        return calendar.listPrimaryEvents(5, true, "startTime", timeMin);
    }
}