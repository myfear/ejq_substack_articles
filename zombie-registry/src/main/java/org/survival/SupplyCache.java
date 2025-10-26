package org.survival;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "supply_caches")
public class SupplyCache extends PanacheEntity {

    @Column(name = "location_name")
    public String locationName;
    public double latitude;
    public double longitude;
    @Column(name = "food_units")
    public int foodUnits;
    @Column(name = "water_units")
    public int waterUnits;
    @Column(name = "medical_supplies")
    public int medicalSupplies;
    @Column(name = "ammunition_count")
    public int ammunitionCount;
    @Column(name = "is_compromised")
    public boolean isCompromised;
}
