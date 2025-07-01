package com.zaigame.IdleClickerGame;

public class Achievement {
    private final String id;
    private final String name;
    private final String description;
    private final AchievementType type;
    private final long goal;
    private final int targetUpgradeIndex; // Used only for UPGRADE_COUNT type, -1 otherwise
    private boolean isUnlocked;

    // Constructor for most achievement types
    public Achievement(String id, String name, String description, AchievementType type, long goal) {
        this(id, name, description, type, goal, -1);
    }

    // Constructor specifically for UPGRADE_COUNT
    public Achievement(String id, String name, String description, AchievementType type, long goal, int targetUpgradeIndex) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.goal = goal;
        this.targetUpgradeIndex = targetUpgradeIndex;
        this.isUnlocked = false;
    }

    // --- Getters and Setters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AchievementType getType() { return type; }
    public long getGoal() { return goal; }
    public int getTargetUpgradeIndex() { return targetUpgradeIndex; }
    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
}