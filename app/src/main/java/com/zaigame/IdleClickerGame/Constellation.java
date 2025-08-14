package com.zaigame.IdleClickerGame;

public class Constellation {
    private final int id;
    private final String name;
    private final String description;
    private final ConstellationType type;
    private final double bonusValue;
    private final int requiredPoints;
    private boolean isUnlocked;
    
    public Constellation(int id, String name, String description, ConstellationType type, 
                        double bonusValue, int requiredPoints) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.bonusValue = bonusValue;
        this.requiredPoints = requiredPoints;
        this.isUnlocked = false;
    }
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ConstellationType getType() { return type; }
    public double getBonusValue() { return bonusValue; }
    public int getRequiredPoints() { return requiredPoints; }
    public boolean isUnlocked() { return isUnlocked; }
    
    // Setters
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
}

enum ConstellationType {
    CLICK_MULTIPLIER,    // Multiplies click value
    SPS_MULTIPLIER,      // Multiplies stardust per second
    UPGRADE_DISCOUNT,    // Reduces upgrade costs
    RESEARCH_SPEED,      // Reduces research costs
    RIFT_FREQUENCY,      // Increases rift spawn rate
    SINGULARITY_BONUS,   // Increases singularity gains
    AUTO_CLICK_SPEED,    // Increases auto-clicker speed
    COMBO_DURATION       // Increases combo timeout
}