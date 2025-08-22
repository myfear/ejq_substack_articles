package com.example.api;

import java.time.LocalDate;

import com.example.api.dto.RiskScoreUpdateRequest;
import com.example.domain.Vehicle;
import com.example.domain.VehicleShare;
import com.example.service.PremiumCalculator;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/vehicles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VehicleResource {

    @Inject
    PremiumCalculator calculator;

    @POST
    @Path("/{id}/risk")
    @Transactional
    public Vehicle updateRisk(@PathParam("id") long id, @Valid RiskScoreUpdateRequest req) {
        Vehicle v = Vehicle.findById(id);
        if (v == null)
            throw new NotFoundException();
        v.currentRiskScore = req.newRiskScore;

        // For demo, recalc today's state. In real systems, use event time.
        // Trigger recomputation for all policies this vehicle belongs to (simplified)
        // For tutorial, find policy by a recent snapshot that references this vehicle
        VehicleShare snap = VehicleShare.find("vehicle.id", id).firstResult();
        if (snap != null) {
            calculator.recalc(snap.snapshot.policy.id, "RISK_SCORE_UPDATED vehicle=" + id, LocalDate.now());
        }
        return v;
    }
}