package com.example.api;

import java.util.List;

import com.example.api.dto.VehicleAdditionRequest;
import com.example.domain.FleetPolicy;
import com.example.domain.FleetPremiumSnapshot;
import com.example.domain.PolicyVehicle;
import com.example.domain.Vehicle;
import com.example.repo.FleetPolicyRepo;
import com.example.repo.PolicyVehicleRepo;
import com.example.repo.VehicleRepo;
import com.example.service.PremiumCalculator;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PolicyResource {

    @Inject
    FleetPolicyRepo policyRepo;
    @Inject
    VehicleRepo vehicleRepo;
    @Inject
    PolicyVehicleRepo pvRepo;
    @Inject
    PremiumCalculator calculator;

    @GET
    @Path("/{id}")
    public FleetPolicy get(@PathParam("id") long id) {
        FleetPolicy p = policyRepo.findById(id);
        if (p == null)
            throw new NotFoundException();
        return p;
    }

    @GET
    @Path("/{id}/snapshots")
    public List<FleetPremiumSnapshot> snapshots(@PathParam("id") long id) {
        return FleetPremiumSnapshot.find("policy.id", id).list();
    }

    @POST
    @Path("/{id}/vehicles")
    @Transactional
    public Response addVehicle(@PathParam("id") long policyId, @Valid VehicleAdditionRequest req) {
        FleetPolicy policy = policyRepo.findById(policyId);
        if (policy == null)
            throw new NotFoundException("Policy not found");

        Vehicle v = Vehicle.find("vin", req.vin).firstResult();
        if (v == null) {
            v = new Vehicle();
            v.vin = req.vin;
            v.makeModel = req.makeModel;
            v.currentRiskScore = req.riskScore;
            v.usageProfile = req.usageProfile == null ? "MIXED" : req.usageProfile;
            vehicleRepo.persist(v);
        }

        PolicyVehicle pv = new PolicyVehicle();
        pv.policy = policy;
        pv.vehicle = v;
        pv.effectiveFrom = req.effectiveFrom;
        pv.persist();

        calculator.recalc(policyId, "VEHICLE_ADDED vin=" + v.vin, req.effectiveFrom);
        return Response.status(Response.Status.CREATED).entity(v).build();
    }
}