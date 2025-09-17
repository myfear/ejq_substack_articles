package com.acme.weather;

import java.time.LocalDateTime;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkiverse.mcp.server.McpLog;
// MCP server APIs
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class WeatherTools {

    @Inject
    @RestClient
    WeatherClient client;

    @Inject
    @RestClient
    GeocodingClient geocodingClient;

    @Tool(name = "getTemperature", description = "Get temperature in Celsius for a coordinate pair")
    @RunOnVirtualThread
    public String getTemperature(
            @ToolArg(description = "Latitude") double latitude,
            @ToolArg(description = "Longitude") double longitude,
            McpLog log) {

        log.info("Invoking getTemperature lat=%s lon=%s", latitude, longitude);

        WeatherResponse resp = client.forecast(latitude, longitude, "temperature_2m");

        String out = "Temperature at (%s,%s): %s°C"
                .formatted(latitude, longitude, resp.current().temperature_2m());

        return out;
    }

    @Tool(name = "findCityCoordinates", description = "Find latitude and longitude coordinates for a city name")
    @RunOnVirtualThread
    public String findCityCoordinates(
            @ToolArg(description = "City name to search for") String cityName,
            McpLog log) {

        log.info("Searching for coordinates of city: %s", cityName);

        GeocodingResponse response = geocodingClient.search(cityName, 10, "en", "json");

        if (response.results().length == 0) {
            return "No cities found with name: " + cityName;
        }

        StringBuilder result = new StringBuilder();
        result.append("Found ").append(response.results().length).append(" cities matching '").append(cityName).append("':\n\n");

        for (int i = 0; i < Math.min(response.results().length, 5); i++) {
            GeocodingResponse.GeocodingResult city = response.results()[i];
            result.append(String.format("%d. %s, %s\n", 
                i + 1, 
                city.name(), 
                city.country()));
            result.append(String.format("   Coordinates: %.4f, %.4f\n", 
                city.latitude(), 
                city.longitude()));
            result.append(String.format("   Population: %d\n", city.population()));
            result.append(String.format("   Timezone: %s\n\n", city.timezone()));
        }

        return result.toString();
    }

    public record WeatherResponse(Current current) {
        public record Current(LocalDateTime time, int interval, double temperature_2m) {
        }
    }

    public record GeocodingResponse(GeocodingResult[] results, double generationtime_ms) {
        public record GeocodingResult(int id, String name, double latitude, double longitude, 
                double elevation, String feature_code, String country_code, int admin1_id, 
                int admin3_id, int admin4_id, String timezone, int population, String[] postcodes, 
                int country_id, String country, String admin1, String admin3, String admin4) {
        }
    }

}