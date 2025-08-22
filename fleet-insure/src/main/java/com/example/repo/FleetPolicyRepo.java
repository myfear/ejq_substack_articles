package com.example.repo;

import com.example.domain.FleetPolicy;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FleetPolicyRepo implements PanacheRepository<FleetPolicy> {
}