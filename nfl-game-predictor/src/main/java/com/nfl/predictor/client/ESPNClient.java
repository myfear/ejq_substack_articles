package com.nfl.predictor.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * ESPN Client for NFL data using the unofficial ESPN API.
 * 
 * Base URL: http://site.api.espn.com/apis/site/v2
 * Documentation: https://github.com/pseudo-r/Public-ESPN-API
 * 
 * Note: This is an unofficial, undocumented API that may change without notice.
 */
@RegisterRestClient(configKey = "espn-api")
@Path("/sports/football/nfl")
public interface ESPNClient {
    
    /**
     * Get scoreboard for specific date(s).
     * According to the unofficial ESPN API documentation:
     * - dates: YYYYMMDD format, or range YYYYMMDD-YYYYMMDD
     * - seasontype: Optional (1=preseason, 2=regular season, 3=postseason)
     * - limit: Optional limit on number of results (default varies, max ~1000)
     * 
     * @param dates Date in YYYYMMDD format, or range YYYYMMDD-YYYYMMDD
     * @param seasonType Optional season type (1=preseason, 2=regular, 3=postseason)
     * @param limit Optional limit on number of results
     * @return Scoreboard response with events/games
     */
    @GET
    @Path("/scoreboard")
    @Produces(MediaType.APPLICATION_JSON)
    ESPNScoreboardResponse getScoreboard(
            @QueryParam("dates") String dates,
            @QueryParam("seasontype") Integer seasonType,
            @QueryParam("limit") Integer limit);
    
    /**
     * Get all NFL teams.
     * 
     * @return Teams response with all NFL teams
     */
    @GET
    @Path("/teams")
    @Produces(MediaType.APPLICATION_JSON)
    ESPNTeamsResponse getTeams();
}