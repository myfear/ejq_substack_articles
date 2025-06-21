package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Aircraft {
    
    @JsonProperty("hex")
    private String hexCode;
    
    @JsonProperty("flight")
    private String callsign;
    
    @JsonProperty("r")
    private String registration;
    
    @JsonProperty("t")
    private String aircraftType;
    
    @JsonProperty("lat")
    private Double latitude;
    
    @JsonProperty("lon")
    private Double longitude;
    
    @JsonProperty("alt_baro")
    private String altitude;
    
    @JsonProperty("gs")
    private Double groundSpeed;
    
    @JsonProperty("track")
    private Double heading;
    
    @JsonProperty("squawk")
    private String squawk;
    
    @JsonProperty("emergency")
    private String emergency;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("mil")
    private Boolean military;
    
    // Constructors
    public Aircraft() {}
    
    // Getters and Setters
    public String getHexCode() { return hexCode; }
    public void setHexCode(String hexCode) { this.hexCode = hexCode; }
    
    public String getCallsign() { return callsign; }
    public void setCallsign(String callsign) { this.callsign = callsign; }
    
    public String getRegistration() { return registration; }
    public void setRegistration(String registration) { this.registration = registration; }
    
    public String getAircraftType() { return aircraftType; }
    public void setAircraftType(String aircraftType) { this.aircraftType = aircraftType; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getAltitude() { return altitude; }
    public void setAltitude(String altitude) { this.altitude = altitude; }
    
    public Double getGroundSpeed() { return groundSpeed; }
    public void setGroundSpeed(Double groundSpeed) { this.groundSpeed = groundSpeed; }
    
    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }
    
    public String getSquawk() { return squawk; }
    public void setSquawk(String squawk) { this.squawk = squawk; }
    
    public String getEmergency() { return emergency; }
    public void setEmergency(String emergency) { this.emergency = emergency; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Boolean getMilitary() { return military; }
    public void setMilitary(Boolean military) { this.military = military; }
    
    @Override
    public String toString() {
        return String.format("Aircraft{callsign='%s', registration='%s', type='%s', lat=%s, lon=%s, alt=%d, military=%s}", 
                callsign, registration, aircraftType, latitude, longitude, altitude, military);
    }
}