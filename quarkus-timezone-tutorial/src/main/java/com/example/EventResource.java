package com.example;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource {

    @GET
    public List<Event> listAll() {
        return Event.listAll();
    }

    @POST
    @Transactional
    public Event create(Event event) {
        event.persist();
        return event;
    }

    @GET
    @Path("/local")
    public List<Event> listAllInTimezone(@HeaderParam("X-Timezone") String timezone) {
        List<Event> events = Event.listAll();

        if (timezone == null || timezone.isEmpty()) {
            return events; // Default to UTC
        }

        ZoneId zoneId = ZoneId.of(timezone);

        return events.stream()
                .peek(event -> {
                    ZonedDateTime zonedDateTime = event.eventTimestamp.atZoneSameInstant(zoneId);
                    event.eventTimestamp = zonedDateTime.toOffsetDateTime();
                })
                .collect(Collectors.toList());
    }

}