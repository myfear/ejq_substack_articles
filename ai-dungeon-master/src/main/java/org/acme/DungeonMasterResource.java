package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * RESTful resource for managing dungeon master game interactions.
 * <p>
 * This resource provides endpoints for starting a new game and processing player actions
 * in an AI-driven dungeon master game. It maintains game state including player information
 * and conversation memory throughout the game session.
 * </p>
 * <p>
 * The resource is application-scoped, meaning a single instance is shared across all requests,
 * which allows state to be maintained across multiple interactions within the same game session.
 * </p>
 *
 * @see GameMaster
 * @see Player
 * @see GameResponse
 */
@Path("/dungeon")
@ApplicationScoped 
public class DungeonMasterResource {

    /**
     * The game master instance that handles AI-driven narrative generation
     * and game mechanics through conversational interactions.
     */
    @Inject
    GameMaster gameMaster;
    
    /**
     * Provider for managing the current player context across different
     * parts of the application, particularly for tool functions.
     */
    @Inject
    PlayerProvider playerProvider;

    /**
     * The current player instance containing character stats, inventory,
     * and other player-specific state information.
     */
    private Player player = new Player();
    
    /**
     * Memory buffer that stores the conversation history between the player
     * and the dungeon master, including all actions and narrative responses.
     */
    private final StringBuilder memory = new StringBuilder();

    /**
     * Starts a new dungeon master game session.
     * <p>
     * This endpoint initializes a fresh game by:
     * <ul>
     *   <li>Resetting the player to default starting state</li>
     *   <li>Clearing all previous conversation memory</li>
     *   <li>Generating an engaging starting scenario in a fantasy tavern</li>
     * </ul>
     * </p>
     *
     * @return a {@link GameResponse} containing the initial narrative and player state
     */
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

    /**
     * Processes a player action and generates the corresponding narrative response.
     * <p>
     * This endpoint handles player actions by:
     * <ul>
     *   <li>Providing the current player status to the game master</li>
     *   <li>Including previous conversation history for context</li>
     *   <li>Generating an appropriate narrative response based on the action</li>
     *   <li>Updating the conversation memory with both the action and response</li>
     * </ul>
     * </p>
     *
     * @param action the player's action or choice as a string
     * @return a {@link GameResponse} containing the narrative response and updated player state
     */
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