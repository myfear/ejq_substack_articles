package com.example.api;

import java.util.List;

import com.example.StartupService;
import com.example.view.ClusterView;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/clusters")
public class ClustersResource {

    @Inject
    StartupService startupService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClusterView> getClusters() {
        return startupService.getClusters();
    }
}
