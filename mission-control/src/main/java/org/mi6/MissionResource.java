package org.mi6;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/mission")
public class MissionResource {

    @Inject
    MissionService missionService;

    @GET
    @Path("/new")
    @Produces(MediaType.APPLICATION_JSON)
    public MissionService.Mission getNewMission() {
        return missionService.generateAndAnalyzeMission();
    }
}