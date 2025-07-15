package org.acme;

public class GameResponse {
    private final String narrative;
    private final Player player;

    public GameResponse(String narrative, Player player) {
        this.narrative = narrative;
        this.player = player;
    }

    // Getters
    public String getNarrative() {
        return narrative;
    }

    public Player getPlayer() {
        return player;
    }
}