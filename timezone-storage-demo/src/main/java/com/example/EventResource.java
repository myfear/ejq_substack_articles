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

/**
 * REST resource for managing events with timezone support.
 * <p>
 * This resource provides endpoints for creating, retrieving, and querying events.
 * Events store their start time as an {@link OffsetDateTime} with timezone information
 * preserved in the database.
 * </p>
 *
 * @author Generated
 */
@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource {

    /**
     * Retrieves all events from the database.
     *
     * @return a list of all events
     */
    @GET
    public List<Event> list() {
        return Event.listAll();
    }

    /**
     * Creates a new event and persists it to the database.
     * <p>
     * The event's start time should include timezone information as an {@link OffsetDateTime}.
     * The timezone information will be stored separately in the database.
     * </p>
     *
     * @param event the event to create
     * @return the persisted event with its generated ID
     */
    @POST
    @Transactional
    public Event create(Event event) {
        event.persist();
        return event;
    }

    /**
     * Retrieves a specific event by its ID.
     *
     * @param id the unique identifier of the event
     * @return the event with the specified ID, or {@code null} if not found
     */
    @GET
    @Path("/{id}")
    public Event get(@PathParam("id") Long id) {
        return Event.findById(id);
    }

    /**
     * Retrieves all upcoming events (events with a start time in the future).
     * <p>
     * The comparison is performed using the current time as an {@link OffsetDateTime},
     * ensuring proper timezone-aware comparison.
     * </p>
     *
     * @return a list of events with start times after the current time
     */
    @GET
    @Path("/upcoming")
    public List<Event> upcoming() {
        return Event.list("startTime > ?1", OffsetDateTime.now());
    }

    /**
     * Converts an event's start time to a different timezone.
     * <p>
     * This endpoint retrieves an event by ID and returns a new event object with the
     * start time converted to the specified timezone. The conversion preserves the
     * same instant in time but adjusts the offset to match the requested timezone.
     * </p>
     * <p>
     * The original event in the database is not modified; this endpoint only returns
     * a converted representation.
     * </p>
     *
     * @param id the unique identifier of the event to convert
     * @param zoneId the timezone ID (e.g., "America/New_York", "Europe/London", "UTC")
     *                as defined by the IANA Time Zone Database
     * @return a new event object with the converted start time
     * @throws jakarta.ws.rs.NotFoundException if no event exists with the specified ID
     * @throws java.time.DateTimeException if the zoneId is invalid
     */
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