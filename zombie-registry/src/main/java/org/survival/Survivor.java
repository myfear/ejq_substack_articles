package org.survival;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "survivors")
public class Survivor extends PanacheEntity {

    public String name;
    @Column(name = "zombie_kills")
    public int zombieKills;
    @Enumerated(EnumType.STRING)
    public SkillSet skillSet;
    @Column(name = "days_survived")
    public int daysSurvived;
    @Column(name = "has_been_bitten")
    public boolean hasBeenBitten;
    @Column(name = "last_seen_date")
    public LocalDate lastSeenDate;

    public enum SkillSet {
        MEDIC, ENGINEER, SCAVENGER, WARRIOR, FARMER, SCIENTIST
    }

    public static List<Survivor> findUnbitten() {
        return list("hasBeenBitten", false);
    }

    public static List<Survivor> findTopWarriors(int limit) {
        return find("ORDER BY zombieKills DESC").page(0, limit).list();
    }
}
