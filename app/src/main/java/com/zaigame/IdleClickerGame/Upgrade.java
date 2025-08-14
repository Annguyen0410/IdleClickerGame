package com.zaigame.IdleClickerGame;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Upgrade {
    public static final int MILESTONE_INTERVAL = 25;
    public static final int TIER_UP_LEVEL = 100;

    String name;
    int count;
    BigInteger baseCost;
    // --- FIX: Changed baseCps from int to long to support larger numbers ---
    long baseCps;

    // --- FIX: Updated the constructor to accept a long ---
    public Upgrade(String name, BigInteger baseCost, long cps) {
        this.name = name;
        this.baseCost = baseCost;
        this.baseCps = cps;
        this.count = 0;
    }

    public String getCurrentName() {
        if (count >= TIER_UP_LEVEL && this.name.equals("Asteroid Catcher")) {
            return "Asteroid Harvester";
        }
        return this.name;
    }

    public int getCurrentIcon() {
        if (count >= TIER_UP_LEVEL && this.name.equals("Asteroid Catcher")) {
            return R.drawable.ic_asteroid_harvester;
        }
        return R.drawable.ic_star_shower;
    }

    public BigInteger getNextCost(boolean isCheaper) {
        BigDecimal cost = new BigDecimal(baseCost).multiply(BigDecimal.valueOf(Math.pow(1.15, count)));
        if (isCheaper) {
            cost = cost.multiply(new BigDecimal("0.95"));
        }
        return cost.toBigInteger();
    }

    public BigDecimal getTotalCpsForThisUpgrade(boolean hasHarmonicResonance, int celestialArchitectureLevel) {
        if (count == 0) return BigDecimal.ZERO;

        // Dynamic milestone interval based on skill
        int primaryMilestoneInterval = MILESTONE_INTERVAL;
        int secondaryMilestoneInterval = 175; // The new milestone from the skill

        int primaryMilestonesPassed = count / primaryMilestoneInterval;
        // Only count secondary milestones if the skill is unlocked
        int secondaryMilestonesPassed = (celestialArchitectureLevel > 0) ? (count / secondaryMilestoneInterval) : 0;

        double milestoneMultiplier = hasHarmonicResonance ? 2.5 : 2.0;

        // Calculate the total bonus from all milestones
        BigDecimal totalMultiplier = BigDecimal.valueOf(Math.pow(milestoneMultiplier, primaryMilestonesPassed + secondaryMilestonesPassed));

        return BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(baseCps)).multiply(totalMultiplier);
    }
    public BigInteger getCostForMultiple(int amount, boolean isCheaper) {
        BigDecimal totalCost = BigDecimal.ZERO;

        // Temporarily store the original count
        int originalCount = this.count;

        // Simulate buying 'amount' of items to calculate total cost
        for (int i = 0; i < amount; i++) {
            BigDecimal nextCost = new BigDecimal(baseCost).multiply(BigDecimal.valueOf(Math.pow(1.15, this.count)));
            totalCost = totalCost.add(nextCost);
            this.count++; // Increment count to get the cost of the *next* item
        }

        // IMPORTANT: Reset the count back to its original value
        this.count = originalCount;

        if (isCheaper) {
            totalCost = totalCost.multiply(new BigDecimal("0.95"));
        }

        return totalCost.toBigInteger();
    }

    // Getter for base cost
    public BigInteger getBaseCost() {
        return baseCost;
    }
}