package com.example.ai;

import java.util.List;

import com.example.model.Aircraft;
import com.example.service.FlightDataService;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FlightDataTools {
    
    @Inject
    FlightDataService flightDataService;
    
    @Tool("Get aircraft within a specified distance from coordinates")
    public String getAircraftNearLocation(
            double latitude, 
            double longitude, 
            int distanceNauticalMiles) {
        
        try {
            List<Aircraft> aircraft = flightDataService
                    .getAircraftByLocation(latitude, longitude, distanceNauticalMiles)
                    .toCompletableFuture()
                    .get();
            
            if (aircraft.isEmpty()) {
                return "No aircraft found in the specified area.";
            }
            
            return formatAircraftList(aircraft, "Aircraft near location");
        } catch (Exception e) {
            return "Error retrieving aircraft data: " + e.getMessage();
        }
    }
    
    @Tool("Get all military aircraft currently tracked")
    public String getMilitaryAircraft() {
        try {
            List<Aircraft> aircraft = flightDataService
                    .getMilitaryAircraft()
                    .toCompletableFuture()
                    .get();
            
            if (aircraft.isEmpty()) {
                return "No military aircraft currently tracked.";
            }
            
            return formatAircraftList(aircraft, "Military Aircraft");
        } catch (Exception e) {
            return "Error retrieving military aircraft data: " + e.getMessage();
        }
    }
    
    @Tool("Find aircraft by callsign (flight number)")
    public String findAircraftByCallsign(String callsign) {
        try {
            List<Aircraft> aircraft = flightDataService
                    .getAircraftByCallsign(callsign.toUpperCase())
                    .toCompletableFuture()
                    .get();
            
            Log.debugf("callsign", "callsign: %s, found %d aircraft", callsign, aircraft.size());

            if (aircraft.isEmpty()) {
                return "No aircraft found with callsign: " + callsign;
            }
            
            return formatAircraftList(aircraft, "Aircraft with callsign " + callsign);
        } catch (Exception e) {
            return "Error retrieving aircraft data: " + e.getMessage();
        }
    }
    
    @Tool("Get aircraft in emergency status (squawk 7700)")
    public String getEmergencyAircraft() {
        try {
            List<Aircraft> aircraft = flightDataService
                    .getEmergencyAircraft()
                    .toCompletableFuture()
                    .get();
            
            if (aircraft.isEmpty()) {
                return "No aircraft currently in emergency status.";
            }
            
            return formatAircraftList(aircraft, "Emergency Aircraft");
        } catch (Exception e) {
            return "Error retrieving emergency aircraft data: " + e.getMessage();
        }
    }
    
    private String formatAircraftList(List<Aircraft> aircraft, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append(" (").append(aircraft.size()).append(" found):\n\n");
        
        for (Aircraft ac : aircraft) {
            sb.append("• ");
            if (ac.getCallsign() != null && !ac.getCallsign().trim().isEmpty()) {
                sb.append("Flight: ").append(ac.getCallsign().trim()).append(" ");
            }
            if (ac.getRegistration() != null) {
                sb.append("(").append(ac.getRegistration()).append(") ");
            }
            if (ac.getAircraftType() != null) {
                sb.append("Type: ").append(ac.getAircraftType()).append(" ");
            }
            if (ac.getLatitude() != null && ac.getLongitude() != null) {
                sb.append("Position: ").append(String.format("%.4f", ac.getLatitude()))
                  .append(", ").append(String.format("%.4f", ac.getLongitude())).append(" ");
            }
            if (ac.getAltitude() != null) {
                sb.append("Alt: ").append(ac.getAltitude()).append("ft ");
            }
            if (ac.getGroundSpeed() != null) {
                sb.append("Speed: ").append(ac.getGroundSpeed().intValue()).append("kts ");
            }
            if (Boolean.TRUE.equals(ac.getMilitary())) {
                sb.append("[MILITARY] ");
            }
            if (ac.getEmergency() != null) {
                sb.append("[EMERGENCY: ").append(ac.getEmergency()).append("] ");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}