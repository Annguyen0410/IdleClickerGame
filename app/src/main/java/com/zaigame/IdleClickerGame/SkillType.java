package com.zaigame.IdleClickerGame;

public enum SkillType {
    // Passive Bonuses
    CLICK_BONUS,           // Increases base click value
    SPS_PERCENT_BOOST,     // Flat % boost to all SPS
    UPGRADE_COST_REDUCTION, // Makes Celestial Objects cheaper
    MILESTONE_BOOST,       // Makes milestones more effective
    SINGULARITY_BOOST,     // Earn more Singularities per reset

    // Active Ability Modifiers
    BOOST_DURATION,        // Increases Shooting Star duration
    BOOST_POWER,           // Increases Shooting Star power
    RIFT_REWARD_BOOST,     // Cosmic Rifts give better rewards
    CRITICAL_CLICK_CHANCE, // Chance for super-powered clicks
    
    // NEW: Advanced Skills
    RESEARCH_COST_REDUCTION, // Makes research cheaper
    GALAXY_BOOST,          // Increases Galaxy Points earned
    EVENT_BOOST            // Increases event frequency

}