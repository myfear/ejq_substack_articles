package com.example;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource {

    @GET
    public List<Event> list() {
        return Event.listAll();
    }

    @POST
    @Transactional
    public Event create(Event event) {
        event.persist();
        return event;
    }

    @GET
    @Path("/{id}")
    public Event get(@PathParam("id") Long id) {
        return Event.findById(id);
    }

    @GET
    @Path("/upcoming")
    public List<Event> upcoming() {
        return Event.list("startTime > ?1", OffsetDateTime.now());
    }

    @GET
    @Path("/{id}/convert")
    public Event convert(@PathParam("id") Long id, @jakarta.ws.rs.QueryParam("zoneId") String zoneId) {
        Event event = Event.findById(id);
        if (event == null) {
            throw new jakarta.ws.rs.NotFoundException("Event with id " + id + " not found");
        }

        // Convert the OffsetDateTime to the specified timezone (same instant, different
        // offset)
        OffsetDateTime convertedStartTime = event.startTime
                .atZoneSameInstant(java.time.ZoneId.of(zoneId))
                .toOffsetDateTime();

        // Create a new Event with the converted startTime
        Event convertedEvent = new Event();
        convertedEvent.id = event.id;
        convertedEvent.title = event.title;
        convertedEvent.description = event.description;
        convertedEvent.startTime = convertedStartTime;

        return convertedEvent;
    }

}