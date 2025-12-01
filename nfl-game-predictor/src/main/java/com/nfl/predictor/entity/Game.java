package com.nfl.predictor.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "games")
public class Game extends PanacheEntityBase {

    @Id
    @Column(nullable = false)
    public String espnId;

    @ManyToOne
    @JoinColumn(name = "home_team_id", referencedColumnName = "espnId")
    public Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id", referencedColumnName = "espnId")
    public Team awayTeam;

    public LocalDateTime gameDate;

    public Integer homeScore;
    public Integer awayScore;

    public Boolean completed = false;

    // ML prediction fields
    public Double predictedHomeWinProbability;
    public Boolean predictedHomeWin;

    public static Game findByEspnId(String espnId) {
        return find("espnId", espnId).firstResult();
    }

    public boolean wasHomeWin() {
        return completed && homeScore != null && awayScore != null && homeScore > awayScore;
    }

    public String getFormattedGameDate() {
        if (gameDate == null) {
            return "Date TBD";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        return gameDate.format(formatter);
    }

    /**
     * Get the predicted winner's win probability as a percentage (0-100) rounded to 1 decimal place
     */
    public Double getPredictedWinnerProbabilityPercent() {
        if (predictedHomeWinProbability == null) {
            return null;
        }
        double percent;
        if (predictedHomeWin != null && predictedHomeWin) {
            percent = predictedHomeWinProbability * 100.0;
        } else {
            percent = (1.0 - predictedHomeWinProbability) * 100.0;
        }
        return Math.round(percent * 10.0) / 10.0;
    }

    /**
     * Get the predicted winner's win probability as a percentage for CSS width (0-100)
     */
    public Double getPredictedWinnerProbabilityPercentForWidth() {
        if (predictedHomeWinProbability == null) {
            return null;
        }
        double percent;
        if (predictedHomeWin != null && predictedHomeWin) {
            percent = predictedHomeWinProbability * 100.0;
        } else {
            percent = (1.0 - predictedHomeWinProbability) * 100.0;
        }
        return percent;
    }
}
