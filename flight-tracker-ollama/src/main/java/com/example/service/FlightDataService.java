package com.example.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import com.example.client.AdsbFiClient;
import com.example.model.Aircraft;
import com.example.model.FlightDataResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FlightDataService {
    
    private static final Logger LOG = Logger.getLogger(FlightDataService.class);
    
    @Inject
    @RestClient
    AdsbFiClient adsbFiClient;
    
    public CompletionStage<List<Aircraft>> getAircraftByLocation(double lat, double lon, int distanceNm) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.infof("Fetching aircraft near lat=%f, lon=%f, distance=%d NM", lat, lon, distanceNm);
                FlightDataResponse response = adsbFiClient.getAircraftByLocation(lat, lon, distanceNm);
                return response.getAircraft() != null ? response.getAircraft() : Collections.emptyList();
            } catch (Exception e) {
                LOG.errorf(e, "Error fetching aircraft by location");
                return Collections.emptyList();
            }
        });
    }
    
    public CompletionStage<List<Aircraft>> getMilitaryAircraft() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("Fetching military aircraft");
                FlightDataResponse response = adsbFiClient.getMilitaryAircraft();
                return response.getAircraft() != null ? response.getAircraft() : Collections.emptyList();
            } catch (Exception e) {
                LOG.errorf(e, "Error fetching military aircraft");
                return Collections.emptyList();
            }
        });
    }
    
    public CompletionStage<List<Aircraft>> getAircraftByCallsign(String callsign) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.infof("Fetching aircraft with callsign: %s", callsign);
                FlightDataResponse response = adsbFiClient.getAircraftByCallsign(callsign);
                return response.getAircraft() != null ? response.getAircraft() : Collections.emptyList();
            } catch (Exception e) {
                LOG.errorf(e, "Error fetching aircraft by callsign: %s", callsign);
                return Collections.emptyList();
            }
        });
    }
    
    public CompletionStage<List<Aircraft>> getEmergencyAircraft() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("Fetching emergency aircraft (squawk 7700)");
                FlightDataResponse response = adsbFiClient.getAircraftBySquawk("7700");
                return response.getAircraft() != null ? response.getAircraft() : Collections.emptyList();
            } catch (Exception e) {
                LOG.errorf(e, "Error fetching emergency aircraft");
                return Collections.emptyList();
            }
        });
    }
}