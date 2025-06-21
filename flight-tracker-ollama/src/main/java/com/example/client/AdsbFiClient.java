package com.example.client;

import com.example.model.FlightDataResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/v2")
public interface AdsbFiClient {
    
    @GET
    @Path("/hex/{hex}")
    @Produces(MediaType.APPLICATION_JSON)
    FlightDataResponse getAircraftByHex(@PathParam("hex") String hex);
    
    @GET
    @Path("/callsign/{callsign}")
    @Produces(MediaType.APPLICATION_JSON)
    FlightDataResponse getAircraftByCallsign(@PathParam("callsign") String callsign);
    
    @GET
    @Path("/registration/{registration}")
    @Produces(MediaType.APPLICATION_JSON)
    FlightDataResponse getAircraftByRegistration(@PathParam("registration") String registration);
    
    @GET
    @Path("/mil")
    @Produces(MediaType.APPLICATION_JSON)
    FlightDataResponse getMilitaryAircraft();
    
    @GET
    @Path("/lat/{lat}/lon/{lon}/dist/{dist}")
    @Produces(MediaType.APPLICATION_JSON)
    FlightDataResponse getAircraftByLocation(
            @PathParam("lat") double latitude,
            @PathParam("lon") double longitude,
            @PathParam("dist") int distanceNm
    );
    
    @GET
    @Path("/sqk/{squawk}")
    @Produces(MediaType.APPLICATION_JSON)
    FlightDataResponse getAircraftBySquawk(@PathParam("squawk") String squawk);
}