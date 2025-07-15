package org.acme;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Player {

    private int hp;
    private final int maxHp;
    private final int strength;
    private final int dexterity;
    private final int intelligence;
    private final List<String> inventory;

    public Player() {
        this.maxHp = 20;
        this.hp = 20;
        this.strength = 14;
        this.dexterity = 12;
        this.intelligence = 10;
        this.inventory = new ArrayList<>();
        this.inventory.add("a rusty sword");
        this.inventory.add("a healing potion");
    }

    // This constructor is for JSON deserialization
    @JsonCreator
    public Player(@JsonProperty("hp") int hp, @JsonProperty("maxHp") int maxHp,
            @JsonProperty("strength") int strength, @JsonProperty("dexterity") int dexterity,
            @JsonProperty("intelligence") int intelligence, @JsonProperty("inventory") List<String> inventory) {
        this.hp = hp;
        this.maxHp = maxHp;
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.inventory = inventory;
    }

    public String getStatusSummary() {
        return String.format(
                "HP: %d/%d, Strength: %d, Dexterity: %d, Intelligence: %d, Inventory: [%s]",
                hp, maxHp, strength, dexterity, intelligence, String.join(", ", inventory));
    }

    // Standard Getters
    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getStrength() {
        return strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public List<String> getInventory() {
        return inventory;
    }

    // Methods to modify player state
    public void takeDamage(int amount) {
        this.hp = Math.max(0, this.hp - amount);
    }

    public void heal(int amount) {
        this.hp = Math.min(this.maxHp, this.hp + amount);
    }
}
