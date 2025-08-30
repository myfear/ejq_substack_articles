package org.acme;

import java.util.concurrent.TimeUnit;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WeatherService {

    @CacheResult(cacheName = "weather-cache")
    public String getDailyForecast(String city) {
        // Simulate slow work
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "The weather in " + city + " is sunny. (Timestamp: " + System.currentTimeMillis() + ")";
    }

    @CacheInvalidate(cacheName = "weather-cache")
    public void invalidateForecast(String city) {
        // No body needed – the annotation does the work
    }

}
