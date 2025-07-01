package com.zaigame.IdleClickerGame;

public enum EventType {
    DOUBLE_STARDUST("Double Stardust", "All stardust gains are doubled!", 2.0, 0),
    TRIPLE_CLICKS("Triple Clicks", "Click value is tripled!", 0, 3.0),
    RIFT_RUSH("Rift Rush", "Rifts appear 5x more frequently!", 0, 0),
    DISCOUNT_DAY("Discount Day", "All upgrades are 50% cheaper!", 0.5, 0),
    SINGULARITY_SURGE("Singularity Surge", "Singularity gains are increased by 50%!", 1.5, 0),
    COMBO_FRENZY("Combo Frenzy", "Combo timeout is doubled and combo multiplier increased!", 0, 0);
    
    private final String name;
    private final String description;
    private final double costMultiplier;
    private final double clickMultiplier;
    
    EventType(String name, String description, double costMultiplier, double clickMultiplier) {
        this.name = name;
        this.description = description;
        this.costMultiplier = costMultiplier;
        this.clickMultiplier = clickMultiplier;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getCostMultiplier() { return costMultiplier; }
    public double getClickMultiplier() { return clickMultiplier; }
}