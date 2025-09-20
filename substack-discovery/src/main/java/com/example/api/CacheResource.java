package com.example.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.example.StartupService;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/cache")
public class CacheResource {

    @Inject
    StartupService startupService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CacheInfo getCacheInfo() {
        try {
            if (Files.exists(Paths.get("clusters_cache.ser"))) {
                long cacheAge = System.currentTimeMillis() - Files.getLastModifiedTime(Paths.get("clusters_cache.ser")).toMillis();
                return new CacheInfo(true, cacheAge, startupService.getClusters().size());
            }
        } catch (IOException e) {
            // ignore
        }
        return new CacheInfo(false, 0, 0);
    }

    @DELETE
    public Response clearCache() {
        try {
            Files.deleteIfExists(Paths.get("clusters_cache.ser"));
            return Response.ok("Cache cleared successfully").build();
        } catch (IOException e) {
            return Response.serverError().entity("Failed to clear cache: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/refresh")
    public Response refreshCache() {
        try {
            Files.deleteIfExists(Paths.get("clusters_cache.ser"));
            return Response.ok("Cache refresh initiated. Check /status for progress.").build();
        } catch (IOException e) {
            return Response.serverError().entity("Failed to refresh cache: " + e.getMessage()).build();
        }
    }

    public static class CacheInfo {
        public boolean exists;
        public long ageInMillis;
        public int clusterCount;

        public CacheInfo(boolean exists, long ageInMillis, int clusterCount) {
            this.exists = exists;
            this.ageInMillis = ageInMillis;
            this.clusterCount = clusterCount;
        }
    }
}
