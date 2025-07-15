package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/dungeon")
@ApplicationScoped 
public class DungeonMasterResource {

    @Inject
    GameMaster gameMaster;
    
    @Inject
    PlayerProvider playerProvider;

    private Player player = new Player();
    private final StringBuilder memory = new StringBuilder();

    @POST
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public GameResponse startGame() {
        this.player = new Player(); // Reset player for a new game
        memory.setLength(0); // Clear memory
        playerProvider.setCurrentPlayer(this.player); // Set current player for tools

        String startingPrompt = "The player has started a new game. Provide an engaging starting scenario in a fantasy tavern and present the first choice.";
        String narrative = gameMaster.chat(startingPrompt);
        memory.append("DM: ").append(narrative).append("\n");
        return new GameResponse(narrative, this.player);
    }

    @POST
    @Path("/action")
    @Produces(MediaType.APPLICATION_JSON)
    public GameResponse performAction(String action) {
        playerProvider.setCurrentPlayer(this.player); // Set current player for tools
        
        String playerStatus = "Current Player Status: " + player.getStatusSummary() + "\n";
        String fullPrompt = playerStatus + "Previous events:\n" + memory.toString() + "\nPlayer action: " + action;

        String narrative = gameMaster.chat(fullPrompt);

        // Append to memory
        memory.append("Player: ").append(action).append("\n");
        memory.append("DM: ").append(narrative).append("\n");

        return new GameResponse(narrative, this.player);
    }
}