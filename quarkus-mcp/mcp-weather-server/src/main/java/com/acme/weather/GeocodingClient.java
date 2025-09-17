package com.acme.weather;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "geocoding")
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface GeocodingClient {

    @GET
    @Path("/search")
    WeatherTools.GeocodingResponse search(@QueryParam("name") String name,
            @QueryParam("count") @DefaultValue("10") int count,
            @QueryParam("language") @DefaultValue("en") String language,
            @QueryParam("format") @DefaultValue("json") String format);
}
