package com.nfl.predictor.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import com.nfl.predictor.client.ESPNClient;
import com.nfl.predictor.client.ESPNScoreboardResponse;
import com.nfl.predictor.entity.Game;
import com.nfl.predictor.entity.Team;
import com.nfl.predictor.ml.ModelTrainer;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class GameDataService {

    private static final Logger LOG = Logger.getLogger(GameDataService.class);

    @Inject
    @RestClient
    ESPNClient espnClient;

    @Inject
    ModelTrainer modelTrainer;

    /**
     * Get raw API response for debugging purposes
     */
    public ESPNScoreboardResponse getRawScoreboardResponse(String date) {
        return espnClient.getScoreboard(date, null, null);
    }

    /**
     * Fetch and store games for a date range.
     * 
     * @param startDate Start date in YYYYMMDD format (e.g., "20240905")
     * @param endDate End date in YYYYMMDD format (e.g., "20250108")
     */
    @Transactional
    public void fetchAndStoreGamesForDateRange(String startDate, String endDate) {
        String dateRange = startDate + "-" + endDate;
        LOG.infof("Fetching games for date range: %s", dateRange);

        try {
            // Use limit=1000 to get as many games as possible
            ESPNScoreboardResponse response = espnClient.getScoreboard(dateRange, null, 1000);

            if (response == null || response.events == null || response.events.isEmpty()) {
                LOG.info("No games found for this date range");
                return;
            }

            LOG.infof("Received %d events from API for date range %s", response.events.size(), dateRange);
            
            for (ESPNScoreboardResponse.Event event : response.events) {
                try {
                    processGame(event);
                } catch (Exception e) {
                    LOG.errorf(e, "Error processing game event %s: %s", event != null ? event.id : "unknown", e.getMessage());
                }
            }

            LOG.infof("Processed %d games for date range %s", response.events.size(), dateRange);
        } catch (Exception e) {
            LOG.errorf(e, "Error fetching games from ESPN API for date range %s: %s", dateRange, e.getMessage());
            throw new RuntimeException("Failed to fetch games from ESPN API", e);
        }
    }

    /**
     * Fetch and store all games for a specific season.
     * NFL seasons typically run from early September to early February.
     * 
     * @param year The season year (e.g., 2024 for the 2024-2025 season)
     */
    @Transactional
    public void fetchAndStoreGamesForSeason(int year) {
        LOG.infof("Fetching all games for %d season", year);
        
        // NFL season typically runs from early September to early February
        // Regular season: Early September to early January
        // Postseason: Early January to early February
        String startDate = String.format("%d0901", year); // September 1
        String endDate = String.format("%d0208", year + 1); // February 8 of next year
        
        LOG.infof("Fetching games from %s to %s", startDate, endDate);
        fetchAndStoreGamesForDateRange(startDate, endDate);
    }

    /**
     * Fetch and store all games for the current season.
     */
    @Transactional
    public void fetchAndStoreGamesForCurrentSeason() {
        int currentYear = java.time.Year.now().getValue();
        // If we're past February, we're in the next season's year
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        if (currentMonth >= 2 && currentMonth < 9) {
            // Between Feb and Sep, we're in the previous season
            fetchAndStoreGamesForSeason(currentYear - 1);
        } else {
            // Sep to Jan, we're in the current season
            fetchAndStoreGamesForSeason(currentYear);
        }
    }

    /**
     * Fetch and store games for a specific date.
     * 
     * @param date Date in YYYYMMDD format (e.g., "20241215")
     */
    @Transactional
    public void fetchAndStoreGames(String date) {
        LOG.infof("Fetching games for date: %s", date);

        try {
            // Call ESPN API with date (seasonType and limit are null to get all season types)
            ESPNScoreboardResponse response = espnClient.getScoreboard(date, null, null);

            if (response == null || response.events == null || response.events.isEmpty()) {
                LOG.info("No games found for this date");
                return;
            }

            LOG.infof("Received %d events from API", response.events.size());
            
            for (ESPNScoreboardResponse.Event event : response.events) {
                try {
                    // Debug: Log event structure
                    if (event != null) {
                        LOG.debugf("Processing event: id=%s, name=%s, date=%s, competitions=%s",
                            event.id, event.name, event.date,
                            event.competitions != null ? "present(" + event.competitions.size() + ")" : "null");
                    }
                    processGame(event);
                } catch (Exception e) {
                    LOG.errorf(e, "Error processing game event %s: %s", event != null ? event.id : "unknown", e.getMessage());
                }
            }

            LOG.infof("Processed %d games", response.events.size());
        } catch (Exception e) {
            LOG.errorf(e, "Error fetching games from ESPN API for date %s: %s", date, e.getMessage());
            throw new RuntimeException("Failed to fetch games from ESPN API", e);
        }
    }

    @Transactional
    public void processGame(ESPNScoreboardResponse.Event event) {
        if (event == null || event.id == null) {
            LOG.warn("Skipping null event or event without ID");
            return;
        }

        Game game = Game.findByEspnId(event.id);

        if (game == null) {
            game = new Game();
            game.espnId = event.id;
        }

        // Use the helper method to get competition from the competitions array
        var competition = event.getCompetition();
        if (competition == null) {
            LOG.warnf("Event %s has no competition data. competitions=%s", 
                event.id, event.competitions != null ? "present(" + event.competitions.size() + ")" : "null");
            return;
        }

        var competitors = competition.competitors;
        if (competitors == null || competitors.isEmpty()) {
            LOG.warnf("Event %s has no competitors", event.id);
            return;
        }

        // Find home and away teams
        var homeComp = competitors.stream()
                .filter(c -> c != null && "home".equals(c.homeAway))
                .findFirst()
                .orElse(null);

        var awayComp = competitors.stream()
                .filter(c -> c != null && "away".equals(c.homeAway))
                .findFirst()
                .orElse(null);

        if (homeComp == null || awayComp == null || homeComp.team == null || awayComp.team == null) {
            LOG.warnf("Event %s missing home or away team data", event.id);
            return;
        }

        game.homeTeam = getOrCreateTeam(homeComp.team);
        game.awayTeam = getOrCreateTeam(awayComp.team);

        if (event.date != null) {
            try {
                game.gameDate = LocalDateTime.parse(event.date, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                LOG.warnf(e, "Failed to parse date '%s' for event %s, trying alternative formats", event.date, event.id);
                // Try alternative date formats if needed
                try {
                    game.gameDate = LocalDateTime.parse(event.date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e2) {
                    LOG.errorf(e2, "Could not parse date '%s' for event %s", event.date, event.id);
                }
            }
        }

        // Check if game is completed using the helper method
        if (competition.isCompleted()) {
            // Track if this game was already completed before processing
            boolean wasAlreadyCompleted = Boolean.TRUE.equals(game.completed);
            game.completed = true;
            
            // Safe score parsing
            try {
                if (homeComp.score != null && !homeComp.score.trim().isEmpty()) {
                    game.homeScore = Integer.parseInt(homeComp.score.trim());
                }
                if (awayComp.score != null && !awayComp.score.trim().isEmpty()) {
                    game.awayScore = Integer.parseInt(awayComp.score.trim());
                }
                
                // Only update team stats if this game was not already completed
                // This prevents double-counting when the same game is fetched multiple times
                if (game.homeScore != null && game.awayScore != null && !wasAlreadyCompleted) {
                    updateTeamStats(game);
                }
            } catch (NumberFormatException e) {
                LOG.warnf(e, "Invalid score format for event %s: home=%s, away=%s", 
                    event.id, homeComp.score, awayComp.score);
            }
        }

        game.persist();
    }

    private Team getOrCreateTeam(ESPNScoreboardResponse.Event.Competition.Competitor.Team teamData) {
        if (teamData == null || teamData.id == null) {
            LOG.warn("Cannot create team from null team data");
            return null;
        }

        Team team = Team.findByEspnId(teamData.id);

        if (team == null) {
            team = new Team();
            team.espnId = teamData.id;
            team.name = teamData.displayName != null ? teamData.displayName : "Unknown";
            team.abbreviation = teamData.abbreviation != null ? teamData.abbreviation : "";
            team.persist();
        }

        return team;
    }

    private void updateTeamStats(Game game) {
        if (game.homeScore == null || game.awayScore == null || 
            game.homeTeam == null || game.awayTeam == null) {
            LOG.warn("Cannot update team stats: missing score or team data");
            return;
        }

        if (game.homeScore > game.awayScore) {
            game.homeTeam.wins++;
            game.awayTeam.losses++;
        } else {
            game.awayTeam.wins++;
            game.homeTeam.losses++;
        }

        game.homeTeam.pointsScored += game.homeScore;
        game.homeTeam.pointsAllowed += game.awayScore;
        game.awayTeam.pointsScored += game.awayScore;
        game.awayTeam.pointsAllowed += game.homeScore;

        game.homeTeam.persist();
        game.awayTeam.persist();
    }

    /**
     * Recalculate all team statistics from scratch based on completed games.
     * This is useful to fix any incorrect stats that may have been accumulated.
     */
    @Transactional
    public void recalculateAllTeamStats() {
        LOG.info("Recalculating all team statistics from completed games");
        
        // Reset all team stats
        List<Team> allTeams = Team.listAll();
        for (Team team : allTeams) {
            team.wins = 0;
            team.losses = 0;
            team.pointsScored = 0;
            team.pointsAllowed = 0;
            team.persist();
        }
        
        // Recalculate stats from all completed games
        List<Game> completedGames = Game.list("completed", true);
        for (Game game : completedGames) {
            if (game.homeScore != null && game.awayScore != null && 
                game.homeTeam != null && game.awayTeam != null) {
                // Re-attach teams to current session
                game.homeTeam = Team.findById(game.homeTeam.espnId);
                game.awayTeam = Team.findById(game.awayTeam.espnId);
                
                if (game.homeScore > game.awayScore) {
                    game.homeTeam.wins++;
                    game.awayTeam.losses++;
                } else {
                    game.awayTeam.wins++;
                    game.homeTeam.losses++;
                }
                
                game.homeTeam.pointsScored += game.homeScore;
                game.homeTeam.pointsAllowed += game.awayScore;
                game.awayTeam.pointsScored += game.awayScore;
                game.awayTeam.pointsAllowed += game.homeScore;
                
                game.homeTeam.persist();
                game.awayTeam.persist();
            }
        }
        
        LOG.infof("Recalculated stats for %d teams based on %d completed games", 
            allTeams.size(), completedGames.size());
    }

    /**
     * Startup method that runs when the application starts.
     * Fetches current and past season data if not already populated, then trains the model.
     */
    void onStart(@Observes StartupEvent ev) {
        LOG.info("Application starting - checking data and training model");
        
        try {
            // Check if we already have games in the database
            long gameCount = Game.count();
            int currentYear = java.time.Year.now().getValue();
            int currentMonth = java.time.LocalDate.now().getMonthValue();
            
            // Determine current season year
            int currentSeasonYear = currentYear;
            if (currentMonth >= 2 && currentMonth < 9) {
                currentSeasonYear = currentYear - 1;
            }
            
            if (gameCount == 0) {
                LOG.info("No games found in database. Fetching current and past season data...");
                
                // Fetch past season (last year)
                int pastSeasonYear = currentSeasonYear - 1;
                LOG.infof("Fetching games for past season: %d", pastSeasonYear);
                fetchAndStoreGamesForSeason(pastSeasonYear);
                
                // Fetch current season
                LOG.infof("Fetching games for current season: %d", currentSeasonYear);
                fetchAndStoreGamesForCurrentSeason();
                
                LOG.info("Data fetch completed. Recalculating team stats...");
                recalculateAllTeamStats();
            } else {
                LOG.infof("Found %d games in database. Skipping data fetch.", gameCount);
            }
            
            // Train the model if we have completed games
            List<Game> completedGames = Game.list("completed", true);
            if (!completedGames.isEmpty()) {
                LOG.infof("Training model on %d completed games...", completedGames.size());
                try {
                    modelTrainer.train(completedGames);
                    LOG.info("Model training completed successfully");
                } catch (Exception e) {
                    LOG.errorf(e, "Error training model: %s", e.getMessage());
                }
            } else {
                LOG.warn("No completed games found. Cannot train model.");
            }
            
        } catch (Exception e) {
            LOG.errorf(e, "Error during startup: %s", e.getMessage());
        }
    }
}
