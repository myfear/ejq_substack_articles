package com.nightclub;

import java.time.Duration;
import java.util.Random;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/club")
@Produces(MediaType.APPLICATION_JSON)
public class NightclubResource {

    private final Random random = new Random();

    @GET
    @Path("/enter")
    public Uni<ClubResponse> enterClub() {
        // Simulate some work (checking ID, taking coat, etc.) - Make it slower
        int delay = 500 + random.nextInt(200); // 500-700ms
        return Uni.createFrom().item(new ClubResponse("Welcome to the club!"))
                .onItem().delayIt().by(Duration.ofMillis(delay));
    }

    @GET
    @Path("/vip-entry")
    public Uni<ClubResponse> vipEntry() {
        // VIP lane is still slow but we'll prioritize these
        int delay = 150 + random.nextInt(150); // 150-300ms
        return Uni.createFrom().item(new ClubResponse("VIP lane! Skip the line!"))
                .onItem().delayIt().by(Duration.ofMillis(delay));
    }

    @GET
    @Path("/bathroom")
    public Uni<ClubResponse> useBathroom() {
        // This takes forever at a nightclub
        int delay = 300 + random.nextInt(500); // 300-800ms
        return Uni.createFrom().item(new ClubResponse("Finally..."))
                .onItem().delayIt().by(Duration.ofMillis(delay));
    }

    public record ClubResponse(String status) {
    }

}