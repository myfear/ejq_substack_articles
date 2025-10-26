package org.survival;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "zombies")
public class Zombie extends PanacheEntity {

    public String type;
    @Column(name = "speed_level")
    public int speedLevel;
    @Column(name = "intelligence_level")
    public int intelligenceLevel;
    @Column(name = "last_spotted_zone")
    public String lastSpottedZone;
    @Column(name = "threat_level")
    public String threatLevel;

    public static long countByThreatLevel(String level) {
        return count("threatLevel", level);
    }
}