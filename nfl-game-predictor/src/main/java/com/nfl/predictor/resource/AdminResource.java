package com.nfl.predictor.resource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import com.nfl.predictor.entity.Game;
import com.nfl.predictor.entity.Team;
import com.nfl.predictor.ml.ModelInference;
import com.nfl.predictor.ml.ModelTrainer;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    @Inject
    ModelTrainer trainer;

    @Inject
    ModelInference inference;

    private static final String MODEL_PATH = "model.nfl";

    /**
     * List all games as JSON
     */
    @GET
    @Path("/games")
    public List<Game> listGames() {
        return Game.listAll();
    }

    /**
     * List all teams as JSON
     */
    @GET
    @Path("/teams")
    public List<Team> listTeams() {
        return Team.listAll();
    }

    /**
     * Check if model is trained and available
     */
    @GET
    @Path("/model/status")
    public Response checkModelStatus() {
        try {
            boolean modelExists = Files.exists(Paths.get(MODEL_PATH));
            if (modelExists) {
                // Try to load the model to verify it's valid
                try {
                    trainer.loadModel();
                    return Response.ok().entity(new ModelStatus(true, "Model is trained and available")).build();
                } catch (Exception e) {
                    return Response.ok().entity(new ModelStatus(false, "Model file exists but cannot be loaded: " + e.getMessage())).build();
                }
            } else {
                return Response.ok().entity(new ModelStatus(false, "Model not trained yet")).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ModelStatus(false, "Error checking model status: " + e.getMessage())).build();
        }
    }

    /**
     * List predictions for upcoming games
     */
    @GET
    @Path("/predictions")
    @Transactional
    public Response listPredictions() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Get all upcoming games (not completed and game date is in the future)
            List<Game> upcomingGames = Game.find(
                "completed = false and gameDate > ?1 order by gameDate",
                now
            ).list();
            
            // Make predictions for each upcoming game using ML model
            for (Game game : upcomingGames) {
                if (game.homeTeam != null && game.awayTeam != null) {
                    // Only predict if prediction doesn't exist
                    if (game.predictedHomeWinProbability == null) {
                        try {
                            var result = inference.predict(game);
                            game.predictedHomeWinProbability = result.homeWinProbability();
                            game.predictedHomeWin = result.homeWinPredicted();
                            game.persist();
                        } catch (Exception e) {
                            // Log error but continue with other games
                            System.err.println("Error predicting game " + game.espnId + ": " + e.getMessage());
                        }
                    }
                }
            }
            
            return Response.ok(upcomingGames).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error getting predictions: " + e.getMessage()).build();
        }
    }

    /**
     * Train the model manually (admin endpoint)
     */
    @GET
    @Path("/model/train")
    @Transactional
    public Response trainModel() {
        try {
            List<Game> completed = Game.list("completed", true);
            if (completed.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No completed games found. Cannot train model.").build();
            }
            trainer.train(completed);
            inference.reloadModel();
            return Response.ok().entity("Model trained & saved on " + completed.size() + " games").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error training model: " + e.getMessage()).build();
        }
    }

    public record ModelStatus(boolean trained, String message) {
    }
}

