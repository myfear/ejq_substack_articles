package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/weather")
public class WeatherResource {

    @Inject
    WeatherService service;

    @GET
    @Path("/{city}")
    @Produces(MediaType.APPLICATION_JSON)
    public Forecast forecast(@PathParam("city") String city) {
        return new Forecast(city, service.getDailyForecast(city));
    }

    @DELETE
    @Path("/{city}")
    public void deleteCache(@PathParam("city") String city) {
        service.invalidateForecast(city);
    }

    public record Forecast(String city, String forecast) {
    }
}
