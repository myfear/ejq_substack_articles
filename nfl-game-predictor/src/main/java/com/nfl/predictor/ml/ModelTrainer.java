package com.nfl.predictor.ml;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.tribuo.Example;
import org.tribuo.Model;
import org.tribuo.MutableDataset;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.classification.sgd.linear.LogisticRegressionTrainer;
import org.tribuo.impl.ArrayExample;
import org.tribuo.provenance.SimpleDataSourceProvenance;

import com.nfl.predictor.entity.Game;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@RegisterForReflection(targets = { Model.class })
public class ModelTrainer {

    @Inject
    FeatureBuilder featureBuilder;

    private static final String MODEL_PATH = "target/model.nfl";

    public Model<Label> train(List<Game> games) throws Exception {
        List<TrainingExample> rows = featureBuilder.buildTrainingSet(games);

        LabelFactory labelFactory = new LabelFactory();
        List<Example<Label>> examples = new ArrayList<>();

        for (TrainingExample r : rows) {
            Example<Label> example = new ArrayExample<>(
                    new Label(r.label()),
                    new String[] { "homeWinPct", "awayWinPct",
                            "homeAvgScored", "awayAvgScored",
                            "homeAvgAllowed", "awayAvgAllowed",
                            "homeAdvantage" },
                    new double[] {
                            r.homeWinPct(),
                            r.awayWinPct(),
                            r.homeAvgPointsScored(),
                            r.awayAvgPointsScored(),
                            r.homeAvgPointsAllowed(),
                            r.awayAvgPointsAllowed(),
                            r.homeAdvantage()
                    });
            examples.add(example);
        }

        SimpleDataSourceProvenance provenance = new SimpleDataSourceProvenance("NFL Games", labelFactory);
        MutableDataset<Label> dataset = new MutableDataset<>(examples, provenance, labelFactory);

        LogisticRegressionTrainer trainer = new LogisticRegressionTrainer();
        Model<Label> model = trainer.train(dataset);

        saveModel(model);

        return model;
    }

    public void saveModel(Model<Label> model) throws Exception {
        model.serializeToFile(Paths.get(MODEL_PATH));
    }

    @SuppressWarnings("unchecked")
    public Model<Label> loadModel() throws Exception {
        return (Model<Label>) Model.deserializeFromFile(Paths.get(MODEL_PATH));
    }
}