package com.zaigame.IdleClickerGame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
    private TextView tvStardustCount, tvSps, tvSingularityCount, tvEventStatus;
    private FloatingActionButton btnClickStar, btnSettings, btnAchievements, btnStatistics;
    private Button btnSupernova, btnShootingStar, btnSkillTree, btnBigCrunch, btnGalaxyCollapse; // MODIFIED: Added buttons
    private Button btnBuy1, btnBuy10, btnBuy25, btnBuy50, btnBuy100; // NEW: Buy amount buttons
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

    // --- NEW: Galaxy Collapse Variables ---
    private int galaxyPoints = 0;
    private final BigInteger GALAXY_COLLAPSE_COST = new BigInteger("1000000000000000000"); // 1 Quintillion stardust
    
    // --- NEW: Big Crunch Variables ---
    private int cosmicEssence = 0;
    private final int BIG_CRUNCH_COST = 10000; // Cost in Singularities

    // --- Feature: Shooting Star Boost ---
    private boolean isBoostActive = false;
    private int boostMultiplier = 1;
    
    // --- NEW: Special Events System ---
    private boolean isEventActive = false;
    private String currentEventType = "";
    private long eventEndTime = 0;
    private Handler eventHandler = new Handler();
    private Runnable eventSpawnerRunnable;

    // --- Game Data Lists ---
    private final List<Upgrade> upgrades = new ArrayList<>();
    private final List<ResearchUpgrade> researchUpgrades = new ArrayList<>();
    private final List<Achievement> achievements = new ArrayList<>();
    private final List<Skill> skills = new ArrayList<>();

    // --- Handlers ---
    private final Handler gameLoopHandler = new Handler();
    private final Handler twinkleHandler = new Handler();
    private final Handler riftHandler = new Handler();
    private Runnable riftSpawnerRunnable;
    private Handler autoSaveHandler = new Handler();
    private Runnable autoSaveRunnable;
    private Handler autoClickHandler = new Handler();
    private Runnable autoClickRunnable;
    private Handler autoBuyHandler = new Handler();
    private Runnable autoBuyRunnable;

    // --- Other Game State ---
    private int currentBuyAmount = 1; // NEW: To track buy amount (1, 10, or 25)
    private final Random random = new Random();
    private long totalClicks = 0;
    private BigInteger totalStardustEverEarned = BigInteger.ZERO;
    private boolean soundEnabled = true;
    
    // --- Performance Optimization ---
    private int frameSkipCounter = 0;
    private final int FRAME_SKIP_INTERVAL = 3; // Update UI every 3rd frame when possible
    private boolean isInBackground = false;
    
    // --- NEW: Statistics Tracking ---
    private long gameStartTime = System.currentTimeMillis();
    private long totalTimePlayed = 0;
    private long sessionStartTime = System.currentTimeMillis();
    private int totalPrestigeCount = 0;
    private int totalGalaxyCollapseCount = 0;
    private int totalBigCrunchCount = 0;
    private BigInteger highestStardustReached = BigInteger.ZERO;
    private long bestSpsAchieved = 0;
    private long totalUpgradesPurchased = 0;
    private int totalResearchPurchased = 0;
    private final Map<String, Integer> eventsSeen = new HashMap<>();
    private long averageClicksPerSecond = 0;
    private BigInteger totalStardustFromClicks = BigInteger.ZERO;
    private BigInteger totalStardustFromProduction = BigInteger.ZERO;

    private SharedPreferences prefs;

    // --- Sound Effects ---
    private SoundPool soundPool;
    private int clickSoundId, upgradeSoundId, milestoneSoundId, achievementSoundId, riftSoundId;
    private int buttonSoundId, notificationSoundId, skillUnlockSoundId;
    
    // --- Background Music ---
    private android.media.MediaPlayer backgroundMusic;
    private boolean musicEnabled = true;
    private float musicVolume = 0.5f;
    private float soundVolume = 1.0f;

    // --- SharedPreferences Keys ---
    private static final String PREFS_NAME = "CosmicClickerPrefs";
    private static final String KEY_STARDUST = "stardustCount";
    private static final String KEY_SINGULARITY = "singularityCount";
    private static final String KEY_GALAXY_POINTS = "galaxyPoints"; // NEW
    private static final String KEY_COSMIC_ESSENCE = "cosmicEssence"; // NEW
    private static final String KEY_UPGRADE_COUNT_PREFIX = "upgrade_count_";
    private static final String KEY_RESEARCH_PREFIX = "research_";
    private static final String KEY_ACHIEVEMENT_PREFIX = "achievement_";
    private static final String KEY_SKILL_LEVEL_PREFIX = "skill_level_";
    private static final String KEY_LAST_ONLINE_TIME = "lastOnlineTime";
    private static final String KEY_LAST_DAILY_REWARD = "lastDailyReward";
    private static final String KEY_TOTAL_CLICKS = "totalClicks";
    private static final String KEY_TOTAL_STARDUST_EARNED = "totalStardustEarned";
    private static final long DAILY_REWARD_AMOUNT = 1000000000;
    
    // --- NEW: Statistics Save Keys ---
    private static final String KEY_TOTAL_TIME_PLAYED = "totalTimePlayed";
    private static final String KEY_TOTAL_PRESTIGE_COUNT = "totalPrestigeCount";
    private static final String KEY_TOTAL_GALAXY_COUNT = "totalGalaxyCount";
    private static final String KEY_TOTAL_BIG_CRUNCH_COUNT = "totalBigCrunchCount";
    private static final String KEY_HIGHEST_STARDUST = "highestStardust";
    private static final String KEY_BEST_SPS = "bestSps";
    private static final String KEY_TOTAL_UPGRADES_PURCHASED = "totalUpgradesPurchased";
    private static final String KEY_TOTAL_RESEARCH_PURCHASED = "totalResearchPurchased";
    private static final String KEY_EVENTS_SEEN_PREFIX = "eventsSeen_";
    private static final String KEY_STARDUST_FROM_CLICKS = "stardustFromClicks";
    private static final String KEY_STARDUST_FROM_PRODUCTION = "stardustFromProduction";
    private static final String KEY_SOUND_ENABLED = "soundEnabled";
    private static final String KEY_MUSIC_ENABLED = "musicEnabled";
    private static final String KEY_MUSIC_VOLUME = "musicVolume";
    private static final String KEY_SOUND_VOLUME = "soundVolume";

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

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadGame();

        setupClickListeners();
        setupTabs();

        startGameLoop();
        startAutoSave();
        startAutomation();
        startEventSpawner();

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
        isInBackground = true;
        pauseBackgroundMusic();
        saveGame();
        gameLoopHandler.removeCallbacksAndMessages(null);
        riftHandler.removeCallbacks(riftSpawnerRunnable);
        twinkleHandler.removeCallbacksAndMessages(null);
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        autoClickHandler.removeCallbacks(autoClickRunnable);
        autoBuyHandler.removeCallbacks(autoBuyRunnable);
        eventHandler.removeCallbacks(eventSpawnerRunnable);
    }
    @Override
    protected void onResume() {
        super.onResume();
        isInBackground = false;
        startBackgroundMusic();
        calculateOfflineProgress();
        checkDailyReward();
        startGameLoop();
        startRiftSpawner();
        startAutoSave();
        startAutomation();
        startEventSpawner();
        createTwinklingStars(15);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameLoopHandler.removeCallbacksAndMessages(null);
        riftHandler.removeCallbacks(riftSpawnerRunnable);
        twinkleHandler.removeCallbacksAndMessages(null);
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        autoClickHandler.removeCallbacks(autoClickRunnable);
        autoBuyHandler.removeCallbacks(autoBuyRunnable);
        eventHandler.removeCallbacks(eventSpawnerRunnable);
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

    // --- Initialization Methods ---
    private void initUI() {
        rootLayout = findViewById(R.id.root_layout);
        tvStardustCount = findViewById(R.id.tvStardustCount);
        tvSps = findViewById(R.id.tvSps);
        tvSingularityCount = findViewById(R.id.tvSingularityCount);
        tvEventStatus = findViewById(R.id.tvEventStatus);
        btnClickStar = findViewById(R.id.btnClickStar);
        btnSettings = findViewById(R.id.btnSettings);
        btnAchievements = findViewById(R.id.btnAchievements);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnSkillTree = findViewById(R.id.btnSkillTree);
        btnSupernova = findViewById(R.id.btnSupernova);
        btnShootingStar = findViewById(R.id.btnShootingStar);
        btnBigCrunch = findViewById(R.id.btnBigCrunch); // NEW
        btnGalaxyCollapse = findViewById(R.id.btnGalaxyCollapse); // NEW
        llUpgradesContainer = findViewById(R.id.llUpgradesContainer);
        tabLayout = findViewById(R.id.tabLayout);

        // --- NEW: Find the buy amount toggle buttons ---
        btnBuy1 = findViewById(R.id.btnBuy1);
        btnBuy10 = findViewById(R.id.btnBuy10);
        btnBuy25 = findViewById(R.id.btnBuy25);
        btnBuy50 = findViewById(R.id.btnBuy50);
        btnBuy100 = findViewById(R.id.btnBuy100);
    }
    private void setupClickListeners() {
        btnClickStar.setOnClickListener(v -> onStarClicked());
        btnSupernova.setOnClickListener(v -> { playButtonSound(); triggerSupernova(); });
        btnShootingStar.setOnClickListener(v -> { playButtonSound(); activateShootingStar(); });
        btnSettings.setOnClickListener(v -> { playButtonSound(); showSettingsDialog(); });
        btnAchievements.setOnClickListener(v -> { playButtonSound(); showAchievementsDialog(); });
        btnStatistics.setOnClickListener(v -> { playButtonSound(); showStatisticsDialog(); });
        btnSkillTree.setOnClickListener(v -> { playButtonSound(); showSkillTreeDialog(); });
        btnBigCrunch.setOnClickListener(v -> { playButtonSound(); triggerBigCrunch(); }); // NEW
        btnGalaxyCollapse.setOnClickListener(v -> { playButtonSound(); triggerGalaxyCollapse(); }); // NEW

        // --- NEW: Listeners for buy amount toggles ---
        btnBuy1.setOnClickListener(v -> { playButtonSound(); setBuyAmount(1); });
        btnBuy10.setOnClickListener(v -> { playButtonSound(); setBuyAmount(10); });
        btnBuy25.setOnClickListener(v -> { playButtonSound(); setBuyAmount(25); });
        btnBuy50.setOnClickListener(v -> { playButtonSound(); setBuyAmount(50); });
        btnBuy100.setOnClickListener(v -> { playButtonSound(); setBuyAmount(100); });
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
        // Early Game - Better balanced progression
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
        
        // Mid Game - Smoother scaling
        upgrades.add(new Upgrade("Cosmic Web Weaver", new BigInteger("1100000000000"), 65000000));
        upgrades.add(new Upgrade("Reality Catalyst", new BigInteger("210000000000000"), 450000000));
        upgrades.add(new Upgrade("Quantum Synthesizer", new BigInteger("3800000000000000"), 3200000000L));
        upgrades.add(new Upgrade("Multiverse Gateway", new BigInteger("62000000000000000"), 24000000000L));
        
        // Late Game - Endgame content
        upgrades.add(new Upgrade("Dimension Forge", new BigInteger("920000000000000000"), 180000000000L));
        upgrades.add(new Upgrade("Time-Space Anomaly", new BigInteger("14000000000000000000"), 1400000000000L));
        upgrades.add(new Upgrade("Omniverse Core", new BigInteger("210000000000000000000"), 11000000000000L));
        upgrades.add(new Upgrade("Infinity Engine", new BigInteger("3100000000000000000000"), 84000000000000L));
        upgrades.add(new Upgrade("Eternity Weaver", new BigInteger("46000000000000000000000"), 620000000000000L));
        upgrades.add(new Upgrade("Absolute Zero", new BigInteger("680000000000000000000000"), 4600000000000000L));
    }
    private void setupResearchUpgrades() {
        researchUpgrades.clear();
        // Early Game Research
        researchUpgrades.add(new ResearchUpgrade("research_click_sps", "Stardust Infusion", "Each click also generates 1% of your SPS.", new BigInteger("50000")));
        researchUpgrades.add(new ResearchUpgrade("research_sps_boost", "Advanced Lensing", "All Celestial Objects produce 10% more Stardust.", new BigInteger("250000")));
        researchUpgrades.add(new ResearchUpgrade("research_auto_click", "Cosmic Automation", "Automatically clicks the star every 5 seconds.", new BigInteger("5000000")));
        
        // Mid Game Research
        researchUpgrades.add(new ResearchUpgrade("research_milestone", "Harmonic Resonance", "Milestone bonuses are 25% more effective (x2.5 instead of x2).", new BigInteger("100000000")));
        researchUpgrades.add(new ResearchUpgrade("research_cheaper", "Cosmic Bargaining", "All upgrades and research are 5% cheaper.", new BigInteger("50000000000")));
        researchUpgrades.add(new ResearchUpgrade("research_bulk_buy", "Quantum Commerce", "Unlock x50 and x100 bulk buy options.", new BigInteger("500000000000")));
        
        // Late Game Research
        researchUpgrades.add(new ResearchUpgrade("research_rift_doubler", "Rift Stabilization", "Cosmic Rift rewards are permanently doubled.", new BigInteger("1000000000000000")));
        researchUpgrades.add(new ResearchUpgrade("research_auto_buy", "Neural Networks", "Automatically purchases the cheapest available upgrade every 10 seconds.", new BigInteger("50000000000000000")));
        researchUpgrades.add(new ResearchUpgrade("research_critical_mass", "Critical Mass Theory", "All production buildings gain +50% effectiveness.", new BigInteger("2000000000000000000")));
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
        achievements.add(new Achievement("forge_1", "Dimensional Engineer", "Build a Dimension Forge.", AchievementType.UPGRADE_COUNT, 1, 14)); // Index 14 is Dimension Forge
        achievements.add(new Achievement("click_1000", "Dedicated Clicker", "Click the star 1,000 times", AchievementType.TOTAL_CLICKS, 1000));
        achievements.add(new Achievement("stardust_1b", "Stardust Billionaire", "Earn 1 billion total stardust", AchievementType.TOTAL_STARDUST, 1000000000));
        achievements.add(new Achievement("automation_master", "Automation Master", "Unlock both auto-click and auto-buy research", AchievementType.RESEARCH_COUNT, 8));
        achievements.add(new Achievement("infinity_1", "Infinite Power", "Build an Infinity Engine.", AchievementType.UPGRADE_COUNT, 1, 17)); // Index 17 is Infinity Engine
        achievements.add(new Achievement("galaxy_1", "Galaxy Destroyer", "Perform your first Galaxy Collapse.", AchievementType.GALAXY_COUNT, 1));
        achievements.add(new Achievement("skill_master", "Skill Master", "Max out any skill tree path.", AchievementType.RESEARCH_COUNT, 15));
        achievements.add(new Achievement("ultimate_clicker", "Ultimate Clicker", "Reach 1 Trillion SPS.", AchievementType.REACH_SPS, 1000000000000L));
        achievements.add(new Achievement("event_seeker", "Event Seeker", "Experience all 4 event types.", AchievementType.TOTAL_CLICKS, 10000));
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
        
        // --- NEW: Path of the Innovator (Research & Efficiency) ---
        skills.add(new Skill("research_1", "Cosmic Research", "Research upgrades are 5% cheaper per level.", SkillType.RESEARCH_COST_REDUCTION, 5, 10, null));
        skills.add(new Skill("galaxy_1", "Galactic Mastery", "+5% Galaxy Points earned per level.", SkillType.GALAXY_BOOST, 8, 25, "research_1"));
        skills.add(new Skill("event_1", "Event Frequency", "Special events occur 10% more often per level.", SkillType.EVENT_BOOST, 3, 75, "galaxy_1"));
    }

    // --- Core Game Logic ---
    private void onStarClicked() {
        animateClick(btnClickStar);
        BigInteger clickValue = BigInteger.ONE;
        clickValue = clickValue.add(BigInteger.valueOf(getSkillLevel("click_1")));
        if (getResearch("research_click_sps").isPurchased()) {
            clickValue = clickValue.add(BigInteger.valueOf(totalSps / 100));
        }
        
        // Check for critical clicks
        int critChance = getSkillLevel("crit_1");
        if (critChance > 0 && random.nextInt(100) < critChance) {
            clickValue = clickValue.multiply(BigInteger.valueOf(1000));
            Toast.makeText(this, "💥 CRITICAL CLICK! 1000x DAMAGE!", Toast.LENGTH_SHORT).show();
        }
        clickValue = getEventModifiedClickValue(clickValue);
        showStardustPopup(clickValue, btnClickStar);
        stardustCount = stardustCount.add(clickValue);
        totalClicks++;
        totalStardustEverEarned = totalStardustEverEarned.add(clickValue);
        
        // Update statistics
        totalStardustFromClicks = totalStardustFromClicks.add(clickValue);
        updateStatistics();
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
            // Now, actually increment the counts and check for milestones
            for (int i = 0; i < amount; i++) {
                upgrade.count++;
                totalUpgradesPurchased++;
                if (upgrade.count % Upgrade.MILESTONE_INTERVAL == 0) { // Check for milestones
                    playMilestoneSound();
                    showMilestoneAnimation(upgrade.getCurrentName(), upgrade.count);
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
        
        // Apply research cost reduction skill
        int researchSkillLevel = getSkillLevel("research_1");
        if (researchSkillLevel > 0) {
            double reduction = researchSkillLevel * 0.05;
            cost = cost.multiply(BigInteger.valueOf((long)((1.0 - reduction) * 100))).divide(BigInteger.valueOf(100));
        }
        if (stardustCount.compareTo(cost) >= 0) {
            stardustCount = stardustCount.subtract(cost);
            research.setPurchased(true);
            totalResearchPurchased++;
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

        for (Upgrade upgrade : upgrades) {
            // Pass the skill level as the second argument
            totalSpsDecimal = totalSpsDecimal.add(upgrade.getTotalCpsForThisUpgrade(harmonicResonance, celestialArchitectureLevel));
        }

        if (getResearch("research_sps_boost").isPurchased()) {
            totalSpsDecimal = totalSpsDecimal.multiply(new BigDecimal("1.1"));
        }
        
        if (getResearch("research_critical_mass").isPurchased()) {
            totalSpsDecimal = totalSpsDecimal.multiply(new BigDecimal("1.5"));
        }

        int spsSkillLevel = getSkillLevel("sps_1");
        BigDecimal spsSkillBonus = BigDecimal.valueOf(spsSkillLevel * 0.02);
        totalSpsDecimal = totalSpsDecimal.multiply(BigDecimal.ONE.add(spsSkillBonus));
        
        // Apply galaxy points bonus
        if (galaxyPoints > 0) {
            BigDecimal galaxyBonus = BigDecimal.valueOf(galaxyPoints * 0.1);
            totalSpsDecimal = totalSpsDecimal.multiply(BigDecimal.ONE.add(galaxyBonus));
        }

        if (cosmicEssence > 0) {
            BigDecimal essenceMultiplier = BigDecimal.valueOf(Math.pow(2, cosmicEssence));
            totalSpsDecimal = totalSpsDecimal.multiply(essenceMultiplier);
        }

        // Apply singularity bonus using the new helper method
        BigDecimal singularityMultiplier = BigDecimal.ONE.add(BigDecimal.valueOf(singularityCount * getSingularityBonus()));
        totalSpsDecimal = totalSpsDecimal.multiply(singularityMultiplier);
        long baseSps = totalSpsDecimal.toBigInteger().longValue();
        totalSps = getEventModifiedSps(baseSps);
    }

    private void triggerSupernova() {
        if (stardustCount.compareTo(supernovaCost) >= 0) {
            double earned = Math.max(1, Math.log10(stardustCount.doubleValue()) - 11);
            int singSkillLevel = getSkillLevel("sing_1");
            double singSkillBonus = singSkillLevel * 0.02;
            earned = earned * (1 + singSkillBonus);
            int earnedSingularities = (int) earned;

            singularityCount += earnedSingularities;
            totalPrestigeCount++;
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

    // --- NEW: Galaxy Collapse Methods ---
    private void triggerGalaxyCollapse() {
        if (stardustCount.compareTo(GALAXY_COLLAPSE_COST) >= 0) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage("🌌 Initiate Galaxy Collapse?\n\nThis will reset your Stardust and Upgrades in exchange for Galaxy Points. Galaxy Points provide a permanent 10% SPS boost each. Continue?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Collapse", (dialog, which) -> {
                        int earnedPoints = (int) Math.max(1, Math.log10(stardustCount.doubleValue()) - 17);
                        
                        // Apply galaxy boost skill
                        int galaxySkillLevel = getSkillLevel("galaxy_1");
                        if (galaxySkillLevel > 0) {
                            double bonus = galaxySkillLevel * 0.05;
                            earnedPoints = (int) (earnedPoints * (1 + bonus));
                        }
                        
                        galaxyPoints += earnedPoints;
                        totalGalaxyCollapseCount++;
                        
                        Toast.makeText(this, "GALAXY COLLAPSE! You earned " + earnedPoints + " Galaxy Points!", Toast.LENGTH_LONG).show();
                        playMilestoneSound();
                        
                        // Reset stardust and upgrades but keep research, skills, singularities
                        stardustCount = BigInteger.ZERO;
                        totalClicks = 0;
                        for (Upgrade upgrade : upgrades) upgrade.count = 0;
                        
                        saveGame();
                        loadGame();
                    })
                    .show();
        } else {
            Toast.makeText(this, "You need at least " + formatBigNumber(GALAXY_COLLAPSE_COST) + " stardust for Galaxy Collapse.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- NEW: Big Crunch Methods ---
    private void triggerBigCrunch() {
        if (singularityCount >= BIG_CRUNCH_COST) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage("💥 Initiate Big Crunch?\n\nThis will reset your Stardust, Upgrades, Research, Skills, and ALL Singularities in exchange for 1 Cosmic Essence. Cosmic Essence provides a massive permanent boost. Are you sure?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("DO IT", (dialog, which) -> {
                        cosmicEssence++;
                        singularityCount = 0; // Reset singularities
                        totalBigCrunchCount++;

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
        editor.putInt(KEY_GALAXY_POINTS, galaxyPoints); // NEW
        editor.putInt(KEY_COSMIC_ESSENCE, cosmicEssence); // NEW
        for (int i = 0; i < upgrades.size(); i++) editor.putInt(KEY_UPGRADE_COUNT_PREFIX + i, upgrades.get(i).count);
        for (ResearchUpgrade research : researchUpgrades) editor.putBoolean(KEY_RESEARCH_PREFIX + research.getId(), research.isPurchased());
        for (Achievement achievement : achievements) editor.putBoolean(KEY_ACHIEVEMENT_PREFIX + achievement.getId(), achievement.isUnlocked());
        for (Skill skill : skills) {
            editor.putInt(KEY_SKILL_LEVEL_PREFIX + skill.getId(), skill.getCurrentLevel());
        }
        editor.putLong(KEY_TOTAL_CLICKS, totalClicks);
        editor.putString(KEY_TOTAL_STARDUST_EARNED, totalStardustEverEarned.toString());
        editor.putLong(KEY_LAST_ONLINE_TIME, System.currentTimeMillis());
        
        // Save statistics
        editor.putLong(KEY_TOTAL_TIME_PLAYED, totalTimePlayed + (System.currentTimeMillis() - sessionStartTime));
        editor.putInt(KEY_TOTAL_PRESTIGE_COUNT, totalPrestigeCount);
        editor.putInt(KEY_TOTAL_GALAXY_COUNT, totalGalaxyCollapseCount);
        editor.putInt(KEY_TOTAL_BIG_CRUNCH_COUNT, totalBigCrunchCount);
        editor.putString(KEY_HIGHEST_STARDUST, highestStardustReached.toString());
        editor.putLong(KEY_BEST_SPS, bestSpsAchieved);
        editor.putLong(KEY_TOTAL_UPGRADES_PURCHASED, totalUpgradesPurchased);
        editor.putInt(KEY_TOTAL_RESEARCH_PURCHASED, totalResearchPurchased);
        editor.putString(KEY_STARDUST_FROM_CLICKS, totalStardustFromClicks.toString());
        editor.putString(KEY_STARDUST_FROM_PRODUCTION, totalStardustFromProduction.toString());
        
        // Save events seen
        for (Map.Entry<String, Integer> entry : eventsSeen.entrySet()) {
            editor.putInt(KEY_EVENTS_SEEN_PREFIX + entry.getKey(), entry.getValue());
        }
        
        // Save audio settings
        editor.putBoolean(KEY_SOUND_ENABLED, soundEnabled);
        editor.putBoolean(KEY_MUSIC_ENABLED, musicEnabled);
        editor.putFloat(KEY_MUSIC_VOLUME, musicVolume);
        editor.putFloat(KEY_SOUND_VOLUME, soundVolume);
        
        editor.apply();
    }

    private void loadGame() {
        stardustCount = new BigInteger(prefs.getString(KEY_STARDUST, "0"));
        singularityCount = prefs.getInt(KEY_SINGULARITY, 0);
        galaxyPoints = prefs.getInt(KEY_GALAXY_POINTS, 0); // NEW
        cosmicEssence = prefs.getInt(KEY_COSMIC_ESSENCE, 0); // NEW
        for (int i = 0; i < upgrades.size(); i++) upgrades.get(i).count = prefs.getInt(KEY_UPGRADE_COUNT_PREFIX + i, 0);
        for (ResearchUpgrade research : researchUpgrades) research.setPurchased(prefs.getBoolean(KEY_RESEARCH_PREFIX + research.getId(), false));
        for (Achievement achievement : achievements) achievement.setUnlocked(prefs.getBoolean(KEY_ACHIEVEMENT_PREFIX + achievement.getId(), false));
        for (Skill skill : skills) {
            skill.setCurrentLevel(prefs.getInt(KEY_SKILL_LEVEL_PREFIX + skill.getId(), 0));
        }
        totalClicks = prefs.getLong(KEY_TOTAL_CLICKS, 0);
        totalStardustEverEarned = new BigInteger(prefs.getString(KEY_TOTAL_STARDUST_EARNED, "0"));
        
        // Load statistics
        totalTimePlayed = prefs.getLong(KEY_TOTAL_TIME_PLAYED, 0);
        totalPrestigeCount = prefs.getInt(KEY_TOTAL_PRESTIGE_COUNT, 0);
        totalGalaxyCollapseCount = prefs.getInt(KEY_TOTAL_GALAXY_COUNT, 0);
        totalBigCrunchCount = prefs.getInt(KEY_TOTAL_BIG_CRUNCH_COUNT, 0);
        highestStardustReached = new BigInteger(prefs.getString(KEY_HIGHEST_STARDUST, "0"));
        bestSpsAchieved = prefs.getLong(KEY_BEST_SPS, 0);
        totalUpgradesPurchased = prefs.getLong(KEY_TOTAL_UPGRADES_PURCHASED, 0);
        totalResearchPurchased = prefs.getInt(KEY_TOTAL_RESEARCH_PURCHASED, 0);
        totalStardustFromClicks = new BigInteger(prefs.getString(KEY_STARDUST_FROM_CLICKS, "0"));
        totalStardustFromProduction = new BigInteger(prefs.getString(KEY_STARDUST_FROM_PRODUCTION, "0"));
        
        // Load events seen
        eventsSeen.clear();
        eventsSeen.put("stardust_rain", prefs.getInt(KEY_EVENTS_SEEN_PREFIX + "stardust_rain", 0));
        eventsSeen.put("cosmic_storm", prefs.getInt(KEY_EVENTS_SEEN_PREFIX + "cosmic_storm", 0));
        eventsSeen.put("black_hole_surge", prefs.getInt(KEY_EVENTS_SEEN_PREFIX + "black_hole_surge", 0));
        eventsSeen.put("time_warp", prefs.getInt(KEY_EVENTS_SEEN_PREFIX + "time_warp", 0));
        
        // Load audio settings
        soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true);
        musicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true);
        musicVolume = prefs.getFloat(KEY_MUSIC_VOLUME, 0.5f);
        soundVolume = prefs.getFloat(KEY_SOUND_VOLUME, 1.0f);
        updateMusicVolume();
        
        sessionStartTime = System.currentTimeMillis();
        recalculateSps();
        updateUI();
        setBuyAmount(currentBuyAmount); // Refresh buy amount buttons
        if (tabLayout.getSelectedTabPosition() == 0) refreshCelestialObjectsUI(); else refreshResearchUI();
    }

    // --- NEW: Method to handle buy amount selection ---
    private void setBuyAmount(int amount) {
        currentBuyAmount = amount;

        // Update the visual state of all buttons
        btnBuy1.setAlpha(amount == 1 ? 1.0f : 0.5f);
        btnBuy10.setAlpha(amount == 10 ? 1.0f : 0.5f);
        btnBuy25.setAlpha(amount == 25 ? 1.0f : 0.5f);
        
        // Only show x50 and x100 if research is unlocked
        boolean bulkUnlocked = getResearch("research_bulk_buy").isPurchased();
        if (bulkUnlocked) {
            btnBuy50.setVisibility(View.VISIBLE);
            btnBuy100.setVisibility(View.VISIBLE);
            btnBuy50.setAlpha(amount == 50 ? 1.0f : 0.5f);
            btnBuy100.setAlpha(amount == 100 ? 1.0f : 0.5f);
        } else {
            btnBuy50.setVisibility(View.GONE);
            btnBuy100.setVisibility(View.GONE);
        }

        // Refresh the UI to show the new costs for the selected amount
        if (tabLayout.getSelectedTabPosition() == 0) {
            refreshCelestialObjectsUI();
        }
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
            buy.setOnClickListener(view -> {
                animateUpgradePurchase(view);
                buyUpgrade(upgrade);
            });

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
                buy.setOnClickListener(view -> {
                    animateUpgradePurchase(view);
                    buyResearch(research);
                });
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
        String galaxyText = (galaxyPoints > 0) ? String.format(Locale.getDefault(), " | %d Galaxy", galaxyPoints) : "";
        String essenceText = (cosmicEssence > 0) ? String.format(Locale.getDefault(), " | %d Essence", cosmicEssence) : "";
        double currentSingularityBonus = getSingularityBonus();
        int bonusPercent = (int) (singularityCount * currentSingularityBonus * 100);
        tvSingularityCount.setText(String.format(Locale.getDefault(), "%d Singularities (+%d%% SPS)%s%s", singularityCount, bonusPercent, galaxyText, essenceText));

        btnSupernova.setVisibility(stardustCount.compareTo(supernovaCost) >= 0 ? View.VISIBLE : View.GONE);
        btnGalaxyCollapse.setVisibility(stardustCount.compareTo(GALAXY_COLLAPSE_COST) >= 0 ? View.VISIBLE : View.GONE);
        btnBigCrunch.setVisibility(singularityCount >= BIG_CRUNCH_COST ? View.VISIBLE : View.GONE);
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
            playNotificationSound();
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
            playNotificationSound();
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
                if (!isInBackground) {
                    BigInteger spsToAdd = BigInteger.valueOf(totalSps);
                    stardustCount = stardustCount.add(spsToAdd);
                    totalStardustEverEarned = totalStardustEverEarned.add(spsToAdd);
                    totalStardustFromProduction = totalStardustFromProduction.add(spsToAdd);
                    
                    // Use frame skipping for better performance
                    frameSkipCounter++;
                    if (frameSkipCounter >= FRAME_SKIP_INTERVAL || totalSps > 1000000) {
                        updateUI();
                        frameSkipCounter = 0;
                    }
                    
                    // Only check achievements every few seconds to save performance
                    if (System.currentTimeMillis() % 3000 < 1000) {
                        checkAchievements();
                        updateStatistics();
                    }
                }
                
                long delay = isInBackground ? 5000 : 1000; // Slower updates when in background
                gameLoopHandler.postDelayed(this, delay);
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
        // Enhanced particle system with better performance and visuals
        int particleCount = isEventActive ? 25 : 15; // More particles during events
        
        for (int i = 0; i < particleCount; i++) {
            final ImageView particle = new ImageView(this);
            particle.setImageResource(R.drawable.ic_stardust_particle);
            
            // Varied particle sizes
            int size = 15 + random.nextInt(25);
            particle.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            particle.setX(x - size/2f);
            particle.setY(y - size/2f);
            
            // Random colors for special events
            if (isEventActive) {
                switch (currentEventType) {
                    case "stardust_rain":
                        particle.setColorFilter(getResources().getColor(R.color.accent_cyan, getTheme()));
                        break;
                    case "cosmic_storm":
                        particle.setColorFilter(getResources().getColor(R.color.accent_gold, getTheme()));
                        break;
                    case "black_hole_surge":
                        particle.setColorFilter(getResources().getColor(R.color.star_color, getTheme()));
                        break;
                }
            }
            
            rootLayout.addView(particle);
            
            // More dynamic particle movement
            float angle = (float) (random.nextFloat() * 2 * Math.PI);
            float velocity = 200 + random.nextFloat() * 300;
            float endX = x + (float) (Math.cos(angle) * velocity);
            float endY = y + (float) (Math.sin(angle) * velocity);
            
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(particle,
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_X, endX - x),
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, endY - y),
                    PropertyValuesHolder.ofFloat(View.ALPHA, 0f),
                    PropertyValuesHolder.ofFloat(View.ROTATION, random.nextFloat() * 720)
            );
            
            animator.setDuration(600 + random.nextInt(600));
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
    private void checkAchievements() {
        for (Achievement achievement : achievements) {
            if (!achievement.isUnlocked()) {
                boolean newlyUnlocked = false;
                switch (achievement.getType()) {
                    case TOTAL_CLICKS: 
                        if (totalClicks >= achievement.getGoal()) newlyUnlocked = true; 
                        break;
                    case TOTAL_STARDUST: 
                        if (totalStardustEverEarned.compareTo(BigInteger.valueOf(achievement.getGoal())) >= 0) newlyUnlocked = true; 
                        break;
                    case UPGRADE_COUNT: 
                        int index = achievement.getTargetUpgradeIndex(); 
                        if (index >= 0 && index < upgrades.size() && upgrades.get(index).count >= achievement.getGoal()) newlyUnlocked = true; 
                        break;
                    case REACH_SPS: 
                        if (totalSps >= achievement.getGoal()) newlyUnlocked = true; 
                        break;
                    case SINGULARITY_COUNT:
                        if (singularityCount >= achievement.getGoal()) newlyUnlocked = true;
                        break;
                    case RESEARCH_COUNT:
                        int researchCount = 0;
                        for (ResearchUpgrade research : researchUpgrades) {
                            if (research.isPurchased()) researchCount++;
                        }
                        if (researchCount >= achievement.getGoal()) newlyUnlocked = true;
                        break;
                    case ESSENCE_COUNT:
                        if (cosmicEssence >= achievement.getGoal()) newlyUnlocked = true;
                        break;
                    case GALAXY_COUNT:
                        if (galaxyPoints >= achievement.getGoal()) newlyUnlocked = true;
                        break;
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
        AlertDialog dialog = builder.setView(dialogView).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    // --- Skill Tree Methods ---
    private void showSkillTreeDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_skill_tree, null);
        LinearLayout container = dialogView.findViewById(R.id.llSkillContainer);
        TextView tvPoints = dialogView.findViewById(R.id.tvSingularityPoints);
        AlertDialog dialog = builder.setView(dialogView).create();
        dialog.setCanceledOnTouchOutside(true);
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
            playSkillUnlockSound();
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
        soundPool = new SoundPool.Builder().setMaxStreams(8).setAudioAttributes(audioAttributes).build();
        
        // Load all sound effects
        clickSoundId = soundPool.load(this, R.raw.click_sound, 1);
        upgradeSoundId = soundPool.load(this, R.raw.upgrade_sound, 1);
        milestoneSoundId = soundPool.load(this, R.raw.milestone_sound, 1);
        achievementSoundId = soundPool.load(this, R.raw.achievement_sound, 1);
        
        // Use existing sounds as placeholders for new sounds until new audio files are added
        buttonSoundId = clickSoundId; // Placeholder: use click sound for buttons
        notificationSoundId = achievementSoundId; // Placeholder: use achievement sound for notifications
        skillUnlockSoundId = milestoneSoundId; // Placeholder: use milestone sound for skill unlocks
        riftSoundId = milestoneSoundId; // Placeholder: use milestone sound for rifts
        
        setupBackgroundMusic();
    }
    
    private void setupBackgroundMusic() {
        try {
            // TODO: Add background_music.mp3 to res/raw folder
            // For now, background music is disabled until audio file is added
            // backgroundMusic = MediaPlayer.create(this, R.raw.background_music);
            // if (backgroundMusic != null) {
            //     backgroundMusic.setLooping(true);
            //     backgroundMusic.setVolume(musicVolume, musicVolume);
            // }
        } catch (Exception e) {
            // Background music file not found, continue without background music
            backgroundMusic = null;
        }
    }
    private void playClickSound() { 
        if (soundEnabled && clickSoundId != 0) soundPool.play(clickSoundId, soundVolume, soundVolume, 0, 0, 1); 
    }
    private void playUpgradeSound() { 
        if (soundEnabled && upgradeSoundId != 0) soundPool.play(upgradeSoundId, soundVolume, soundVolume, 0, 0, 1); 
    }
    private void playMilestoneSound() { 
        if (soundEnabled && milestoneSoundId != 0) soundPool.play(milestoneSoundId, soundVolume, soundVolume, 1, 0, 1); 
    }
    private void playAchievementSound() { 
        if (soundEnabled && achievementSoundId != 0) soundPool.play(achievementSoundId, soundVolume * 0.8f, soundVolume * 0.8f, 2, 0, 1); 
    }
    private void playRiftSound() { 
        if (soundEnabled && riftSoundId != 0) soundPool.play(riftSoundId, soundVolume * 0.7f, soundVolume * 0.7f, 3, 0, 1); 
    }
    private void playButtonSound() { 
        if (soundEnabled && buttonSoundId != 0) soundPool.play(buttonSoundId, soundVolume * 0.5f, soundVolume * 0.5f, 0, 0, 1.2f); 
    }
    private void playNotificationSound() { 
        if (soundEnabled && notificationSoundId != 0) soundPool.play(notificationSoundId, soundVolume, soundVolume, 1, 0, 1); 
    }
    private void playSkillUnlockSound() { 
        if (soundEnabled && skillUnlockSoundId != 0) soundPool.play(skillUnlockSoundId, soundVolume, soundVolume, 2, 0, 1); 
    }
    
    // Background music controls
    private void startBackgroundMusic() {
        if (musicEnabled && backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
    }
    
    private void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }
    
    private void updateMusicVolume() {
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(musicVolume, musicVolume);
        }
    }
    private void animateClick(View view) {
        // Enhanced click animation with more dramatic effect
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f, 1.1f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 5f, -5f, 0f);
        
        set.playTogether(scaleX, scaleY, rotation);
        set.setDuration(300);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
        
        // Add a glow effect for events
        if (isEventActive && "stardust_rain".equals(currentEventType)) {
            ObjectAnimator glow = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f);
            glow.setDuration(200);
            glow.start();
        }
    }
    
    private void animateUpgradePurchase(View view) {
        // Create a satisfying purchase animation
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1.05f, 1f);
        
        set.playTogether(scaleX, scaleY);
        set.setDuration(200);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
        
        // Add a color flash effect
        ObjectAnimator colorFlash = ObjectAnimator.ofArgb(view, "backgroundColor", 
                getResources().getColor(R.color.accent_gold, getTheme()),
                getResources().getColor(android.R.color.transparent, getTheme()));
        colorFlash.setDuration(300);
        colorFlash.start();
    }
    
    private void showMilestoneAnimation(String upgradeName, int count) {
        // Create a celebratory milestone animation
        TextView milestoneText = new TextView(this);
        milestoneText.setText(String.format("🎉 %s reached %d! 🎉", upgradeName, count));
        milestoneText.setTextColor(getResources().getColor(R.color.accent_gold, getTheme()));
        milestoneText.setTextSize(20f);
        milestoneText.setTypeface(null, Typeface.BOLD);
        milestoneText.setShadowLayer(5, 0, 0, getResources().getColor(R.color.black, getTheme()));
        milestoneText.setGravity(android.view.Gravity.CENTER);
        
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        milestoneText.setLayoutParams(params);
        rootLayout.addView(milestoneText);
        
        // Position in center of screen
        milestoneText.setX((rootLayout.getWidth() - milestoneText.getWidth()) / 2f);
        milestoneText.setY((rootLayout.getHeight() - milestoneText.getHeight()) / 2f);
        
        // Animate the milestone celebration
        AnimatorSet celebrationSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(milestoneText, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(milestoneText, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(milestoneText, "rotation", 0f, 10f, -10f, 0f);
        
        celebrationSet.playTogether(scaleX, scaleY, rotation);
        celebrationSet.setDuration(800);
        celebrationSet.setInterpolator(new DecelerateInterpolator());
        
        celebrationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Fade out after celebration
                milestoneText.animate()
                        .alpha(0f)
                        .translationY(-100)
                        .setDuration(1000)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (milestoneText.getParent() != null) {
                                    rootLayout.removeView(milestoneText);
                                }
                            }
                        })
                        .start();
            }
        });
        
        celebrationSet.start();
    }
    
    private void createTwinklingStars(int count) {
        if (rootLayout.getWidth() == 0 || isInBackground) return;
        
        // Reduce star count when performance is needed
        int actualCount = totalSps > 100000 ? Math.min(count, 8) : count;
        
        // Remove existing stars
        for (int i = 0; i < rootLayout.getChildCount(); i++) {
            if ("twinkling_star".equals(rootLayout.getChildAt(i).getTag())) {
                rootLayout.removeViewAt(i);
                i--;
            }
        }
        
        // Create new stars with better performance
        for (int i = 0; i < actualCount; i++) {
            final View star = new View(this);
            star.setTag("twinkling_star");
            star.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
            int size = random.nextInt(3) + 2;
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(size, size);
            star.setLayoutParams(params);
            rootLayout.addView(star, 0);
            
            // Only animate if not in performance mode
            if (totalSps < 500000) {
                twinkle(star);
            } else {
                // Static stars for performance
                star.setX(random.nextInt(rootLayout.getWidth()));
                star.setY(random.nextInt(rootLayout.getHeight()));
                star.setAlpha(0.3f + random.nextFloat() * 0.7f);
            }
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
        
        SwitchMaterial switchSound = dialogView.findViewById(R.id.switchSound);
        SwitchMaterial switchMusic = dialogView.findViewById(R.id.switchMusic);
        android.widget.SeekBar seekBarMusicVolume = dialogView.findViewById(R.id.seekBarMusicVolume);
        android.widget.SeekBar seekBarSoundVolume = dialogView.findViewById(R.id.seekBarSoundVolume);
        
        // Set current values
        switchSound.setChecked(soundEnabled);
        switchMusic.setChecked(musicEnabled);
        seekBarMusicVolume.setProgress((int)(musicVolume * 100));
        seekBarSoundVolume.setProgress((int)(soundVolume * 100));
        
        // Set listeners
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundEnabled = isChecked;
            saveGame(); // Save immediately
        });
        
        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            musicEnabled = isChecked;
            if (musicEnabled) {
                startBackgroundMusic();
            } else {
                pauseBackgroundMusic();
            }
            saveGame(); // Save immediately
        });
        
        seekBarMusicVolume.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicVolume = progress / 100.0f;
                    updateMusicVolume();
                }
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
                saveGame(); // Save when user stops dragging
            }
        });
        
        seekBarSoundVolume.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    soundVolume = progress / 100.0f;
                    // Play a test sound to preview volume
                    playButtonSound();
                }
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
                saveGame(); // Save when user stops dragging
            }
        });
        
        AlertDialog dialog = builder.setView(dialogView).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
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
    
    // --- NEW: Statistics Methods ---
    private void updateStatistics() {
        // Update highest stardust reached
        if (stardustCount.compareTo(highestStardustReached) > 0) {
            highestStardustReached = stardustCount;
        }
        
        // Update best SPS achieved
        if (totalSps > bestSpsAchieved) {
            bestSpsAchieved = totalSps;
        }
        
        // Calculate average clicks per second
        long currentSessionTime = System.currentTimeMillis() - sessionStartTime;
        if (currentSessionTime > 0) {
            averageClicksPerSecond = (totalClicks * 1000) / (totalTimePlayed + currentSessionTime);
        }
    }
    
    private void showStatisticsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_statistics, null);
        
        if (dialogView == null) {
            Toast.makeText(this, "Error loading statistics dialog", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Game Progress Statistics
        TextView tvTimePlayed = dialogView.findViewById(R.id.tvTimePlayed);
        TextView tvTotalClicks = dialogView.findViewById(R.id.tvTotalClicks);
        TextView tvTotalStardust = dialogView.findViewById(R.id.tvTotalStardust);
        TextView tvHighestStardust = dialogView.findViewById(R.id.tvHighestStardust);
        TextView tvBestSps = dialogView.findViewById(R.id.tvBestSps);
        
        // Prestige Statistics
        TextView tvTotalPrestiges = dialogView.findViewById(R.id.tvTotalPrestiges);
        TextView tvGalaxyCollapses = dialogView.findViewById(R.id.tvGalaxyCollapses);
        TextView tvBigCrunches = dialogView.findViewById(R.id.tvBigCrunches);
        
        // Purchase Statistics
        TextView tvUpgradesPurchased = dialogView.findViewById(R.id.tvUpgradesPurchased);
        TextView tvResearchPurchased = dialogView.findViewById(R.id.tvResearchPurchased);
        
        // Efficiency Statistics
        TextView tvClicksPerSecond = dialogView.findViewById(R.id.tvClicksPerSecond);
        TextView tvStardustFromClicks = dialogView.findViewById(R.id.tvStardustFromClicks);
        TextView tvStardustFromProduction = dialogView.findViewById(R.id.tvStardustFromProduction);
        
        // Event Statistics
        TextView tvEventsStardust = dialogView.findViewById(R.id.tvEventsStardust);
        TextView tvEventsCosmic = dialogView.findViewById(R.id.tvEventsCosmic);
        TextView tvEventsBlackhole = dialogView.findViewById(R.id.tvEventsBlackhole);
        TextView tvEventsTime = dialogView.findViewById(R.id.tvEventsTime);
        
        // Calculate current session + total time
        long currentTotalTime = totalTimePlayed + (System.currentTimeMillis() - sessionStartTime);
        updateStatistics();
        
        // Format and set the statistics
        tvTimePlayed.setText(formatTime(currentTotalTime));
        tvTotalClicks.setText(String.format("%,d clicks", totalClicks));
        tvTotalStardust.setText(formatBigNumber(totalStardustEverEarned));
        tvHighestStardust.setText(formatBigNumber(highestStardustReached));
        tvBestSps.setText(formatBigNumber(BigInteger.valueOf(bestSpsAchieved)) + "/sec");
        
        tvTotalPrestiges.setText(String.valueOf(totalPrestigeCount));
        tvGalaxyCollapses.setText(String.valueOf(totalGalaxyCollapseCount));
        tvBigCrunches.setText(String.valueOf(totalBigCrunchCount));
        
        tvUpgradesPurchased.setText(String.format("%,d upgrades", totalUpgradesPurchased));
        tvResearchPurchased.setText(String.format("%d research", totalResearchPurchased));
        
        tvClicksPerSecond.setText(String.format("%.2f clicks/sec", averageClicksPerSecond / 1000.0));
        tvStardustFromClicks.setText(formatBigNumber(totalStardustFromClicks));
        tvStardustFromProduction.setText(formatBigNumber(totalStardustFromProduction));
        
        tvEventsStardust.setText(String.valueOf(eventsSeen.getOrDefault("stardust_rain", 0)));
        tvEventsCosmic.setText(String.valueOf(eventsSeen.getOrDefault("cosmic_storm", 0)));
        tvEventsBlackhole.setText(String.valueOf(eventsSeen.getOrDefault("black_hole_surge", 0)));
        tvEventsTime.setText(String.valueOf(eventsSeen.getOrDefault("time_warp", 0)));
        
        AlertDialog dialog = builder.setView(dialogView).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
    
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    // --- NEW: Automation Features ---
    private void startAutomation() {
        startAutoClick();
        startAutoBuy();
    }
    
    private void startAutoClick() {
        if (getResearch("research_auto_click").isPurchased()) {
            autoClickRunnable = new Runnable() {
                @Override
                public void run() {
                    onStarClicked();
                    autoClickHandler.postDelayed(this, 5000); // Every 5 seconds
                }
            };
            autoClickHandler.postDelayed(autoClickRunnable, 5000);
        }
    }
    
    private void startAutoBuy() {
        if (getResearch("research_auto_buy").isPurchased()) {
            autoBuyRunnable = new Runnable() {
                @Override
                public void run() {
                    autoBuyCheapestUpgrade();
                    autoBuyHandler.postDelayed(this, 10000); // Every 10 seconds
                }
            };
            autoBuyHandler.postDelayed(autoBuyRunnable, 10000);
        }
    }
    
    private void autoBuyCheapestUpgrade() {
        Upgrade cheapest = null;
        BigInteger cheapestCost = null;
        boolean isCheaper = getResearch("research_cheaper").isPurchased();
        
        for (Upgrade upgrade : upgrades) {
            BigInteger cost = upgrade.getNextCost(isCheaper);
            if ((cheapest == null || cost.compareTo(cheapestCost) < 0) && stardustCount.compareTo(cost) >= 0) {
                cheapest = upgrade;
                cheapestCost = cost;
            }
        }
        
        if (cheapest != null) {
            buyUpgrade(cheapest);
        }
    }
    
    // --- NEW: Special Events System ---
    private void startEventSpawner() {
        eventSpawnerRunnable = new Runnable() {
            @Override
            public void run() {
                int baseChance = 15;
                int eventSkillLevel = getSkillLevel("event_1");
                int actualChance = baseChance + (eventSkillLevel * 10); // +10% per level
                
                if (!isEventActive && random.nextInt(100) < actualChance) {
                    triggerRandomEvent();
                }
                eventHandler.postDelayed(this, 60000); // Check every minute
            }
        };
        eventHandler.postDelayed(eventSpawnerRunnable, 60000);
    }
    
    private void triggerRandomEvent() {
        String[] eventTypes = {"stardust_rain", "cosmic_storm", "black_hole_surge", "time_warp"};
        currentEventType = eventTypes[random.nextInt(eventTypes.length)];
        isEventActive = true;
        eventEndTime = System.currentTimeMillis() + 30000; // 30 seconds
        
        switch (currentEventType) {
            case "stardust_rain":
                Toast.makeText(this, "🌟 STARDUST RAIN! Click power doubled for 30 seconds!", Toast.LENGTH_LONG).show();
                tvEventStatus.setText("🌟 STARDUST RAIN ACTIVE");
                break;
            case "cosmic_storm":
                Toast.makeText(this, "⚡ COSMIC STORM! All production tripled for 30 seconds!", Toast.LENGTH_LONG).show();
                tvEventStatus.setText("⚡ COSMIC STORM ACTIVE");
                break;
            case "black_hole_surge":
                Toast.makeText(this, "🌌 BLACK HOLE SURGE! Rift spawn rate increased!", Toast.LENGTH_LONG).show();
                tvEventStatus.setText("🌌 BLACK HOLE SURGE ACTIVE");
                spawnEventRifts();
                break;
            case "time_warp":
                Toast.makeText(this, "⏰ TIME WARP! Shooting Star cooldown reduced to 5 seconds!", Toast.LENGTH_LONG).show();
                tvEventStatus.setText("⏰ TIME WARP ACTIVE");
                break;
        }
        
        // Track event for statistics
        eventsSeen.put(currentEventType, eventsSeen.getOrDefault(currentEventType, 0) + 1);
        
        // Show event status
        tvEventStatus.setVisibility(View.VISIBLE);
        tvEventStatus.setAlpha(0f);
        tvEventStatus.animate().alpha(1f).setDuration(500).start();
        
        // End event after 30 seconds
        eventHandler.postDelayed(() -> {
            isEventActive = false;
            currentEventType = "";
            Toast.makeText(this, "Event ended!", Toast.LENGTH_SHORT).show();
            
            // Hide event status with animation
            tvEventStatus.animate().alpha(0f).setDuration(500).withEndAction(() -> {
                tvEventStatus.setVisibility(View.GONE);
            }).start();
        }, 30000);
    }
    
    private void spawnEventRifts() {
        for (int i = 0; i < 3; i++) {
            eventHandler.postDelayed(() -> spawnRift(), i * 2000); // Spawn 3 rifts over 6 seconds
        }
    }
    
    // Update click logic to handle events
    private BigInteger getEventModifiedClickValue(BigInteger baseValue) {
        if (isEventActive && "stardust_rain".equals(currentEventType)) {
            return baseValue.multiply(BigInteger.valueOf(2));
        }
        return baseValue;
    }
    
    // Update SPS calculation to handle events
    private long getEventModifiedSps(long baseSps) {
        if (isEventActive && "cosmic_storm".equals(currentEventType)) {
            return baseSps * 3;
        }
        return baseSps;
    }
}