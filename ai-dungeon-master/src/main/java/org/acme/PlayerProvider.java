package org.acme;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlayerProvider {

    private Player currentPlayer;

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }
} 