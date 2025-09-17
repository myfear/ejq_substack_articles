package com.acme.weather;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "open-meteo")
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface WeatherClient {

    @GET
    @Path("/forecast")
    WeatherTools.WeatherResponse forecast(@QueryParam("latitude") double lat,
            @QueryParam("longitude") double lon,
            @QueryParam("current") @DefaultValue("temperature_2m") String current);
}