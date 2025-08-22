package com.example.api;

import com.example.domain.RecalcJob;
import com.example.service.PremiumCalculator;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobResource {

    @Inject
    PremiumCalculator calc;

    public static class RecalcRequest {
        public long policyId;
        public String trigger;
    }

    @POST
    @Transactional
    public RecalcJob submit(RecalcRequest req) {
        RecalcJob j = new RecalcJob();
        j.policyId = req.policyId;
        j.trigger = req.trigger == null ? "ASYNC_RECALC" : req.trigger;
        j.status = RecalcJob.Status.QUEUED;
        j.persist();
        return j;
    }

    @GET
    @Path("/{id}")
    public RecalcJob get(@PathParam("id") long id) {
        RecalcJob j = RecalcJob.findById(id);
        if (j == null)
            throw new NotFoundException();
        return j;
    }
}