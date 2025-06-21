package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightDataResponse {
    
    @JsonProperty("ac")
    private List<Aircraft> aircraft;
    
    @JsonProperty("total")
    private Integer total;
    
    @JsonProperty("now")
    private Long now;
    
    public FlightDataResponse() {}
    
    public List<Aircraft> getAircraft() { return aircraft; }
    public void setAircraft(List<Aircraft> aircraft) { this.aircraft = aircraft; }
    
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    
    public Long getNow() { return now; }
    public void setNow(Long now) { this.now = now; }
}