package com.example.repo;

import com.example.domain.ReinsuranceLayer;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReinsuranceLayerRepo implements PanacheRepository<ReinsuranceLayer> {
}