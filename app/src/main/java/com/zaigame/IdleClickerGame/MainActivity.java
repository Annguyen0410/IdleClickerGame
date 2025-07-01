package com.zaigame.IdleClickerGame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // --- UI Elements ---
    private TextView tvStardustCount, tvSps, tvSingularityCount;
    private FloatingActionButton btnClickStar, btnSettings, btnAchievements;
    private Button btnSupernova, btnShootingStar, btnSkillTree, btnBigCrunch; // MODIFIED: Added btnBigCrunch
    private Button btnAutoClicker; // NEW: Auto-clicker button
    private Button btnBuy1, btnBuy10, btnBuy25; // NEW: Buy amount buttons
    private LinearLayout llUpgradesContainer;
    private ConstraintLayout rootLayout;
    private TabLayout tabLayout;

    // --- NEW: View Caching Maps ---
    private final Map<Upgrade, View> upgradeViewCache = new HashMap<>();
    private final Map<ResearchUpgrade, View> researchViewCache = new HashMap<>();

    // --- NEW: Caching for displayed text ---
    private String lastDisplayedStardust = "";
    private String lastDisplayedSps = "";

    // --- Game State Variables ---
    private BigInteger stardustCount = BigInteger.ZERO;
    private long totalSps = 0;
    private int singularityCount = 0;
    private final double singularityBonus = 0.05;
    private final BigInteger supernovaCost = new BigInteger("1000000000000");

    // --- NEW: Big Crunch Variables ---
    private int cosmicEssence = 0;
    private final int BIG_CRUNCH_COST = 10000; // Cost in Singularities

    // --- Feature: Shooting Star Boost ---
    private boolean isBoostActive = false;
    private int boostMultiplier = 1;

    // --- Game Data Lists ---
    private final List<Upgrade> upgrades = new ArrayList<>();
    private final List<ResearchUpgrade> researchUpgrades = new ArrayList<>();
    private final List<Achievement> achievements = new ArrayList<>();
    private final List<Skill> skills = new ArrayList<>();
    private final List<Mastery> masteries = new ArrayList<>(); // NEW: Mastery system

    // --- Handlers ---
    private final Handler gameLoopHandler = new Handler();
    private final Handler twinkleHandler = new Handler();
    private final Handler riftHandler = new Handler();
    private Runnable riftSpawnerRunnable;
    private Handler autoSaveHandler = new Handler();
    private Runnable autoSaveRunnable;

    // --- Other Game State ---
    private int currentBuyAmount = 1; // NEW: To track buy amount (1, 10, or 25)
    private final Random random = new Random();
    private long totalClicks = 0;
    private BigInteger totalStardustEverEarned = BigInteger.ZERO;
    private boolean soundEnabled = true;

    private SharedPreferences prefs;

    // --- Sound Effects ---
    private SoundPool soundPool;
    private int clickSoundId, upgradeSoundId, milestoneSoundId, achievementSoundId, riftSoundId;

    // --- NEW: Statistics Variables ---
    private int totalSupernovas = 0;
    private int totalBigCrunches = 0;
    private int totalRiftsTapped = 0;
    private long gameStartTime = 0;

    // --- NEW: SharedPreferences Keys ---
    private static final String PREFS_NAME = "CosmicClickerPrefs";
    private static final String KEY_STARDUST = "stardustCount";
    private static final String KEY_SINGULARITY = "singularityCount";
    private static final String KEY_COSMIC_ESSENCE = "cosmicEssence"; // NEW
    private static final String KEY_AUTO_CLICKER_LEVEL = "autoClickerLevel"; // NEW
    private static final String KEY_TOTAL_SUPERNOVAS = "totalSupernovas"; // NEW
    private static final String KEY_TOTAL_BIG_CRUNCHES = "totalBigCrunches"; // NEW
    private static final String KEY_TOTAL_RIFTS_TAPPED = "totalRiftsTapped"; // NEW
    private static final String KEY_GAME_START_TIME = "gameStartTime"; // NEW
    private static final String KEY_MASTERY_LEVEL_PREFIX = "mastery_level_"; // NEW
    private static final String KEY_MASTERY_XP_PREFIX = "mastery_xp_"; // NEW
    private static final String KEY_UPGRADE_COUNT_PREFIX = "upgrade_count_";
    private static final String KEY_RESEARCH_PREFIX = "research_";
    private static final String KEY_ACHIEVEMENT_PREFIX = "achievement_";
    private static final String KEY_SKILL_LEVEL_PREFIX = "skill_level_";
    private static final String KEY_LAST_ONLINE_TIME = "lastOnlineTime";
    private static final String KEY_LAST_DAILY_REWARD = "lastDailyReward";
    private static final String KEY_TOTAL_CLICKS = "totalClicks";
    private static final String KEY_TOTAL_STARDUST_EARNED = "totalStardustEarned";
    private static final long DAILY_REWARD_AMOUNT = 1000000000;

    // --- NEW: Auto-Clicker Variables ---
    private boolean autoClickerActive = false;
    private int autoClickerLevel = 0;
    private final int AUTO_CLICKER_BASE_COST = 1000000; // 1 Million stardust
    private final Handler autoClickHandler = new Handler();
    private Runnable autoClickRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        setupSoundPool();
        setupUpgrades();
        setupResearchUpgrades();
        setupAchievements();
        setupSkills();
        setupMasteries(); // NEW: Setup mastery system

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadGame();

        setupClickListeners();
        setupTabs();

        startGameLoop();
        startAutoSave();

        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        createTwinklingStars(15);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
    }

    // --- Lifecycle Methods ---
    @Override
    protected void onPause() {
        super.onPause();
        saveGame();
        gameLoopHandler.removeCallbacksAndMessages(null);
        riftHandler.removeCallbacks(riftSpawnerRunnable);
        twinkleHandler.removeCallbacksAndMessages(null);
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        stopAutoClicker(); // NEW: Stop auto-clicker when paused
    }
    @Override
    protected void onResume() {
        super.onResume();
        calculateOfflineProgress();
        checkDailyReward();
        startGameLoop();
        startRiftSpawner();
        startAutoSave();
        createTwinklingStars(15);
        if (autoClickerLevel > 0) startAutoClicker(); // NEW: Restart auto-clicker when resumed
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameLoopHandler.removeCallbacksAndMessages(null);
        riftHandler.removeCallbacks(riftSpawnerRunnable);
        twinkleHandler.removeCallbacksAndMessages(null);
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        autoClickHandler.removeCallbacksAndMessages(null); // NEW: Clean up auto-clicker
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    // --- Initialization Methods ---
    private void initUI() {
        rootLayout = findViewById(R.id.root_layout);
        tvStardustCount = findViewById(R.id.tvStardustCount);
        tvSps = findViewById(R.id.tvSps);
        tvSingularityCount = findViewById(R.id.tvSingularityCount);
        btnClickStar = findViewById(R.id.btnClickStar);
        btnSettings = findViewById(R.id.btnSettings);
        btnAchievements = findViewById(R.id.btnAchievements);
        btnSkillTree = findViewById(R.id.btnSkillTree);
        btnSupernova = findViewById(R.id.btnSupernova);
        btnShootingStar = findViewById(R.id.btnShootingStar);
        btnBigCrunch = findViewById(R.id.btnBigCrunch); // NEW
        btnAutoClicker = findViewById(R.id.btnAutoClicker); // NEW
        llUpgradesContainer = findViewById(R.id.llUpgradesContainer);
        tabLayout = findViewById(R.id.tabLayout);

        // --- NEW: Find the buy amount toggle buttons ---
        btnBuy1 = findViewById(R.id.btnBuy1);
        btnBuy10 = findViewById(R.id.btnBuy10);
        btnBuy25 = findViewById(R.id.btnBuy25);
    }
    private void setupClickListeners() {
        btnClickStar.setOnClickListener(v -> onStarClicked());
        btnSupernova.setOnClickListener(v -> triggerSupernova());
        btnShootingStar.setOnClickListener(v -> activateShootingStar());
        btnSettings.setOnClickListener(v -> showSettingsDialog());
        btnAchievements.setOnClickListener(v -> showAchievementsDialog());
        btnSkillTree.setOnClickListener(v -> showSkillTreeDialog());
        btnBigCrunch.setOnClickListener(v -> triggerBigCrunch()); // NEW

        // --- NEW: Listeners for buy amount toggles ---
        btnBuy1.setOnClickListener(v -> setBuyAmount(1));
        btnBuy10.setOnClickListener(v -> setBuyAmount(10));
        btnBuy25.setOnClickListener(v -> setBuyAmount(25));

        // --- NEW: Auto-clicker button click listener ---
        btnAutoClicker.setOnClickListener(v -> upgradeAutoClicker());
    }
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Celestial Objects"));
        tabLayout.addTab(tabLayout.newTab().setText("Research"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) refreshCelestialObjectsUI(); else refreshResearchUI();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        refreshCelestialObjectsUI();
    }
    private void setupUpgrades() {
        upgrades.clear();
        upgrades.add(new Upgrade("Asteroid Catcher", new BigInteger("15"), 1));
        upgrades.add(new Upgrade("Comet Collector", new BigInteger("100"), 8));
        upgrades.add(new Upgrade("Gas Giant Siphon", new BigInteger("1100"), 47));
        upgrades.add(new Upgrade("Dyson Swarm", new BigInteger("12000"), 260));
        upgrades.add(new Upgrade("Nebula Nursery", new BigInteger("130000"), 1400));
        upgrades.add(new Upgrade("Pulsar Beam", new BigInteger("1400000"), 7800));
        upgrades.add(new Upgrade("Black Hole Mine", new BigInteger("20000000"), 44000));
        upgrades.add(new Upgrade("Star Cluster", new BigInteger("330000000"), 260000));
        upgrades.add(new Upgrade("Galactic Filament", new BigInteger("5100000000"), 1600000));
        upgrades.add(new Upgrade("Supercluster Lens", new BigInteger("75000000000"), 10000000));
        upgrades.add(new Upgrade("Cosmic Web Weaver", new BigInteger("1100000000000"), 65000000));
        upgrades.add(new Upgrade("Reality Catalyst", new BigInteger("210000000000000"), 450000000));

        // --- NEW ENDGAME UPGRADES ---
        upgrades.add(new Upgrade("Dimension Forge", new BigInteger("54000000000000000"), 3600000000L)); // 5.4e16
        upgrades.add(new Upgrade("Time-Space Anomaly", new BigInteger("8800000000000000000"), 250000000000L)); // 8.8e18
        upgrades.add(new Upgrade("Omniverse Core", new BigInteger("13000000000000000000000"), 19000000000000L)); // 1.3e22
    }
    private void setupResearchUpgrades() {
        researchUpgrades.clear();
        researchUpgrades.add(new ResearchUpgrade("research_click_sps", "Stardust Infusion", "Each click also generates 1% of your SPS.", new BigInteger("50000")));
        researchUpgrades.add(new ResearchUpgrade("research_sps_boost", "Advanced Lensing", "All Celestial Objects produce 10% more Stardust.", new BigInteger("250000")));
        researchUpgrades.add(new ResearchUpgrade("research_milestone", "Harmonic Resonance", "Milestone bonuses are 25% more effective (x2.5 instead of x2).", new BigInteger("100000000")));
        researchUpgrades.add(new ResearchUpgrade("research_cheaper", "Cosmic Bargaining", "All upgrades and research are 5% cheaper.", new BigInteger("50000000000")));

        // --- NEW RESEARCH ---
        researchUpgrades.add(new ResearchUpgrade("research_rift_doubler", "Rift Stabilization", "Cosmic Rift rewards are permanently doubled.", new BigInteger("1000000000000000"))); // 1 Quadrillion
    }
    private void setupAchievements() {
        achievements.clear();
        achievements.add(new Achievement("click_1", "First Tap", "Click the star for the first time", AchievementType.TOTAL_CLICKS, 1));
        achievements.add(new Achievement("click_100", "Novice Tapper", "Click the star 100 times", AchievementType.TOTAL_CLICKS, 100));
        achievements.add(new Achievement("stardust_1k", "Stardust Hoarder", "Earn 1,000 total stardust", AchievementType.TOTAL_STARDUST, 1000));
        achievements.add(new Achievement("asteroid_1", "Rock Collector", "Buy your first Asteroid Catcher", AchievementType.UPGRADE_COUNT, 1, 0));
        achievements.add(new Achievement("asteroid_25", "Asteroid Belt", "Own 25 Asteroid Catchers", AchievementType.UPGRADE_COUNT, 25, 0));
        achievements.add(new Achievement("comet_1", "Comet Chaser", "Buy your first Comet Collector", AchievementType.UPGRADE_COUNT, 1, 1));
        achievements.add(new Achievement("sps_100", "Cosmic Engine", "Reach 100 Stardust per second", AchievementType.REACH_SPS, 100));

        // --- NEW ACHIEVEMENTS ---
        achievements.add(new Achievement("supernova_1", "Star-Breaker", "Perform your first Supernova.", AchievementType.SINGULARITY_COUNT, 1));
        achievements.add(new Achievement("research_1", "A New Age", "Purchase your first Research upgrade.", AchievementType.RESEARCH_COUNT, 1));
        achievements.add(new Achievement("essence_1", "The Beginning is the End", "Initiate your first Big Crunch.", AchievementType.ESSENCE_COUNT, 1));
        achievements.add(new Achievement("forge_1", "Dimensional Engineer", "Build a Dimension Forge.", AchievementType.UPGRADE_COUNT, 1, 12)); // Index 12 is Dimension Forge
        
        // --- NEW: Auto-Clicker and Mastery Achievements ---
        achievements.add(new Achievement("auto_clicker_1", "Automated Assistant", "Activate your first Auto-Clicker.", AchievementType.AUTO_CLICKER_LEVEL, 1));
        achievements.add(new Achievement("auto_clicker_5", "Clicking Machine", "Upgrade Auto-Clicker to level 5.", AchievementType.AUTO_CLICKER_LEVEL, 5));
        achievements.add(new Achievement("mastery_1", "First Mastery", "Reach mastery level 1 on any upgrade.", AchievementType.MASTERY_COUNT, 1));
        achievements.add(new Achievement("mastery_10", "Master of the Cosmos", "Reach mastery level 5 on any upgrade.", AchievementType.MASTERY_COUNT, 5));
        achievements.add(new Achievement("rifts_10", "Rift Walker", "Tap 10 Cosmic Rifts.", AchievementType.RIFTS_TAPPED, 10));
    }
    private void setupSkills() {
        skills.clear();
        // ID, Name, Description, Type, MaxLevel, CostPerLevel, RequiredSkillID

        // --- Path of the Architect (Idle) ---
        skills.add(new Skill("sps_1", "Cosmic Currents", "+2% total SPS per level.", SkillType.SPS_PERCENT_BOOST, 10, 1, null));
        skills.add(new Skill("cost_1", "Efficient Construction", "Celestial Objects are 1% cheaper per level.", SkillType.UPGRADE_COST_REDUCTION, 5, 2, "sps_1"));
        skills.add(new Skill("arch_1", "Celestial Architecture", "Unlocks a new milestone at 175 for all objects.", SkillType.MILESTONE_BOOST, 1, 100, "cost_1"));

        // --- Path of the Harbinger (Active) ---
        skills.add(new Skill("click_1", "Reinforced Fingertips", "Base click value +1 per level.", SkillType.CLICK_BONUS, 5, 1, null));
        skills.add(new Skill("crit_1", "Critical Clicks", "+1% chance per level for clicks to be 1000x stronger.", SkillType.CRITICAL_CLICK_CHANCE, 5, 5, "click_1"));
        skills.add(new Skill("rift_1", "Rift Magnetism", "Rifts appear 10% more often per level.", SkillType.RIFT_REWARD_BOOST, 3, 20, "crit_1"));

        // --- Path of the Scholar (Prestige) ---
        skills.add(new Skill("sing_1", "Supernova Insight", "+2% Singularities earned per level.", SkillType.SINGULARITY_BOOST, 10, 3, null));
        skills.add(new Skill("sing_2", "Singularity Attunement", "Singularity SPS bonus is +1% stronger per level.", SkillType.SINGULARITY_BOOST, 5, 50, "sing_1"));
    }
    private void setupMasteries() {
        masteries.clear();
        // Create a mastery for each upgrade
        for (int i = 0; i < upgrades.size(); i++) {
            masteries.add(new Mastery(i));
        }
    }

    // --- Core Game Logic ---
    private void onStarClicked() {
        animateClick(btnClickStar);
        BigInteger clickValue = BigInteger.ONE;
        clickValue = clickValue.add(BigInteger.valueOf(getSkillLevel("click_1")));
        if (getResearch("research_click_sps").isPurchased()) {
            clickValue = clickValue.add(BigInteger.valueOf(totalSps / 100));
        }
        showStardustPopup(clickValue, btnClickStar);
        
        // NEW: Enhanced particle effects on click
        createEnhancedClickParticles(
            btnClickStar.getX() + btnClickStar.getWidth() / 2f,
            btnClickStar.getY() + btnClickStar.getHeight() / 2f,
            clickValue
        );
        
        stardustCount = stardustCount.add(clickValue);
        totalClicks++;
        totalStardustEverEarned = totalStardustEverEarned.add(clickValue);
        playClickSound();
        updateUI();
        checkAchievements();
    }
    private void buyUpgrade(Upgrade upgrade) {
        // Call the powerful buyMultiple method with the currently selected amount
        buyMultiple(upgrade, currentBuyAmount);
    }

    // NEW: "Buy Multiple" logic foundation
    private void buyMultiple(Upgrade upgrade, int amount) {
        // First, check if we can afford the total cost. This prevents partial buys.
        BigInteger totalCost = calculateCostForNUpgrades(upgrade, amount, getResearch("research_cheaper").isPurchased());
        if (stardustCount.compareTo(totalCost) >= 0) {
            stardustCount = stardustCount.subtract(totalCost);
            
            // Find upgrade index for mastery
            int upgradeIndex = upgrades.indexOf(upgrade);
            
            // Now, actually increment the counts
            for (int i = 0; i < amount; i++) {
                if (upgrade.count % Upgrade.MILESTONE_INTERVAL == 0 && i > 0) { // Check for milestones within the bulk purchase
                    playMilestoneSound();
                }
                upgrade.count++;
                
                // NEW: Add mastery experience (more XP for expensive upgrades)
                if (upgradeIndex >= 0 && upgradeIndex < masteries.size()) {
                    int xpGain = Math.max(1, upgradeIndex + 1); // Later upgrades give more XP
                    masteries.get(upgradeIndex).addExperience(xpGain);
                }
            }
            playUpgradeSound();
            recalculateSps();
            refreshCelestialObjectsUI();
            updateUI();
            checkAchievements();
        } else {
            Toast.makeText(this, "Not enough stardust!", Toast.LENGTH_SHORT).show();
        }
    }

    private void buyResearch(ResearchUpgrade research) {
        BigInteger cost = research.getCost();
        if (getResearch("research_cheaper").isPurchased()) {
            cost = cost.multiply(BigInteger.valueOf(95)).divide(BigInteger.valueOf(100));
        }
        if (stardustCount.compareTo(cost) >= 0) {
            stardustCount = stardustCount.subtract(cost);
            research.setPurchased(true);
            playMilestoneSound();
            recalculateSps();
            refreshResearchUI();
            updateUI();
            Toast.makeText(this, "Research Complete: " + research.getName(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Not enough stardust!", Toast.LENGTH_SHORT).show();
        }
    }

    private void recalculateSps() {
        BigDecimal totalSpsDecimal = BigDecimal.ZERO;
        boolean harmonicResonance = getResearch("research_milestone").isPurchased();

        // Get the skill level BEFORE the loop
        int celestialArchitectureLevel = getSkillLevel("arch_1");

        for (int i = 0; i < upgrades.size(); i++) {
            Upgrade upgrade = upgrades.get(i);
            // Pass the skill level as the second argument
            BigDecimal upgradeCps = upgrade.getTotalCpsForThisUpgrade(harmonicResonance, celestialArchitectureLevel);
            
            // NEW: Apply mastery bonus
            if (i < masteries.size()) {
                double masteryBonus = masteries.get(i).getBonusMultiplier();
                upgradeCps = upgradeCps.multiply(BigDecimal.valueOf(masteryBonus));
            }
            
            totalSpsDecimal = totalSpsDecimal.add(upgradeCps);
        }

        if (getResearch("research_sps_boost").isPurchased()) {
            totalSpsDecimal = totalSpsDecimal.multiply(new BigDecimal("1.1"));
        }

        int spsSkillLevel = getSkillLevel("sps_1");
        BigDecimal spsSkillBonus = BigDecimal.valueOf(spsSkillLevel * 0.02);
        totalSpsDecimal = totalSpsDecimal.multiply(BigDecimal.ONE.add(spsSkillBonus));

        if (cosmicEssence > 0) {
            BigDecimal essenceMultiplier = BigDecimal.valueOf(Math.pow(2, cosmicEssence));
            totalSpsDecimal = totalSpsDecimal.multiply(essenceMultiplier);
        }

        // Apply singularity bonus using the new helper method
        BigDecimal singularityMultiplier = BigDecimal.ONE.add(BigDecimal.valueOf(singularityCount * getSingularityBonus()));
        totalSpsDecimal = totalSpsDecimal.multiply(singularityMultiplier);
        totalSps = totalSpsDecimal.toBigInteger().longValue();
    }

    private void triggerSupernova() {
        if (stardustCount.compareTo(supernovaCost) >= 0) {
            double earned = Math.max(1, Math.log10(stardustCount.doubleValue()) - 11);
            int singSkillLevel = getSkillLevel("sing_1");
            double singSkillBonus = singSkillLevel * 0.02;
            earned = earned * (1 + singSkillBonus);
            int earnedSingularities = (int) earned;

            singularityCount += earnedSingularities;
            totalSupernovas++; // NEW: Track supernova count
            Toast.makeText(this, "SUPERNOVA! You earned " + earnedSingularities + " Singularities!", Toast.LENGTH_LONG).show();

            // Reset everything EXCEPT singularities, essence and skills
            stardustCount = BigInteger.ZERO;
            totalClicks = 0;
            for (Upgrade upgrade : upgrades) upgrade.count = 0;
            for (ResearchUpgrade research : researchUpgrades) research.setPurchased(false);

            saveGame(); // Save new singularity count
            loadGame(); // Reload to apply bonuses and update UI
        } else {
            Toast.makeText(this, "You need at least " + formatBigNumber(supernovaCost) + " stardust to go Supernova.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- NEW: Big Crunch Methods ---
    private void triggerBigCrunch() {
        if (singularityCount >= BIG_CRUNCH_COST) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Initiate Big Crunch?")
                    .setMessage("This will reset your Stardust, Upgrades, Research, Skills, and ALL Singularities in exchange for 1 Cosmic Essence. Cosmic Essence provides a massive permanent boost. Are you sure?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("DO IT", (dialog, which) -> {
                        cosmicEssence++;
                        totalBigCrunches++; // NEW: Track big crunch count
                        singularityCount = 0; // Reset singularities

                        Toast.makeText(this, "BIG CRUNCH! You have earned 1 Cosmic Essence!", Toast.LENGTH_LONG).show();
                        playMilestoneSound();

                        // Perform a full reset, even harder than a supernova
                        fullReset();
                    })
                    .show();
        }
    }

    private void fullReset() {
        stardustCount = BigInteger.ZERO;
        totalClicks = 0;
        totalStardustEverEarned = BigInteger.ZERO;

        for (Upgrade upgrade : upgrades) upgrade.count = 0;
        for (ResearchUpgrade research : researchUpgrades) research.setPurchased(false);
        for (Skill skill : skills) skill.setCurrentLevel(0);
        // Note: We DO NOT reset achievements or cosmicEssence

        saveGame(); // Save the new essence/singularity count immediately
        loadGame(); // Reload to apply all new states and update UI
    }

    // --- Save and Load ---
    private void saveGame() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_STARDUST, stardustCount.toString());
        editor.putInt(KEY_SINGULARITY, singularityCount);
        editor.putInt(KEY_COSMIC_ESSENCE, cosmicEssence); // NEW
        editor.putInt(KEY_AUTO_CLICKER_LEVEL, autoClickerLevel); // NEW
        for (int i = 0; i < upgrades.size(); i++) editor.putInt(KEY_UPGRADE_COUNT_PREFIX + i, upgrades.get(i).count);
        for (ResearchUpgrade research : researchUpgrades) editor.putBoolean(KEY_RESEARCH_PREFIX + research.getId(), research.isPurchased());
        for (Achievement achievement : achievements) editor.putBoolean(KEY_ACHIEVEMENT_PREFIX + achievement.getId(), achievement.isUnlocked());
        for (Skill skill : skills) {
            editor.putInt(KEY_SKILL_LEVEL_PREFIX + skill.getId(), skill.getCurrentLevel());
        }
        // NEW: Save mastery data
        for (int i = 0; i < masteries.size(); i++) {
            Mastery mastery = masteries.get(i);
            editor.putInt(KEY_MASTERY_LEVEL_PREFIX + i, mastery.getLevel());
            editor.putInt(KEY_MASTERY_XP_PREFIX + i, mastery.getExperience());
        }
        editor.putLong(KEY_TOTAL_CLICKS, totalClicks);
        editor.putString(KEY_TOTAL_STARDUST_EARNED, totalStardustEverEarned.toString());
        editor.putLong(KEY_LAST_ONLINE_TIME, System.currentTimeMillis());
        editor.putInt(KEY_TOTAL_SUPERNOVAS, totalSupernovas);
        editor.putInt(KEY_TOTAL_BIG_CRUNCHES, totalBigCrunches);
        editor.putInt(KEY_TOTAL_RIFTS_TAPPED, totalRiftsTapped);
        editor.putLong(KEY_GAME_START_TIME, gameStartTime);
        editor.apply();
    }

    private void loadGame() {
        stardustCount = new BigInteger(prefs.getString(KEY_STARDUST, "0"));
        singularityCount = prefs.getInt(KEY_SINGULARITY, 0);
        cosmicEssence = prefs.getInt(KEY_COSMIC_ESSENCE, 0); // NEW
        autoClickerLevel = prefs.getInt(KEY_AUTO_CLICKER_LEVEL, 0); // NEW
        for (int i = 0; i < upgrades.size(); i++) upgrades.get(i).count = prefs.getInt(KEY_UPGRADE_COUNT_PREFIX + i, 0);
        for (ResearchUpgrade research : researchUpgrades) research.setPurchased(prefs.getBoolean(KEY_RESEARCH_PREFIX + research.getId(), false));
        for (Achievement achievement : achievements) achievement.setUnlocked(prefs.getBoolean(KEY_ACHIEVEMENT_PREFIX + achievement.getId(), false));
        for (Skill skill : skills) {
            skill.setCurrentLevel(prefs.getInt(KEY_SKILL_LEVEL_PREFIX + skill.getId(), 0));
        }
        // NEW: Load mastery data
        for (int i = 0; i < masteries.size(); i++) {
            Mastery mastery = masteries.get(i);
            mastery.setLevel(prefs.getInt(KEY_MASTERY_LEVEL_PREFIX + i, 0));
            mastery.setExperience(prefs.getInt(KEY_MASTERY_XP_PREFIX + i, 0));
        }
        totalClicks = prefs.getLong(KEY_TOTAL_CLICKS, 0);
        totalStardustEverEarned = new BigInteger(prefs.getString(KEY_TOTAL_STARDUST_EARNED, "0"));
        totalSupernovas = prefs.getInt(KEY_TOTAL_SUPERNOVAS, 0);
        totalBigCrunches = prefs.getInt(KEY_TOTAL_BIG_CRUNCHES, 0);
        totalRiftsTapped = prefs.getInt(KEY_TOTAL_RIFTS_TAPPED, 0);
        gameStartTime = prefs.getLong(KEY_GAME_START_TIME, 0);
        
        // NEW: Initialize game start time if this is first load
        if (gameStartTime == 0) {
            gameStartTime = System.currentTimeMillis();
            prefs.edit().putLong(KEY_GAME_START_TIME, gameStartTime).apply();
        }
        
        recalculateSps();
        updateUI();
        if (tabLayout.getSelectedTabPosition() == 0) refreshCelestialObjectsUI(); else refreshResearchUI();
        
        // NEW: Start auto-clicker if it was active
        if (autoClickerLevel > 0) {
            startAutoClicker();
        }
    }

    // --- NEW: Method to handle buy amount selection ---
    private void setBuyAmount(int amount) {
        currentBuyAmount = amount;

        // Update the visual state of the buttons
        btnBuy1.setAlpha(amount == 1 ? 1.0f : 0.5f);
        btnBuy10.setAlpha(amount == 10 ? 1.0f : 0.5f);
        btnBuy25.setAlpha(amount == 25 ? 1.0f : 0.5f);

        // Refresh the UI to show the new costs for the selected amount
        if (tabLayout.getSelectedTabPosition() == 0) {
            refreshCelestialObjectsUI();
        }
        // Note: Research tab is not affected by buy amount, so no need to refresh it.
    }


    // --- UI Update Methods ---
    // REPLACED: Cached version
    private void refreshCelestialObjectsUI() {
        llUpgradesContainer.removeAllViews(); // Clear the container first

        // Create any views that don't exist in the cache yet
        for (Upgrade upgrade : upgrades) {
            if (!upgradeViewCache.containsKey(upgrade)) {
                View v = LayoutInflater.from(this).inflate(R.layout.upgrade_item, llUpgradesContainer, false);
                upgradeViewCache.put(upgrade, v); // Add the new view to the cache
            }
        }

        // Now, update and add all views from the cache
        for (Upgrade upgrade : upgrades) {
            View v = upgradeViewCache.get(upgrade);
            if (v == null) continue; // Safety check

            // If the view's parent is not null, it means it's already in the layout from a previous refresh.
            // This can happen if the tab wasn't switched. We remove it to prevent a crash before re-adding it.
            if (v.getParent() != null) {
                ((ViewGroup) v.getParent()).removeView(v);
            }

            ImageView icon = v.findViewById(R.id.imgUpgradeIcon);
            TextView name = v.findViewById(R.id.tvUpgradeName);
            Button buy = v.findViewById(R.id.btnBuyUpgrade);
            ProgressBar pBar = v.findViewById(R.id.progressUpgrade);

            name.setText(String.format(Locale.getDefault(), "%s (%d)", upgrade.getCurrentName(), upgrade.count));
            icon.setImageResource(upgrade.getCurrentIcon());

            // --- CHANGE: Calculate the total cost for the selected buy amount ---
            BigInteger totalCost = calculateCostForNUpgrades(upgrade, currentBuyAmount, getResearch("research_cheaper").isPurchased());
            buy.setText(String.format(Locale.getDefault(), "Buy %d (%s)", currentBuyAmount, formatBigNumber(totalCost)));

            // We must set the listener every time, as the reference can be lost
            buy.setOnClickListener(view -> buyUpgrade(upgrade));

            pBar.setMax(Upgrade.MILESTONE_INTERVAL);
            pBar.setProgress(upgrade.count % Upgrade.MILESTONE_INTERVAL);

            // Add the (potentially updated) view to the layout
            llUpgradesContainer.addView(v);
        }
    }

    // REPLACED: Cached version
    private void refreshResearchUI() {
        llUpgradesContainer.removeAllViews();

        for (ResearchUpgrade research : researchUpgrades) {
            if (!researchViewCache.containsKey(research)) {
                View v = LayoutInflater.from(this).inflate(R.layout.research_item, llUpgradesContainer, false);
                researchViewCache.put(research, v);
            }
        }

        for (ResearchUpgrade research : researchUpgrades) {
            View v = researchViewCache.get(research);
            if (v == null) continue;

            if (v.getParent() != null) {
                ((ViewGroup) v.getParent()).removeView(v);
            }

            TextView name = v.findViewById(R.id.tvResearchName);
            TextView desc = v.findViewById(R.id.tvResearchDesc);
            Button buy = v.findViewById(R.id.btnBuyResearch);

            name.setText(research.getName());
            desc.setText(research.getDescription());

            if (research.isPurchased()) {
                buy.setText("PURCHASED");
                buy.setEnabled(false);
                v.setAlpha(0.6f);
            } else {
                BigInteger cost = research.getCost();
                if (getResearch("research_cheaper").isPurchased()) {
                    cost = cost.multiply(BigInteger.valueOf(95)).divide(BigInteger.valueOf(100));
                }
                buy.setText(String.format("Buy (%s)", formatBigNumber(cost)));
                buy.setEnabled(true);
                v.setAlpha(1.0f);
                buy.setOnClickListener(view -> buyResearch(research));
            }
            llUpgradesContainer.addView(v);
        }
    }

    // REPLACED: Debounced version
    private void updateUI() {
        // --- Stardust Count ---
        String newStardustText = formatBigNumber(stardustCount);
        if (!newStardustText.equals(lastDisplayedStardust)) {
            tvStardustCount.setText(String.format("%s Stardust", newStardustText));
            lastDisplayedStardust = newStardustText;
        }

        // --- SPS Count ---
        String newSpsText = formatBigNumber(BigInteger.valueOf(totalSps));
        if (!newSpsText.equals(lastDisplayedSps)) {
            tvSps.setText(String.format(Locale.getDefault(), "per second: %s", newSpsText));
            lastDisplayedSps = newSpsText;
        }

        // --- These don't need debouncing as they change less often ---
        String essenceText = (cosmicEssence > 0) ? String.format(Locale.getDefault(), " | %d Essence", cosmicEssence) : "";
        double currentSingularityBonus = getSingularityBonus();
        int bonusPercent = (int) (singularityCount * currentSingularityBonus * 100);
        tvSingularityCount.setText(String.format(Locale.getDefault(), "%d Singularities (+%d%% SPS)%s", singularityCount, bonusPercent, essenceText));

        btnSupernova.setVisibility(stardustCount.compareTo(supernovaCost) >= 0 ? View.VISIBLE : View.GONE);
        btnBigCrunch.setVisibility(singularityCount >= BIG_CRUNCH_COST ? View.VISIBLE : View.GONE);
        
        // --- NEW: Auto-Clicker Button Update ---
        if (autoClickerLevel == 0) {
            btnAutoClicker.setText(String.format("AUTO (%s)", formatBigNumber(getAutoClickerCost())));
            btnAutoClicker.setEnabled(stardustCount.compareTo(getAutoClickerCost()) >= 0);
        } else {
            btnAutoClicker.setText(String.format("LV%d (%s)", autoClickerLevel, formatBigNumber(getAutoClickerCost())));
            btnAutoClicker.setEnabled(stardustCount.compareTo(getAutoClickerCost()) >= 0);
        }
    }

    // --- Number Formatting ---
    private String formatBigNumber(BigInteger number) {
        if (number.compareTo(BigInteger.valueOf(1000)) < 0) return number.toString();
        int mag = (number.toString().length() - 1) / 3;
        if (mag >= numberSuffixes.length) mag = numberSuffixes.length - 1;
        BigDecimal shortValue = new BigDecimal(number).divide(
                new BigDecimal(BigInteger.TEN.pow(mag * 3)), 2, RoundingMode.HALF_UP);
        return shortValue + numberSuffixes[mag];
    }
    private static final String[] numberSuffixes = {
            "", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc",
            "UDc", "DDc", "TDc", "QaDc", "QiDc", "SxDc", "SpDc", "OcDc", "NoDc", "Vg"
    };

    // --- Helper Methods ---
    private BigInteger calculateCostForNUpgrades(Upgrade upgrade, int amount, boolean isCheaperResearch) {
        BigInteger totalCost = BigInteger.ZERO;

        // This is a "dry run" of the buyMultiple logic.
        // It's not ideal to temporarily change object state, but it's the only way
        // without access to the Upgrade class's internal cost formula.
        int originalCount = upgrade.count;
        for (int i = 0; i < amount; i++) {
            BigInteger costForThisLevel = getAdjustedUpgradeCost(upgrade.getNextCost(isCheaperResearch));
            totalCost = totalCost.add(costForThisLevel);
            upgrade.count++; // Increment count to get the cost for the *next* level in the next iteration
        }
        upgrade.count = originalCount; // IMPORTANT: Reset the count back to its original value

        return totalCost;
    }

    // NEW: Helper for Singularity Bonus calculation
    private double getSingularityBonus() {
        double currentBonus = singularityBonus;
        int attunementLevel = getSkillLevel("sing_2");
        if (attunementLevel > 0) {
            currentBonus += (attunementLevel * 0.01);
        }
        return currentBonus;
    }

    private ResearchUpgrade getResearch(String id) {
        for (ResearchUpgrade research : researchUpgrades) {
            if (research.getId().equals(id)) return research;
        }
        return new ResearchUpgrade("", "", "", BigInteger.ZERO);
    }
    private void showStardustPopup(BigInteger amount, View anchor) {
        TextView popup = new TextView(this);
        popup.setText(String.format(Locale.getDefault(), "+%s", formatBigNumber(amount)));
        popup.setTextColor(getResources().getColor(R.color.white, getTheme()));
        popup.setTextSize(18f);
        popup.setShadowLayer(5, 0, 0, getResources().getColor(R.color.black, getTheme()));
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        popup.setLayoutParams(params);
        rootLayout.addView(popup);
        popup.setX(anchor.getX() + anchor.getWidth() / 2f - popup.getWidth() / 2f);
        popup.setY(anchor.getY() + anchor.getHeight() / 2f - popup.getHeight() / 2f);
        popup.animate()
                .translationYBy(-200)
                .alpha(0f)
                .setDuration(1500)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (popup.getParent() != null) {
                            rootLayout.removeView(popup);
                        }
                    }
                })
                .start();
    }

    private void calculateOfflineProgress() {
        long lastOnlineTime = prefs.getLong(KEY_LAST_ONLINE_TIME, 0);
        if (lastOnlineTime == 0) return;

        long currentTime = System.currentTimeMillis();
        long offlineMillis = currentTime - lastOnlineTime;
        long offlineSeconds = offlineMillis / 1000;

        // Only show the dialog if offline for a meaningful amount of time (e.g., > 1 minute)
        if (offlineSeconds > 60) {
            long offlineSps = totalSps / boostMultiplier;
            BigInteger stardustEarned = BigInteger.valueOf(offlineSps).multiply(BigInteger.valueOf(offlineSeconds));

            stardustCount = stardustCount.add(stardustEarned);
            totalStardustEverEarned = totalStardustEverEarned.add(stardustEarned);

            // Show the new dialog instead of a Toast
            showOfflineProgressDialog(offlineSeconds, offlineSps, stardustEarned);

            // Still need to update the UI and check achievements after the dialog is closed
            updateUI();
            checkAchievements();
        }
    }

    private void showOfflineProgressDialog(long offlineSeconds, long offlineSps, BigInteger stardustEarned) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_offline_progress, null);

        TextView tvOfflineTime = dialogView.findViewById(R.id.tvOfflineTime);
        TextView tvOfflineSps = dialogView.findViewById(R.id.tvOfflineSps);
        TextView tvOfflineStardust = dialogView.findViewById(R.id.tvOfflineStardust);
        Button btnClaim = dialogView.findViewById(R.id.btnClaimOffline);

        // Format the time away nicely
        long hours = offlineSeconds / 3600;
        long minutes = (offlineSeconds % 3600) / 60;
        String timeAwayText = String.format(Locale.getDefault(), "You were away for: %d hours, %d min", hours, minutes);

        tvOfflineTime.setText(timeAwayText);
        tvOfflineSps.setText(String.format("Your empire generated: %s/s", formatBigNumber(BigInteger.valueOf(offlineSps))));
        tvOfflineStardust.setText(formatBigNumber(stardustEarned));

        builder.setView(dialogView);
        builder.setCancelable(false); // Player must click the button

        AlertDialog dialog = builder.create();
        btnClaim.setOnClickListener(v -> {
            // The currency has already been added, so we just dismiss the dialog
            dialog.dismiss();
        });

        dialog.show();
    }

    private void checkDailyReward() {
        long lastClaim = prefs.getLong(KEY_LAST_DAILY_REWARD, 0);
        long now = System.currentTimeMillis();
        if (now - lastClaim > 24 * 60 * 60 * 1000) {
            showDailyRewardDialog();
        }
    }
    private void showDailyRewardDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_daily_reward, null);
        Button btnClaim = dialogView.findViewById(R.id.btnClaimDailyReward);
        builder.setView(dialogView).setCancelable(false);
        AlertDialog dialog = builder.create();
        btnClaim.setOnClickListener(v -> {
            BigInteger reward = BigInteger.valueOf(DAILY_REWARD_AMOUNT);
            stardustCount = stardustCount.add(reward);
            totalStardustEverEarned = totalStardustEverEarned.add(reward);
            prefs.edit().putLong(KEY_LAST_DAILY_REWARD, System.currentTimeMillis()).apply();
            updateUI();
            checkAchievements();
            Toast.makeText(this, "+" + formatBigNumber(reward) + " Stardust!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
    }
    private void startGameLoop() {
        Runnable gameRunnable = new Runnable() {
            @Override
            public void run() {
                BigInteger spsToAdd = BigInteger.valueOf(totalSps);
                stardustCount = stardustCount.add(spsToAdd);
                totalStardustEverEarned = totalStardustEverEarned.add(spsToAdd);
                updateUI();
                checkAchievements();
                gameLoopHandler.postDelayed(this, 1000);
            }
        };
        gameLoopHandler.post(gameRunnable);
    }
    private void startRiftSpawner() {
        riftSpawnerRunnable = new Runnable() {
            @Override
            public void run() {
                spawnRift();
                riftHandler.postDelayed(this, 20000 + random.nextInt(25000));
            }
        };
        riftHandler.postDelayed(riftSpawnerRunnable, 15000);
    }
    private void spawnRift() {
        if (rootLayout.getWidth() == 0) return;
        final ImageView rift = new ImageView(this);
        rift.setImageResource(R.drawable.ic_cosmic_rift);
        int riftSize = 250;
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(riftSize, riftSize);
        rift.setLayoutParams(params);
        int margin = 150;
        rift.setX(margin + random.nextInt(rootLayout.getWidth() - (2 * margin)));
        rift.setY(margin + random.nextInt(rootLayout.getHeight() - (2 * margin)));
        rift.setAlpha(0f);
        rift.setScaleX(0.5f);
        rift.setScaleY(0.5f);
        rootLayout.addView(rift);
        AnimatorSet fadeInSet = new AnimatorSet();
        fadeInSet.playTogether(
                ObjectAnimator.ofFloat(rift, "alpha", 1f),
                ObjectAnimator.ofFloat(rift, "scaleX", 1.2f, 1f),
                ObjectAnimator.ofFloat(rift, "scaleY", 1.2f, 1f)
        );
        fadeInSet.setDuration(500);
        final Handler timeoutHandler = new Handler();
        final Runnable fadeOutRunnable = () -> {
            if (rift.getParent() != null) {
                ObjectAnimator.ofFloat(rift, "alpha", 0f).setDuration(500).start();
            }
        };
        rift.setOnClickListener(v -> {
            timeoutHandler.removeCallbacks(fadeOutRunnable);
            rift.setOnClickListener(null);
            BigInteger reward = BigInteger.valueOf(250).add(BigInteger.valueOf(totalSps * 15));
            stardustCount = stardustCount.add(reward);
            totalStardustEverEarned = totalStardustEverEarned.add(reward);
            totalRiftsTapped++; // NEW: Track rifts tapped
            Toast.makeText(this, "Rift Tapped! +" + formatBigNumber(reward) + " Stardust!", Toast.LENGTH_SHORT).show();
            playRiftSound();
            explodeParticles(rift.getX() + riftSize / 2f, rift.getY() + riftSize / 2f);
            rootLayout.removeView(rift);
            updateUI();
            checkAchievements();
        });
        fadeInSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                timeoutHandler.postDelayed(fadeOutRunnable, 4000);
            }
        });
        fadeInSet.start();
    }
    private void explodeParticles(float x, float y) {
        for (int i = 0; i < 15; i++) {
            final ImageView particle = new ImageView(this);
            particle.setImageResource(R.drawable.ic_stardust_particle);
            int size = 20 + random.nextInt(30);
            particle.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            particle.setX(x - size/2f);
            particle.setY(y - size/2f);
            rootLayout.addView(particle);
            float endX = (float) (x + (random.nextFloat() - 0.5) * 400);
            float endY = (float) (y + (random.nextFloat() - 0.5) * 400);
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(particle,
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_X, endX),
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, endY),
                    PropertyValuesHolder.ofFloat(View.ALPHA, 0f)
            );
            animator.setDuration(800 + random.nextInt(400));
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) { rootLayout.removeView(particle); }
            });
            animator.start();
        }
    }
    private void checkAchievements() {
        for (Achievement achievement : achievements) {
            if (!achievement.isUnlocked()) {
                boolean newlyUnlocked = false;
                switch (achievement.getType()) {
                    case TOTAL_CLICKS: if (totalClicks >= achievement.getGoal()) newlyUnlocked = true; break;
                    case TOTAL_STARDUST: if (totalStardustEverEarned.compareTo(BigInteger.valueOf(achievement.getGoal())) >= 0) newlyUnlocked = true; break;
                    case UPGRADE_COUNT: int index = achievement.getTargetUpgradeIndex(); if (index >= 0 && index < upgrades.size() && upgrades.get(index).count >= achievement.getGoal()) newlyUnlocked = true; break;
                    case REACH_SPS: if (totalSps >= achievement.getGoal()) newlyUnlocked = true; break;
                    case SINGULARITY_COUNT: if (singularityCount >= achievement.getGoal()) newlyUnlocked = true; break;
                    case RESEARCH_COUNT: 
                        int researchCount = 0;
                        for (ResearchUpgrade research : researchUpgrades) {
                            if (research.isPurchased()) researchCount++;
                        }
                        if (researchCount >= achievement.getGoal()) newlyUnlocked = true; 
                        break;
                    case ESSENCE_COUNT: if (cosmicEssence >= achievement.getGoal()) newlyUnlocked = true; break;
                    
                    // --- NEW: Auto-Clicker and Mastery Achievement Types ---
                    case AUTO_CLICKER_LEVEL: if (autoClickerLevel >= achievement.getGoal()) newlyUnlocked = true; break;
                    case MASTERY_COUNT:
                        int maxMasteryLevel = 0;
                        for (Mastery mastery : masteries) {
                            maxMasteryLevel = Math.max(maxMasteryLevel, mastery.getLevel());
                        }
                        if (maxMasteryLevel >= achievement.getGoal()) newlyUnlocked = true;
                        break;
                    case RIFTS_TAPPED: if (totalRiftsTapped >= achievement.getGoal()) newlyUnlocked = true; break;
                }
                if (newlyUnlocked) unlockAchievement(achievement);
            }
        }
    }
    private void unlockAchievement(Achievement achievement) {
        achievement.setUnlocked(true);
        playAchievementSound();
        Toast.makeText(this, "Achievement Unlocked: " + achievement.getName(), Toast.LENGTH_LONG).show();
    }
    private void showAchievementsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_achievements, null);
        LinearLayout container = dialogView.findViewById(R.id.llAchievementsContainer);
        container.removeAllViews();
        for(Achievement achievement : achievements) {
            View itemView = getLayoutInflater().inflate(R.layout.achievement_item, container, false);
            ImageView icon = itemView.findViewById(R.id.imgAchievementIcon);
            TextView name = itemView.findViewById(R.id.tvAchievementName);
            TextView desc = itemView.findViewById(R.id.tvAchievementDesc);
            name.setText(achievement.getName());
            desc.setText(achievement.getDescription());
            if (achievement.isUnlocked()) {
                icon.setImageResource(R.drawable.ic_achievement_unlocked);
                name.setAlpha(1.0f); desc.setAlpha(1.0f);
            } else {
                icon.setImageResource(R.drawable.ic_achievement_locked);
                name.setAlpha(0.5f); desc.setAlpha(0.5f);
            }
            container.addView(itemView);
        }
        builder.setView(dialogView).setPositiveButton("Close", (d, w) -> d.dismiss()).show();
    }

    // --- Skill Tree Methods ---
    private void showSkillTreeDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_skill_tree, null);
        LinearLayout container = dialogView.findViewById(R.id.llSkillContainer);
        TextView tvPoints = dialogView.findViewById(R.id.tvSingularityPoints);
        AlertDialog dialog = builder.setView(dialogView).setPositiveButton("Close", (d, w) -> d.dismiss()).create();
        populateSkillTreeUI(container, tvPoints, dialog);
        dialog.show();
    }
    private void populateSkillTreeUI(LinearLayout container, TextView tvPoints, AlertDialog dialog) {
        container.removeAllViews();
        tvPoints.setText(String.format(Locale.getDefault(), "%d Unspent Singularities", singularityCount));
        Map<String, Skill> skillMap = getSkillMap();

        for (Skill skill : skills) {
            View itemView = getLayoutInflater().inflate(R.layout.skill_item, container, false);
            TextView name = itemView.findViewById(R.id.tvSkillName);
            TextView desc = itemView.findViewById(R.id.tvSkillDesc);
            Button levelUpBtn = itemView.findViewById(R.id.btnLevelUpSkill);

            name.setText(String.format(Locale.getDefault(), "%s (%d/%d)", skill.getName(), skill.getCurrentLevel(), skill.getMaxLevel()));
            desc.setText(skill.getDescription());

            boolean requirementMet = skill.getRequiredSkillId() == null || (skillMap.get(skill.getRequiredSkillId()) != null && skillMap.get(skill.getRequiredSkillId()).getCurrentLevel() > 0);
            boolean canAfford = singularityCount >= skill.getNextCost();

            if (!requirementMet) {
                itemView.setAlpha(0.4f);
                levelUpBtn.setEnabled(false);
                levelUpBtn.setText("LOCKED");
            } else if (!skill.canLevelUp()) {
                itemView.setAlpha(0.7f);
                levelUpBtn.setEnabled(false);
                levelUpBtn.setText("MAX");
            } else {
                levelUpBtn.setText(String.format(Locale.getDefault(), "UP (%d)", skill.getNextCost()));
                levelUpBtn.setEnabled(canAfford);
                levelUpBtn.setOnClickListener(v -> {
                    levelUpSkill(skill);
                    populateSkillTreeUI(container, tvPoints, dialog); // Refresh the UI in place
                });
            }
            container.addView(itemView);
        }
    }
    private void levelUpSkill(Skill skill) {
        if (singularityCount >= skill.getNextCost() && skill.canLevelUp()) {
            singularityCount -= skill.getNextCost();
            skill.setCurrentLevel(skill.getCurrentLevel() + 1);
            recalculateSps();
            updateUI();
        }
    }
    private int getSkillLevel(String skillId) {
        for (Skill skill : skills) {
            if (skill.getId().equals(skillId)) {
                return skill.getCurrentLevel();
            }
        }
        return 0;
    }
    private Map<String, Skill> getSkillMap() {
        Map<String, Skill> map = new HashMap<>();
        for (Skill skill : skills) {
            map.put(skill.getId(), skill);
        }
        return map;
    }
    private BigInteger getAdjustedUpgradeCost(BigInteger originalCost) {
        int costReductionLevel = getSkillLevel("cost_1");
        if (costReductionLevel > 0) {
            BigDecimal reductionPercent = BigDecimal.valueOf(costReductionLevel * 0.01);
            BigDecimal multiplier = BigDecimal.ONE.subtract(reductionPercent);
            return new BigDecimal(originalCost).multiply(multiplier).toBigInteger();
        }
        return originalCost;
    }

    // --- Sound and Animation ---
    private void setupSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        soundPool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();
    }
    private void playClickSound() { if (soundEnabled && clickSoundId != 0) soundPool.play(clickSoundId, 1, 1, 0, 0, 1); }
    private void playUpgradeSound() { if (soundEnabled && upgradeSoundId != 0) soundPool.play(upgradeSoundId, 1, 1, 0, 0, 1); }
    private void playMilestoneSound() { if (soundEnabled && milestoneSoundId != 0) soundPool.play(milestoneSoundId, 1, 1, 1, 0, 1); }
    private void playAchievementSound() { if (soundEnabled && achievementSoundId != 0) soundPool.play(achievementSoundId, 0.8f, 0.8f, 2, 0, 1); }
    private void playRiftSound() { if (soundEnabled && riftSoundId != 0) soundPool.play(riftSoundId, 0.7f, 0.7f, 3, 0, 1); }
    private void animateClick(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1f)
        );
        set.setDuration(150);
        set.start();
    }
    private void createTwinklingStars(int count) {
        if (rootLayout.getWidth() == 0) return;
        for (int i = 0; i < rootLayout.getChildCount(); i++) {
            if ("twinkling_star".equals(rootLayout.getChildAt(i).getTag())) {
                rootLayout.removeViewAt(i);
                i--;
            }
        }
        for (int i = 0; i < count; i++) {
            final View star = new View(this);
            star.setTag("twinkling_star");
            star.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
            int size = random.nextInt(3) + 2;
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(size, size);
            star.setLayoutParams(params);
            rootLayout.addView(star, 0);
            twinkle(star);
        }
    }
    private void twinkle(final View star) {
        star.setX(random.nextInt(rootLayout.getWidth()));
        star.setY(random.nextInt(rootLayout.getHeight()));
        star.setAlpha(0f);
        long delay = random.nextInt(5000);
        long duration = random.nextInt(3000) + 1000;
        twinkleHandler.postDelayed(() -> {
            star.animate().alpha(1f).setDuration(duration).setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        star.animate().alpha(0f).setDuration(duration).setInterpolator(new DecelerateInterpolator())
                                .withEndAction(() -> twinkle(star));
                    });
        }, delay);
    }
    private void activateShootingStar() {
        if (isBoostActive) return;
        isBoostActive = true;
        boostMultiplier = 5;
        recalculateSps();
        updateUI();
        btnShootingStar.setEnabled(false);
        new CountDownTimer(10 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                btnShootingStar.setText(String.format(Locale.getDefault(), "BOOST! %ds", millisUntilFinished / 1000));
            }
            public void onFinish() {
                boostMultiplier = 1;
                recalculateSps();
                updateUI();
                startCooldown();
            }
        }.start();
    }
    private void startCooldown() {
        new CountDownTimer(60 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                btnShootingStar.setText(String.format(Locale.getDefault(), "CD %ds", millisUntilFinished / 1000));
            }
            public void onFinish() {
                isBoostActive = false;
                btnShootingStar.setEnabled(true);
                btnShootingStar.setText("SHOOTING STAR");
            }
        }.start();
    }
    private void showSettingsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        
        SwitchMaterial soundSwitch = dialogView.findViewById(R.id.switchSound);
        Button btnStatistics = dialogView.findViewById(R.id.btnStatistics);
        
        soundSwitch.setChecked(soundEnabled);
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> soundEnabled = isChecked);
        
        btnStatistics.setOnClickListener(v -> showStatisticsDialog());
        
        builder.setView(dialogView)
                .setTitle("Settings")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // --- NEW: Statistics Dialog ---
    private void showStatisticsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_statistics, null);
        
        // General Stats
        TextView tvTotalClicks = dialogView.findViewById(R.id.tvTotalClicks);
        TextView tvTotalStardust = dialogView.findViewById(R.id.tvTotalStardust);
        TextView tvCurrentSps = dialogView.findViewById(R.id.tvCurrentSps);
        TextView tvPlayTime = dialogView.findViewById(R.id.tvPlayTime);
        
        // Prestige Stats
        TextView tvSingularities = dialogView.findViewById(R.id.tvSingularities);
        TextView tvCosmicEssence = dialogView.findViewById(R.id.tvCosmicEssence);
        TextView tvSupernovas = dialogView.findViewById(R.id.tvSupernovas);
        TextView tvBigCrunches = dialogView.findViewById(R.id.tvBigCrunches);
        
        // Automation Stats
        TextView tvAutoClickerLevel = dialogView.findViewById(R.id.tvAutoClickerLevel);
        TextView tvTotalUpgrades = dialogView.findViewById(R.id.tvTotalUpgrades);
        TextView tvResearchCompleted = dialogView.findViewById(R.id.tvResearchCompleted);
        TextView tvSkillPoints = dialogView.findViewById(R.id.tvSkillPoints);
        
        // Achievement Stats
        TextView tvAchievementsUnlocked = dialogView.findViewById(R.id.tvAchievementsUnlocked);
        ProgressBar progressAchievements = dialogView.findViewById(R.id.progressAchievements);
        TextView tvRiftsTapped = dialogView.findViewById(R.id.tvRiftsTapped);
        
        // Calculate values
        long playTimeHours = gameStartTime > 0 ? (System.currentTimeMillis() - gameStartTime) / (1000 * 60 * 60) : 0;
        int totalUpgradeCount = 0;
        for (Upgrade upgrade : upgrades) {
            totalUpgradeCount += upgrade.count;
        }
        int researchCompleted = 0;
        for (ResearchUpgrade research : researchUpgrades) {
            if (research.isPurchased()) researchCompleted++;
        }
        int totalSkillPoints = 0;
        for (Skill skill : skills) {
            totalSkillPoints += skill.getCurrentLevel();
        }
        int achievementsUnlocked = 0;
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) achievementsUnlocked++;
        }
        int achievementPercent = (achievementsUnlocked * 100) / achievements.size();
        
        // Set values
        tvTotalClicks.setText(String.format("Total Clicks: %,d", totalClicks));
        tvTotalStardust.setText(String.format("Total Stardust Earned: %s", formatBigNumber(totalStardustEverEarned)));
        tvCurrentSps.setText(String.format("Current SPS: %s", formatBigNumber(BigInteger.valueOf(totalSps))));
        tvPlayTime.setText(String.format("Play Time: %d hours", playTimeHours));
        
        tvSingularities.setText(String.format("Singularities: %d", singularityCount));
        tvCosmicEssence.setText(String.format("Cosmic Essence: %d", cosmicEssence));
        tvSupernovas.setText(String.format("Total Supernovas: %d", totalSupernovas));
        tvBigCrunches.setText(String.format("Total Big Crunches: %d", totalBigCrunches));
        
        tvAutoClickerLevel.setText(String.format("Auto-Clicker Level: %d", autoClickerLevel));
        tvTotalUpgrades.setText(String.format("Total Upgrades Owned: %,d", totalUpgradeCount));
        tvResearchCompleted.setText(String.format("Research Completed: %d/%d", researchCompleted, researchUpgrades.size()));
        tvSkillPoints.setText(String.format("Total Skill Points Spent: %d", totalSkillPoints));
        
        tvAchievementsUnlocked.setText(String.format("Achievements Unlocked: %d/%d", achievementsUnlocked, achievements.size()));
        progressAchievements.setProgress(achievementPercent);
        tvRiftsTapped.setText(String.format("Cosmic Rifts Tapped: %d", totalRiftsTapped));
        
        builder.setView(dialogView)
                .setTitle("Game Statistics")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startAutoSave() {
        autoSaveRunnable = new Runnable() {
            @Override
            public void run() {
                saveGame();
                autoSaveHandler.postDelayed(this, 10000);
            }
        };
        autoSaveHandler.postDelayed(autoSaveRunnable, 10000);
    }

    // --- NEW: Auto-Clicker Methods ---
    private void startAutoClicker() {
        if (autoClickerLevel > 0 && !autoClickerActive) {
            autoClickerActive = true;
            autoClickRunnable = new Runnable() {
                @Override
                public void run() {
                    if (autoClickerActive && autoClickerLevel > 0) {
                        performAutoClick();
                        // Auto-click every 3 seconds, reduced by 0.2s per level (minimum 1s)
                        int delay = Math.max(1000, 3000 - (autoClickerLevel * 200));
                        autoClickHandler.postDelayed(this, delay);
                    }
                }
            };
            autoClickHandler.post(autoClickRunnable);
        }
    }

    private void stopAutoClicker() {
        autoClickerActive = false;
        if (autoClickRunnable != null) {
            autoClickHandler.removeCallbacks(autoClickRunnable);
        }
    }

    private void performAutoClick() {
        // Simulate a click but with visual feedback
        BigInteger clickValue = BigInteger.ONE;
        clickValue = clickValue.add(BigInteger.valueOf(getSkillLevel("click_1")));
        if (getResearch("research_click_sps").isPurchased()) {
            clickValue = clickValue.add(BigInteger.valueOf(totalSps / 100));
        }
        
        // Auto-clicker gets efficiency bonus
        BigDecimal efficiency = BigDecimal.valueOf(1.0 + (autoClickerLevel * 0.1));
        clickValue = new BigDecimal(clickValue).multiply(efficiency).toBigInteger();
        
        stardustCount = stardustCount.add(clickValue);
        totalStardustEverEarned = totalStardustEverEarned.add(clickValue);
        
        // Visual feedback for auto-click
        showStardustPopup(clickValue, btnClickStar);
        animateAutoClick();
        
        updateUI();
        checkAchievements();
    }

    private void animateAutoClick() {
        // Subtle animation for auto-clicks
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btnClickStar, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btnClickStar, "scaleY", 1f, 1.1f, 1f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(200);
        animatorSet.start();
    }

    private BigInteger getAutoClickerCost() {
        return BigInteger.valueOf(AUTO_CLICKER_BASE_COST).multiply(
            BigInteger.valueOf((long) Math.pow(2, autoClickerLevel))
        );
    }

    private void upgradeAutoClicker() {
        BigInteger cost = getAutoClickerCost();
        if (stardustCount.compareTo(cost) >= 0) {
            stardustCount = stardustCount.subtract(cost);
            autoClickerLevel++;
            
            if (autoClickerLevel == 1) {
                startAutoClicker();
                Toast.makeText(this, "Auto-Clicker Activated!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Auto-Clicker Level " + autoClickerLevel + "!", Toast.LENGTH_SHORT).show();
            }
            
            updateUI();
            playUpgradeSound();
        } else {
            Toast.makeText(this, "Not enough stardust for Auto-Clicker!", Toast.LENGTH_SHORT).show();
        }
    }

    // --- NEW: Enhanced Particle Effects ---
    private void createEnhancedClickParticles(float x, float y, BigInteger value) {
        int particleCount = Math.min(20, Math.max(5, value.toString().length() / 2));
        
        for (int i = 0; i < particleCount; i++) {
            final ImageView particle = new ImageView(this);
            
            // Choose particle type based on value magnitude
            if (value.compareTo(BigInteger.valueOf(1000000)) >= 0) {
                particle.setImageResource(R.drawable.ic_star_shower); // Gold particles for big values
            } else if (value.compareTo(BigInteger.valueOf(1000)) >= 0) {
                particle.setImageResource(R.drawable.ic_cosmic_rift); // Blue particles for medium values
            } else {
                particle.setImageResource(R.drawable.ic_stardust_particle); // Regular particles for small values
            }
            
            int size = 15 + random.nextInt(25);
            particle.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            particle.setX(x - size/2f + (random.nextFloat() - 0.5f) * 100);
            particle.setY(y - size/2f + (random.nextFloat() - 0.5f) * 100);
            particle.setAlpha(0.8f);
            rootLayout.addView(particle);
            
            // Enhanced animation with rotation and scaling
            float endX = x + (random.nextFloat() - 0.5f) * 600;
            float endY = y + (random.nextFloat() - 0.5f) * 600;
            float rotation = random.nextFloat() * 720f - 360f;
            
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(particle,
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_X, endX),
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, endY),
                    PropertyValuesHolder.ofFloat(View.ALPHA, 0f),
                    PropertyValuesHolder.ofFloat(View.ROTATION, rotation),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f)
            );
            animator.setDuration(1200 + random.nextInt(800));
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override 
                public void onAnimationEnd(Animator animation) { 
                    if (particle.getParent() != null) {
                        rootLayout.removeView(particle); 
                    }
                }
            });
            animator.start();
        }
    }
}