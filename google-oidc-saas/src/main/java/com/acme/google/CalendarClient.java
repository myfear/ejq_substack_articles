package com.acme.google;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/calendar/v3")
@RegisterRestClient(configKey = "google-calendar")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@AccessToken
// @RegisterProvider(GoogleAccessTokenFilter.class)
public interface CalendarClient {

    @GET
    @Path("/calendars/primary/events")
    String listPrimaryEvents(@QueryParam("maxResults") @DefaultValue("5") int max,
            @QueryParam("singleEvents") @DefaultValue("true") boolean singleEvents,
            @QueryParam("orderBy") @DefaultValue("startTime") String orderBy,
            @QueryParam("timeMin") String timeMinIso);
}