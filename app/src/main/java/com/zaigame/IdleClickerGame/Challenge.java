package com.zaigame.IdleClickerGame;

import java.math.BigInteger;

public class Challenge {
    private final String id;
    private final String name;
    private final String description;
    private final ChallengeType type;
    private final BigInteger targetValue;
    private final int rewardSingularities;
    private final int rewardConstellationPoints;
    private BigInteger currentProgress;
    private boolean isCompleted;
    private boolean isActive;
    
    public Challenge(String id, String name, String description, ChallengeType type,
                    BigInteger targetValue, int rewardSingularities, int rewardConstellationPoints) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.targetValue = targetValue;
        this.rewardSingularities = rewardSingularities;
        this.rewardConstellationPoints = rewardConstellationPoints;
        this.currentProgress = BigInteger.ZERO;
        this.isCompleted = false;
        this.isActive = false;
    }
    
    public boolean checkCompletion() {
        if (currentProgress.compareTo(targetValue) >= 0) {
            isCompleted = true;
            isActive = false;
            return true;
        }
        return false;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ChallengeType getType() { return type; }
    public BigInteger getTargetValue() { return targetValue; }
    public int getRewardSingularities() { return rewardSingularities; }
    public int getRewardConstellationPoints() { return rewardConstellationPoints; }
    public BigInteger getCurrentProgress() { return currentProgress; }
    public boolean isCompleted() { return isCompleted; }
    public boolean isActive() { return isActive; }
    
    // Setters
    public void setCurrentProgress(BigInteger progress) { this.currentProgress = progress; }
    public void addProgress(BigInteger amount) { this.currentProgress = currentProgress.add(amount); }
    public void setActive(boolean active) { this.isActive = active; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }
}

enum ChallengeType {
    EARN_STARDUST,           // Earn X stardust in one run
    REACH_SPS,               // Reach X SPS
    CLICK_COUNT,             // Click X times
    NO_UPGRADES_STARDUST,    // Earn X stardust without buying upgrades
    SPEED_RUN,               // Reach X stardust in Y seconds
    COMBO_CHAIN,             // Maintain X combo
    NO_CLICK_CHALLENGE,      // Earn X stardust without clicking
    RIFT_COLLECTOR           // Collect X rifts in one run
}