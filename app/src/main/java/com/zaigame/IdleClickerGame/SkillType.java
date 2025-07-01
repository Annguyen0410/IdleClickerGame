package com.zaigame.IdleClickerGame;

public enum SkillType {
    // Passive Bonuses
    CLICK_BONUS,           // Increases base click value
    SPS_PERCENT_BOOST,     // Flat % boost to all SPS
    UPGRADE_COST_REDUCTION, // Makes Celestial Objects cheaper
    MILESTONE_BOOST,       // Makes milestones more effective
    SINGULARITY_BOOST,     // Earn more Singularities per reset

    // Active Ability Modifiers (we can implement these later)
    BOOST_DURATION,        // Increases Shooting Star duration
    BOOST_POWER,           // Increases Shooting Star power
    RIFT_REWARD_BOOST,      // Cosmic Rifts give better rewards
    CRITICAL_CLICK_CHANCE // NEW

}