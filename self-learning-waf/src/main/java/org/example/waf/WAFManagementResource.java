package org.example.waf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/management/waf")
@ApplicationScoped
public class WAFManagementResource {

    @Inject
    SelfLearningWAFFilter wafFilter;

    @GET
    @Path("/stats")
    @Produces("application/json")
    public Response getStatistics() {
        return Response.ok(wafFilter.getStatistics()).build();
    }

    @POST
    @Path("/false-positive")
    public Response reportFalsePositive() {
        wafFilter.reportFalsePositive();
        return Response.ok("Threshold adjusted.").build();
    }
}