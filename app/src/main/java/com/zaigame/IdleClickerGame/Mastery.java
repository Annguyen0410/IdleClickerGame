package com.zaigame.IdleClickerGame;

public class Mastery {
    private final int upgradeIndex;
    private int level;
    private int experience;
    
    // Experience needed for each level follows fibonacci-like progression
    private static final int[] XP_REQUIREMENTS = {
        100, 250, 500, 1000, 2000, 4000, 8000, 15000, 30000, 50000
    };
    
    public Mastery(int upgradeIndex) {
        this.upgradeIndex = upgradeIndex;
        this.level = 0;
        this.experience = 0;
    }
    
    public void addExperience(int amount) {
        experience += amount;
        checkLevelUp();
    }
    
    private void checkLevelUp() {
        if (level < XP_REQUIREMENTS.length && experience >= XP_REQUIREMENTS[level]) {
            level++;
            // Could add notification here for level up
        }
    }
    
    public double getBonusMultiplier() {
        // Each mastery level provides 5% bonus
        return 1.0 + (level * 0.05);
    }
    
    public int getExperienceForNextLevel() {
        if (level >= XP_REQUIREMENTS.length) return -1; // Max level
        return XP_REQUIREMENTS[level] - experience;
    }
    
    public double getProgressPercent() {
        if (level >= XP_REQUIREMENTS.length) return 100.0; // Max level
        if (level == 0) return (double) experience / XP_REQUIREMENTS[0] * 100.0;
        
        int prevXP = level > 0 ? XP_REQUIREMENTS[level - 1] : 0;
        int currentXP = XP_REQUIREMENTS[level];
        return (double) (experience - prevXP) / (currentXP - prevXP) * 100.0;
    }
    
    // Getters
    public int getUpgradeIndex() { return upgradeIndex; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    
    // Setters for save/load
    public void setLevel(int level) { this.level = level; }
    public void setExperience(int experience) { this.experience = experience; }
}