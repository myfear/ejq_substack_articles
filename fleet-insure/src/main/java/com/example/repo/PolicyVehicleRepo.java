package com.example.repo;

import java.time.LocalDate;
import java.util.List;

import com.example.domain.PolicyVehicle;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PolicyVehicleRepo implements PanacheRepository<PolicyVehicle> {

    public List<PolicyVehicle> activeOn(long policyId, LocalDate date) {
        return find("policy.id = ?1 and effectiveFrom <= ?2 and (effectiveTo is null or effectiveTo >= ?2)",
                policyId, date).list();
    }
}