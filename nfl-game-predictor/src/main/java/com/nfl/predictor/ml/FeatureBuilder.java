package com.nfl.predictor.ml;

import java.util.List;

import com.nfl.predictor.entity.Game;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FeatureBuilder {

    public TrainingExample fromGame(Game g) {

        double homeWinPct = g.homeTeam.getWinPercentage();
        double awayWinPct = g.awayTeam.getWinPercentage();

        double homeAvg = g.homeTeam.getAveragePointsScored();
        double awayAvg = g.awayTeam.getAveragePointsScored();

        double homeAllowed = g.homeTeam.getAveragePointsAllowed();
        double awayAllowed = g.awayTeam.getAveragePointsAllowed();

        double homeAdvantage = 1.0; // constant feature

        // Label is only needed for training; for prediction, use a placeholder
        String label = "UNKNOWN";
        if (g.completed != null && g.completed && g.wasHomeWin()) {
            label = "HOME_WIN";
        } else if (g.completed != null && g.completed) {
            label = "AWAY_WIN";
        }

        return new TrainingExample(
                homeWinPct,
                awayWinPct,
                homeAvg,
                awayAvg,
                homeAllowed,
                awayAllowed,
                homeAdvantage,
                label);
    }

    public List<TrainingExample> buildTrainingSet(List<Game> completedGames) {
        return completedGames.stream()
                .map(this::fromGame)
                .toList();
    }
}