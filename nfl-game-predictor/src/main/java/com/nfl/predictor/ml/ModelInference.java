package com.nfl.predictor.ml;

import org.tribuo.Example;
import org.tribuo.Model;
import org.tribuo.classification.Label;
import org.tribuo.impl.ArrayExample;

import com.nfl.predictor.entity.Game;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ModelInference {

    @Inject
    FeatureBuilder builder;

    @Inject
    ModelTrainer trainer;

    private Model<Label> model;

    private void ensureModelLoaded() throws Exception {
        if (model == null) {
            model = trainer.loadModel();
        }
    }

    /**
     * Force reload the model (useful after retraining)
     */
    public void reloadModel() throws Exception {
        model = trainer.loadModel();
    }

    public PredictionResult predict(Game game) throws Exception {

        ensureModelLoaded();

        var features = builder.fromGame(game);

        Example<Label> ex = new ArrayExample<>(
                null,
                new String[] { "homeWinPct", "awayWinPct",
                        "homeAvgScored", "awayAvgScored",
                        "homeAvgAllowed", "awayAvgAllowed",
                        "homeAdvantage" },
                new double[] {
                        features.homeWinPct(),
                        features.awayWinPct(),
                        features.homeAvgPointsScored(),
                        features.awayAvgPointsScored(),
                        features.homeAvgPointsAllowed(),
                        features.awayAvgPointsAllowed(),
                        features.homeAdvantage()
                });

        var prediction = model.predict(ex);
        var dist = prediction.getOutputScores();
        Label homeWinLabel = dist.get("HOME_WIN");
        double homeProb = homeWinLabel != null ? homeWinLabel.getScore() : 0.5;

        return new PredictionResult(homeProb, homeProb > 0.5);
    }

    public record PredictionResult(double homeWinProbability, boolean homeWinPredicted) {
    }
}