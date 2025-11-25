package com.example.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

import com.example.domain.FleetPolicy;
import com.example.domain.PolicyVehicle;
import com.example.domain.ReinsuranceLayer;
import com.example.domain.Vehicle;
import com.example.repo.FleetPolicyRepo;
import com.example.repo.ReinsuranceLayerRepo;
import com.example.service.PremiumCalculator;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST resource for bootstrapping demo data in the fleet insurance system.
 * <p>
 * This resource provides an endpoint to initialize the system with sample data,
 * including reinsurance layers, fleet policies, vehicles, and historical premium snapshots.
 * It is primarily used for demonstration and testing purposes.
 * </p>
 *
 * @author Fleet Insurance System
 */
@Path("/bootstrap")
@Produces(MediaType.APPLICATION_JSON)
public class BootstrapResource {

    @Inject
    FleetPolicyRepo policyRepo;
    @Inject
    ReinsuranceLayerRepo layerRepo;
    @Inject
    PremiumCalculator calculator;

    /**
     * Creates a complete demo fleet policy with associated data.
     * <p>
     * This method performs the following operations:
     * <ul>
     *   <li>Creates three reinsurance layers (Layer A, B, C) if none exist</li>
     *   <li>Creates a new fleet policy with a coverage limit of $1,000,000</li>
     *   <li>Adds 10 vehicles to the policy with randomized risk scores (60-89)</li>
     *   <li>Generates historical premium snapshots for the past 6 months</li>
     * </ul>
     * </p>
     * <p>
     * The policy is created with:
     * <ul>
     *   <li>Effective date: 6 months ago (first day of that month)</li>
     *   <li>Expiration date: 1 year from effective date</li>
     *   <li>Rate lock period: 3 months from effective date</li>
     * </ul>
     * </p>
     *
     * @return the created {@link FleetPolicy} with all associated vehicles and snapshots
     */
    @POST
    @Transactional
    public FleetPolicy create() {
        // Reinsurance layers
        if (layerRepo.count() == 0) {
            layer("Layer A", "0", "100000");
            layer("Layer B", "100000", "250000");
            layer("Layer C", "250000", "1000000000");
        }

        FleetPolicy p = new FleetPolicy();
        p.policyNumber = "POL-" + System.currentTimeMillis();
        p.customer = "InsureCorp Demo Customer";
        p.coverageLimit = new BigDecimal("1000000.00");
        p.effectiveFrom = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        p.effectiveTo = p.effectiveFrom.plusYears(1).minusDays(1);
        p.rateLockUntil = p.effectiveFrom.plusMonths(3);
        policyRepo.persist(p);

        // 10 vehicles with random risk
        Random r = new Random(42);
        for (int i = 0; i < 10; i++) {
            Vehicle v = new Vehicle();
            v.vin = "VIN" + (100000 + i);
            v.makeModel = "Truck-" + (i + 1);
            v.currentRiskScore = 60 + r.nextInt(30);
            v.usageProfile = i % 2 == 0 ? "URBAN" : "HIGHWAY";
            v.persist();

            PolicyVehicle pv = new PolicyVehicle();
            pv.policy = p;
            pv.vehicle = v;
            pv.effectiveFrom = p.effectiveFrom;
            pv.persist();
        }

        // Historical snapshots: one per month
        for (int m = 5; m >= 0; m--) {
            LocalDate monthPoint = LocalDate.now().minusMonths(m).withDayOfMonth(15);
            calculator.recalc(p.id, "MONTHLY_SNAPSHOT", monthPoint);
        }
        return p;
    }

    /**
     * Creates and persists a reinsurance layer with the specified bounds.
     *
     * @param name the name of the reinsurance layer (e.g., "Layer A")
     * @param low  the lower bound of the layer as a string representation of a decimal value
     * @param high the upper bound of the layer as a string representation of a decimal value
     */
    private void layer(String name, String low, String high) {
        ReinsuranceLayer l = new ReinsuranceLayer();
        l.name = name;
        l.lowerBound = new BigDecimal(low);
        l.upperBound = new BigDecimal(high);
        layerRepo.persist(l);
    }
}