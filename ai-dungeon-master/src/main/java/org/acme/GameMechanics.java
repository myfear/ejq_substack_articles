package org.acme;

import java.util.Random;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GameMechanics {

    private final Random random = new Random();

    @Inject
    PlayerProvider playerProvider;

    @Tool("Performs a skill check for a given attribute (strength, dexterity, or intelligence). Returns true for success, false for failure.")
    public boolean performSkillCheck(String attribute) {
        Player player = playerProvider.getCurrentPlayer();
        int attributeValue;
        switch (attribute.toLowerCase()) {
            case "strength":
                attributeValue = player.getStrength();
                break;
            case "dexterity":
                attributeValue = player.getDexterity();
                break;
            case "intelligence":
                attributeValue = player.getIntelligence();
                break;
            default:
                attributeValue = 10; // Neutral check for unknown attributes
        }

        // Classic D&D-style check: d20 + attribute modifier vs. a Difficulty Class (DC)
        int modifier = (attributeValue - 10) / 2;
        int diceRoll = random.nextInt(20) + 1; // A d20 roll
        int difficultyClass = 12; // A medium difficulty

        boolean success = (diceRoll + modifier) >= difficultyClass;

        System.out.printf("--- Skill Check (%s): Roll (%d) + Modifier (%d) vs DC (%d) -> %s ---%n",
                attribute, diceRoll, modifier, difficultyClass, success ? "SUCCESS" : "FAILURE");

        return success;
    }
}