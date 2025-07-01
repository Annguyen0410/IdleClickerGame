package com.zaigame.IdleClickerGame;

public class Skill {
    private final String id;
    private final String name;
    private final String description;
    private final SkillType type;
    private final int maxLevel;
    private final int costPerLevel;
    private int currentLevel;

    // This will define which skill needs to be unlocked first (null if it's a starting skill)
    private final String requiredSkillId;

    public Skill(String id, String name, String description, SkillType type, int maxLevel, int costPerLevel, String requiredSkillId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.maxLevel = maxLevel;
        this.costPerLevel = costPerLevel;
        this.requiredSkillId = requiredSkillId;
        this.currentLevel = 0;
    }

    public boolean canLevelUp() {
        return currentLevel < maxLevel;
    }

    public int getNextCost() {
        // Cost increases with each level
        return costPerLevel * (currentLevel + 1);
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkillType getType() { return type; }
    public int getMaxLevel() { return maxLevel; }
    public int getCurrentLevel() { return currentLevel; }
    public String getRequiredSkillId() { return requiredSkillId; }

    // Setter
    public void setCurrentLevel(int level) { this.currentLevel = level; }
}