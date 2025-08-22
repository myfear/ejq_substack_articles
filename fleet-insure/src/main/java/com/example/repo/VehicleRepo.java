package com.example.repo;

import com.example.domain.Vehicle;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VehicleRepo implements PanacheRepository<Vehicle> {
}
