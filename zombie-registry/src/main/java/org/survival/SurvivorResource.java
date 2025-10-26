package org.survival;

import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/survivors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SurvivorResource {

    @GET
    public List<Survivor> getAllSurvivors() {
        return Survivor.listAll();
    }

    @GET
    @Path("/unbitten")
    public List<Survivor> getUnbitten() {
        return Survivor.findUnbitten();
    }

    @GET
    @Path("/leaderboard")
    public List<Survivor> getTopWarriors(@QueryParam("limit") @DefaultValue("5") int limit) {
        return Survivor.findTopWarriors(limit);
    }

    @GET
    @Path("/stats")
    public Response getSurvivorStats() {
        long total = Survivor.count();
        long bitten = Survivor.count("hasBeenBitten", true);
        long totalKills = Survivor.find("SELECT SUM(s.zombieKills) FROM Survivor s")
                .project(Long.class)
                .firstResult();

        return Response.ok(new Stats(total, bitten, totalKills)).build();
    }

    @POST
    @Transactional
    public Response registerNewSurvivor(Survivor survivor) {
        survivor.persist();
        return Response.status(Response.Status.CREATED).entity(survivor).build();
    }

    public record Stats(long totalSurvivors, long bitten, long totalZombieKills) {
    }
}

@Path("/zombies")
@Produces(MediaType.APPLICATION_JSON)
class ZombieResource {

    @GET
    public List<Zombie> getAllZombies() {
        return Zombie.listAll();
    }

    @GET
    @Path("/threat/{level}")
    public Response countByThreatLevel(@PathParam("level") String level) {
        long count = Zombie.countByThreatLevel(level);
        return Response.ok(new ThreatCount(level, count)).build();
    }

    public record ThreatCount(String threatLevel, long count) {
    }
}

@Path("/supplies")
@Produces(MediaType.APPLICATION_JSON)
class SupplyCacheResource {

    @GET
    public List<SupplyCache> getAllCaches() {
        return SupplyCache.listAll();
    }

    @GET
    @Path("/safe")
    public List<SupplyCache> getSafeCaches() {
        return SupplyCache.list("isCompromised", false);
    }

    @GET
    @Path("/nearest")
    public List<SupplyCache> getNearestCaches(
            @QueryParam("lat") double lat,
            @QueryParam("lon") double lon) {
        // Simple distance calculation (not production-ready!)
        return SupplyCache.<SupplyCache>streamAll()
                .sorted((a, b) -> {
                    double distA = Math.sqrt(Math.pow(a.latitude - lat, 2) + Math.pow(a.longitude - lon, 2));
                    double distB = Math.sqrt(Math.pow(b.latitude - lat, 2) + Math.pow(b.longitude - lon, 2));
                    return Double.compare(distA, distB);
                })
                .limit(3)
                .toList();
    }
}