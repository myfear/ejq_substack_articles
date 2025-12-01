package com.nfl.predictor.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teams")
public class Team extends PanacheEntityBase {

    @Id
    @Column(nullable = false)
    public String espnId;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String abbreviation;

    // Stats for ML features
    public Integer wins = 0;
    public Integer losses = 0;
    public Integer pointsScored = 0;
    public Integer pointsAllowed = 0;

    public static Team findByEspnId(String espnId) {
        return find("espnId", espnId).firstResult();
    }

    public double getWinPercentage() {
        int totalGames = wins + losses;
        return totalGames > 0 ? (double) wins / totalGames : 0.5;
    }

    public double getAveragePointsScored() {
        int totalGames = wins + losses;
        return totalGames > 0 ? (double) pointsScored / totalGames : 20.0;
    }

    public double getAveragePointsAllowed() {
        int totalGames = wins + losses;
        return totalGames > 0 ? (double) pointsAllowed / totalGames : 20.0;
    }
}