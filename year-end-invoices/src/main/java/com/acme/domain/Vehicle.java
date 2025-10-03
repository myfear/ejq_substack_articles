package com.acme.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Vehicle extends PanacheEntity {
    public String vin;
    public String registration;
    public String typklasse; // vehicle type class
    public String regionalklasse; // regional risk zone (Bundesland mapping)
}