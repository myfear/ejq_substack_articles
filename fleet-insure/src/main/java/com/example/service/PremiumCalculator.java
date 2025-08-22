package com.example.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.example.domain.AuditEntry;
import com.example.domain.FleetPolicy;
import com.example.domain.FleetPremiumSnapshot;
import com.example.domain.ReinsuranceLayer;
import com.example.domain.SnapshotReinsuranceAllocation;
import com.example.domain.Vehicle;
import com.example.domain.VehicleShare;
import com.example.repo.FleetPolicyRepo;
import com.example.repo.PolicyVehicleRepo;
import com.example.repo.ReinsuranceLayerRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Computes fleet-wide premium snapshots for a given policy and as-of date.
 * - Creates immutable snapshots with vehicle shares and reinsurance allocations
 * - Dedupes only when (same asOf + same risk vector)
 * - Records an audit entry for each computation
 */
@ApplicationScoped
public class PremiumCalculator {

    @Inject
    FleetPolicyRepo policyRepo;
    @Inject
    PolicyVehicleRepo policyVehicleRepo;
    @Inject
    ReinsuranceLayerRepo layerRepo;

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    /**
     * Demo rate: currency units per risk point per day. Replace with your actuarial
     * model.
     */
    private static final BigDecimal RATE_PER_RISK_POINT_PER_DAY = new BigDecimal("1.25");

    @Transactional
    public FleetPremiumSnapshot recalc(long policyId, String trigger, LocalDate asOf) {
        FleetPolicy policy = policyRepo.findById(policyId);
        if (policy == null)
            throw new IllegalArgumentException("Policy not found: " + policyId);

        // 1) Determine active vehicles for the as-of date
        var memberships = policyVehicleRepo.activeOn(policyId, asOf);
        var vehicles = memberships.stream().map(m -> m.vehicle).collect(Collectors.toList());

        // 2) Build a stable risk vector and hash it
        Map<Long, Integer> riskMap = new TreeMap<>();
        for (Vehicle v : vehicles)
            riskMap.put(v.id, v.currentRiskScore);
        String riskHash = hashRisk(riskMap);

        // 3) Find the previous snapshot to decide dedupe and set "previousSnapshot"
        // link
        FleetPremiumSnapshot previous = FleetPremiumSnapshot
                .find("policy = ?1 order by asOf desc, id desc", policy)
                .firstResult();

        // Dedupe ONLY if the same asOf date and the same risk vector were already
        // computed
        if (previous != null && asOf.equals(previous.asOf) && previous.riskHash.equals(riskHash)) {
            return previous;
        }

        // 4) Premium math
        int totalRisk = vehicles.stream().mapToInt(v -> v.currentRiskScore).sum();
        totalRisk = Math.max(totalRisk, 0);

        long remainingDays = daysRemaining(policy, asOf);
        BigDecimal totalPremium = BigDecimal.ZERO;
        List<VehicleShare> shares = new ArrayList<>(vehicles.size());

        for (Vehicle v : vehicles) {
            BigDecimal pct = totalRisk == 0
                    ? BigDecimal.ZERO
                    : new BigDecimal(v.currentRiskScore).divide(new BigDecimal(totalRisk), 6, RoundingMode.HALF_UP);

            BigDecimal daily = new BigDecimal(v.currentRiskScore).multiply(RATE_PER_RISK_POINT_PER_DAY, MC);
            BigDecimal contribution = daily.multiply(new BigDecimal(remainingDays), MC);

            VehicleShare s = new VehicleShare();
            s.vehicle = v;
            s.riskScore = v.currentRiskScore;
            s.fleetPercentage = pct;
            s.premiumContribution = contribution.setScale(2, RoundingMode.HALF_UP);
            s.exposureUnits = new BigDecimal(v.currentRiskScore).setScale(4, RoundingMode.HALF_UP);
            s.effectiveFromDate = LocalDateTime.of(asOf, LocalTime.NOON);
            shares.add(s);

            totalPremium = totalPremium.add(contribution, MC);
        }

        // 5) Create the snapshot aggregate
        FleetPremiumSnapshot snap = new FleetPremiumSnapshot();
        snap.policy = policy;
        snap.asOf = asOf;
        snap.calculationDate = LocalDateTime.now();
        snap.calculationTrigger = trigger;
        snap.previousSnapshot = previous;
        snap.totalPremium = totalPremium.setScale(2, RoundingMode.HALF_UP);
        snap.riskHash = riskHash;

        for (VehicleShare s : shares)
            s.snapshot = snap;
        snap.vehicleShares = shares;

        // Attribute exposure to the snapshot month (using asOf, not "now")
        BigDecimal exposureSum = shares.stream()
                .map(vs -> vs.exposureUnits)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        snap.exposureUnitsByMonth = Map.of(asOf.getMonth(), exposureSum);

        // 6) Reinsurance allocations for each layer based on total premium
        List<ReinsuranceLayer> layers = layerRepo.listAll().stream()
                .sorted(Comparator.comparing(l -> l.lowerBound))
                .toList();

        List<SnapshotReinsuranceAllocation> allocations = new ArrayList<>(layers.size());
        for (ReinsuranceLayer layer : layers) {
            BigDecimal alloc = allocationForLayer(layer, snap.totalPremium).setScale(2, RoundingMode.HALF_UP);
            SnapshotReinsuranceAllocation a = new SnapshotReinsuranceAllocation();
            a.snapshot = snap;
            a.layer = layer;
            a.allocatedAmount = alloc;
            allocations.add(a);
        }
        snap.reinsuranceAllocations = allocations;

        // 7) Persist via owning aggregate (policy has cascade = ALL)
        policy.snapshots = policy.snapshots == null ? new ArrayList<>() : policy.snapshots;
        policy.snapshots.add(snap);

        // 8) Audit entry
        AuditEntry ae = new AuditEntry();
        ae.policy = policy;
        ae.reason = "Recalculation";
        ae.trigger = trigger + " asOf=" + asOf;
        policy.auditEntries = policy.auditEntries == null ? new ArrayList<>() : policy.auditEntries;
        policy.auditEntries.add(ae);

        return snap;
    }

    // --- helpers -------------------------------------------------------------

    private static long daysRemaining(FleetPolicy p, LocalDate asOf) {
        if (asOf.isAfter(p.effectiveTo))
            return 0;
        LocalDate start = asOf;
        LocalDate endInclusive = p.effectiveTo;
        return Math.max(0, Duration.between(start.atStartOfDay(), endInclusive.plusDays(1).atStartOfDay()).toDays());
        // inclusive of policy end date
    }

    private static BigDecimal allocationForLayer(ReinsuranceLayer layer, BigDecimal total) {
        // amount covered in this band = max(min(total, upper) - lower, 0)
        BigDecimal capped = total.min(layer.upperBound);
        BigDecimal raw = capped.subtract(layer.lowerBound, MC);
        return raw.max(BigDecimal.ZERO);
    }

    private static String hashRisk(Map<Long, Integer> riskMap) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            riskMap.forEach((id, score) -> sb.append(id).append(':').append(score).append(';'));
            byte[] hash = md.digest(sb.toString().getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash)
                hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
