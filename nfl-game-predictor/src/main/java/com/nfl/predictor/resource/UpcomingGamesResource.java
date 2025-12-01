package com.nfl.predictor.resource;

import java.time.LocalDateTime;
import java.util.List;

import com.nfl.predictor.entity.Game;
import com.nfl.predictor.ml.ModelInference;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/upcoming-games")
public class UpcomingGamesResource {

    private static final int GAMES_PER_PAGE = 9;

    @Inject
    ModelInference modelInference;

    @Inject
    @Location("upcoming-games.html")
    Template upcomingGamesTemplate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getUpcomingGames(@QueryParam("page") @DefaultValue("0") int page) {
        LocalDateTime now = LocalDateTime.now();
        
        // Get all upcoming games (not completed and game date is in the future)
        List<Game> allUpcomingGames = Game.find(
            "completed = false and gameDate > ?1 and homeTeam is not null and awayTeam is not null order by gameDate",
            now
        ).list();
        
        // Filter out games with TBD teams (espnId = '-1' or '-2')
        allUpcomingGames = allUpcomingGames.stream()
            .filter(game -> game.homeTeam != null && game.awayTeam != null)
            .filter(game -> !"-1".equals(game.homeTeam.espnId) && !"-2".equals(game.homeTeam.espnId))
            .filter(game -> !"-1".equals(game.awayTeam.espnId) && !"-2".equals(game.awayTeam.espnId))
            .toList();
        
        // Make predictions for all games (we need to process all to get accurate predictions)
        for (Game game : allUpcomingGames) {
            if (game.homeTeam != null && game.awayTeam != null) {
                // Only predict if prediction doesn't exist
                if (game.predictedHomeWinProbability == null) {
                    try {
                        var result = modelInference.predict(game);
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
        
        // Calculate pagination
        int totalGames = allUpcomingGames.size();
        int totalPages = (int) Math.ceil((double) totalGames / GAMES_PER_PAGE);
        
        // Ensure page is within valid range
        if (page < 0) {
            page = 0;
        } else if (page >= totalPages && totalPages > 0) {
            page = totalPages - 1;
        }
        
        // Get games for current page
        int startIndex = page * GAMES_PER_PAGE;
        int endIndex = Math.min(startIndex + GAMES_PER_PAGE, totalGames);
        List<Game> gamesForPage = allUpcomingGames.subList(startIndex, endIndex);
        
        // Calculate page numbers to display (show up to 5 pages around current)
        record PageInfo(int pageIndex, int displayNumber) {}
        List<PageInfo> pageInfos = new java.util.ArrayList<>();
        int maxVisiblePages = 5;
        int startPage = Math.max(0, page - maxVisiblePages / 2);
        int endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
        
        // Adjust if we're near the end
        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = Math.max(0, endPage - maxVisiblePages + 1);
        }
        
        for (int i = startPage; i <= endPage; i++) {
            pageInfos.add(new PageInfo(i, i + 1));
        }
        
        return upcomingGamesTemplate.data("games", gamesForPage)
                .data("currentPage", page)
                .data("totalPages", totalPages)
                .data("totalGames", totalGames)
                .data("startIndex", startIndex + 1)
                .data("endIndex", endIndex)
                .data("pageInfos", pageInfos)
                .data("showFirstPage", startPage > 0)
                .data("showLastPage", endPage < totalPages - 1)
                .data("showFirstEllipsis", startPage > 1)
                .data("showLastEllipsis", endPage < totalPages - 2)
                .data("lastPageIndex", totalPages - 1)
                .data("nextPage", page + 1)
                .data("prevPage", page - 1)
                .data("paginationRange", String.format("%d-%d of %d", startIndex + 1, endIndex, totalGames));
    }
}

