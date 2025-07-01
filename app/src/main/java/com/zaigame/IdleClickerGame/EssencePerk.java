package com.zaigame.IdleClickerGame;

import java.io.Serializable;

public class EssencePerk implements Serializable {
    private final String id;
    private final String name;
    private final String description;
    private final int costEssence;
    private boolean purchased;

    public EssencePerk(String id, String name, String description, int costEssence) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.costEssence = costEssence;
        this.purchased = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCostEssence() { return costEssence; }
    public boolean isPurchased() { return purchased; }
    public void setPurchased(boolean purchased) { this.purchased = purchased; }
}