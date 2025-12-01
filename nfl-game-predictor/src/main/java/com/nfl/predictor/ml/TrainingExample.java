package com.nfl.predictor.ml;

public record TrainingExample(
        double homeWinPct,
        double awayWinPct,
        double homeAvgPointsScored,
        double awayAvgPointsScored,
        double homeAvgPointsAllowed,
        double awayAvgPointsAllowed,
        double homeAdvantage,
        String label // “HOME_WIN” or “AWAY_WIN”
) {
}