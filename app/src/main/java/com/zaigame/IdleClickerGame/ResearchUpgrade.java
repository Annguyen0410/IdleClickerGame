package com.zaigame.IdleClickerGame;

import java.math.BigInteger;

public class ResearchUpgrade {
    private final String id;
    private final String name;
    private final String description;
    private final BigInteger cost; // Changed to BigInteger
    private boolean isPurchased;

    public ResearchUpgrade(String id, String name, String description, BigInteger cost) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.isPurchased = false;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigInteger getCost() { return cost; }
    public boolean isPurchased() { return isPurchased; }

    // Setter
    public void setPurchased(boolean purchased) { isPurchased = purchased; }
}