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
    private TextView tvComboMultiplier, tvEventStatus, tvConstellationPoints; // NEW
    private FloatingActionButton btnClickStar, btnSettings, btnAchievements;
    private Button btnSupernova, btnShootingStar, btnSkillTree, btnBigCrunch; // MODIFIED: Added btnBigCrunch
    private Button btnBuy1, btnBuy10, btnBuy25; // NEW: Buy amount buttons
    private Button btnConstellations, btnChallenges, btnStatistics, btnAutoClicker; // NEW
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

    // --- NEW FEATURES ---
    // Auto-Clicker System
    private boolean autoClickerUnlocked = false;
    private int autoClickerLevel = 0;
    private long lastAutoClick = 0;
    private static final long AUTO_CLICK_INTERVAL = 100; // milliseconds
    
    // Constellation System
    private int constellationPoints = 0;
    private final List<Constellation> constellations = new ArrayList<>();
    private int activeConstellationId = -1;
    
    // Challenge System
    private final List<Challenge> challenges = new ArrayList<>();
    private Challenge activeChallenge = null;
    
    // Special Events
    private boolean isEventActive = false;
    private EventType currentEvent = null;
    private long eventEndTime = 0;
    
    // Statistics Tracking
    private long sessionStartTime = 0;
    private long totalPlayTime = 0;
    private int totalSupernovas = 0;
    private int totalBigCrunches = 0;
    private BigInteger highestStardust = BigInteger.ZERO;
    private long totalRiftsCollected = 0;
    
    // Combo System
    private int currentCombo = 0;
    private long lastClickTime = 0;
    private static final long COMBO_TIMEOUT = 2000; // 2 seconds to maintain combo

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

    // --- SharedPreferences Keys ---
    private static final String PREFS_NAME = "CosmicClickerPrefs";
    private static final String KEY_STARDUST = "stardustCount";
    private static final String KEY_SINGULARITY = "singularityCount";
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
    
    // --- NEW: Additional save keys ---
    private static final String KEY_CONSTELLATION_POINTS = "constellationPoints";
    private static final String KEY_ACTIVE_CONSTELLATION = "activeConstellation";
    private static final String KEY_CONSTELLATION_UNLOCKED_PREFIX = "constellation_unlocked_";
    private static final String KEY_CHALLENGE_COMPLETED_PREFIX = "challenge_completed_";
    private static final String KEY_CHALLENGE_PROGRESS_PREFIX = "challenge_progress_";
    private static final String KEY_AUTO_CLICKER_UNLOCKED = "autoClickerUnlocked";
    private static final String KEY_AUTO_CLICKER_LEVEL = "autoClickerLevel";
    private static final String KEY_TOTAL_PLAY_TIME = "totalPlayTime";
    private static final String KEY_TOTAL_SUPERNOVAS = "totalSupernovas";
    private static final String KEY_TOTAL_BIG_CRUNCHES = "totalBigCrunches";
    private static final String KEY_HIGHEST_STARDUST = "highestStardust";
    private static final String KEY_TOTAL_RIFTS = "totalRifts";

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
        setupConstellations();
        setupChallenges();

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadGame();

        setupClickListeners();
        setupTabs();

        sessionStartTime = System.currentTimeMillis();
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
        // Update total play time before saving
        totalPlayTime += System.currentTimeMillis() - sessionStartTime;
        saveGame();
        gameLoopHandler.removeCallbacksAndMessages(null);
        riftHandler.removeCallbacks(riftSpawnerRunnable);
        twinkleHandler.removeCallbacksAndMessages(null);
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
    }
    @Override
    protected void onResume() {
        super.onResume();
        sessionStartTime = System.currentTimeMillis(); // Reset session start time
        calculateOfflineProgress();
        checkDailyReward();
        startGameLoop();
        startRiftSpawner();
        startAutoSave();
        createTwinklingStars(15);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameLoopHandler.removeCallbacksAndMessages(null);
        riftHandler.removeCallbacks(riftSpawnerRunnable);
        twinkleHandler.removeCallbacksAndMessages(null);
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
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
        tvComboMultiplier = findViewById(R.id.tvComboMultiplier);
        tvEventStatus = findViewById(R.id.tvEventStatus);
        tvConstellationPoints = findViewById(R.id.tvConstellationPoints);
        btnClickStar = findViewById(R.id.btnClickStar);
        btnSettings = findViewById(R.id.btnSettings);
        btnAchievements = findViewById(R.id.btnAchievements);
        btnSkillTree = findViewById(R.id.btnSkillTree);
        btnSupernova = findViewById(R.id.btnSupernova);
        btnShootingStar = findViewById(R.id.btnShootingStar);
        btnBigCrunch = findViewById(R.id.btnBigCrunch); // NEW
        llUpgradesContainer = findViewById(R.id.llUpgradesContainer);
        tabLayout = findViewById(R.id.tabLayout);

        // --- NEW: Find the buy amount toggle buttons ---
        btnBuy1 = findViewById(R.id.btnBuy1);
        btnBuy10 = findViewById(R.id.btnBuy10);
        btnBuy25 = findViewById(R.id.btnBuy25);

        // --- NEW: Find the new buttons ---
        btnConstellations = findViewById(R.id.btnConstellations);
        btnChallenges = findViewById(R.id.btnChallenges);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnAutoClicker = findViewById(R.id.btnAutoClicker);
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
        
        // --- NEW: Listeners for new feature buttons ---
        if (btnConstellations != null) {
            btnConstellations.setOnClickListener(v -> showConstellationsDialog());
        }
        if (btnChallenges != null) {
            btnChallenges.setOnClickListener(v -> showChallengesDialog());
        }
        if (btnStatistics != null) {
            btnStatistics.setOnClickListener(v -> showStatisticsDialog());
        }
        if (btnAutoClicker != null) {
            btnAutoClicker.setOnClickListener(v -> showAutoClickerDialog());
        }
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
    private void setupConstellations() {
        constellations.clear();
        constellations.add(new Constellation(0, "Orion's Belt", "Click value increased by 50%", 
            ConstellationType.CLICK_MULTIPLIER, 1.5, 5));
        constellations.add(new Constellation(1, "Andromeda's Grace", "SPS increased by 25%", 
            ConstellationType.SPS_MULTIPLIER, 1.25, 10));
        constellations.add(new Constellation(2, "Cassiopeia's Wisdom", "Research costs reduced by 20%", 
            ConstellationType.RESEARCH_SPEED, 0.8, 15));
        constellations.add(new Constellation(3, "Draco's Fortune", "Upgrade costs reduced by 15%", 
            ConstellationType.UPGRADE_DISCOUNT, 0.85, 20));
        constellations.add(new Constellation(4, "Phoenix Rising", "Singularity gains increased by 30%", 
            ConstellationType.SINGULARITY_BONUS, 1.3, 30));
        constellations.add(new Constellation(5, "Pegasus Swift", "Auto-clicker speed doubled", 
            ConstellationType.AUTO_CLICK_SPEED, 2.0, 25));
        constellations.add(new Constellation(6, "Ursa Major", "Rift spawn rate increased by 100%", 
            ConstellationType.RIFT_FREQUENCY, 2.0, 35));
        constellations.add(new Constellation(7, "Lyra's Rhythm", "Combo timeout increased by 50%", 
            ConstellationType.COMBO_DURATION, 1.5, 40));
    }
    private void setupChallenges() {
        challenges.clear();
        // Beginner challenges
        challenges.add(new Challenge("ch_stardust_1", "Stardust Collector I", 
            "Earn 1 Million stardust in a single run", ChallengeType.EARN_STARDUST, 
            new BigInteger("1000000"), 1, 1));
        challenges.add(new Challenge("ch_clicks_1", "Click Master I", 
            "Click 1,000 times", ChallengeType.CLICK_COUNT, 
            new BigInteger("1000"), 2, 0));
        
        // Intermediate challenges
        challenges.add(new Challenge("ch_sps_1", "Stellar Engine", 
            "Reach 10,000 SPS", ChallengeType.REACH_SPS, 
            new BigInteger("10000"), 5, 2));
        challenges.add(new Challenge("ch_combo_1", "Combo King", 
            "Maintain a 100x combo", ChallengeType.COMBO_CHAIN, 
            new BigInteger("100"), 10, 3));
        
        // Advanced challenges
        challenges.add(new Challenge("ch_no_click", "Idle Master", 
            "Earn 1 Billion stardust without clicking", ChallengeType.NO_CLICK_CHALLENGE, 
            new BigInteger("1000000000"), 20, 5));
        challenges.add(new Challenge("ch_speed_1", "Speed Demon", 
            "Earn 100 Million stardust in 5 minutes", ChallengeType.SPEED_RUN, 
            new BigInteger("100000000"), 15, 4));
        
        // Expert challenges
        challenges.add(new Challenge("ch_rift_master", "Rift Master", 
            "Collect 50 rifts in one run", ChallengeType.RIFT_COLLECTOR, 
            new BigInteger("50"), 30, 10));
        challenges.add(new Challenge("ch_no_upgrade", "Minimalist", 
            "Earn 10 Million stardust without buying upgrades", ChallengeType.NO_UPGRADES_STARDUST, 
            new BigInteger("10000000"), 25, 8));
    }

    // --- Core Game Logic ---
    private void onStarClicked() {
        animateClick(btnClickStar);
        
        // Check and update combo
        long currentTime = System.currentTimeMillis();
        long comboTimeout = COMBO_TIMEOUT;
        
        // Apply constellation bonus to combo timeout
        for (Constellation c : constellations) {
            if (c.isUnlocked() && c.getType() == ConstellationType.COMBO_DURATION) {
                comboTimeout = (long)(comboTimeout * c.getBonusValue());
            }
        }
        
        if (currentEvent == EventType.COMBO_FRENZY) {
            comboTimeout *= 2;
        }
        
        if (currentTime - lastClickTime < comboTimeout) {
            currentCombo++;
            if (currentCombo > 999) currentCombo = 999; // Cap at 999
        } else {
            currentCombo = 1;
        }
        lastClickTime = currentTime;
        
        // Calculate click value with combo multiplier
        BigInteger clickValue = BigInteger.ONE;
        clickValue = clickValue.add(BigInteger.valueOf(getSkillLevel("click_1")));
        
        // Apply combo multiplier (1 + combo/100)
        double comboMultiplier = 1.0 + (currentCombo / 100.0);
        clickValue = clickValue.multiply(BigInteger.valueOf((long)(comboMultiplier * 100))).divide(BigInteger.valueOf(100));
        
        // Apply constellation click multiplier
        for (Constellation c : constellations) {
            if (c.isUnlocked() && c.getType() == ConstellationType.CLICK_MULTIPLIER) {
                clickValue = clickValue.multiply(BigInteger.valueOf((long)(c.getBonusValue() * 100))).divide(BigInteger.valueOf(100));
            }
        }
        
        // Apply event multiplier
        if (currentEvent == EventType.TRIPLE_CLICKS) {
            clickValue = clickValue.multiply(BigInteger.valueOf(3));
        }
        
        // Critical click chance
        int critChance = getSkillLevel("crit_1");
        if (random.nextInt(100) < critChance) {
            clickValue = clickValue.multiply(BigInteger.valueOf(1000));
            showCriticalClickEffect();
        }
        
        if (getResearch("research_click_sps").isPurchased()) {
            clickValue = clickValue.add(BigInteger.valueOf(totalSps / 100));
        }
        
        showStardustPopup(clickValue, btnClickStar);
        stardustCount = stardustCount.add(clickValue);
        totalClicks++;
        totalStardustEverEarned = totalStardustEverEarned.add(clickValue);
        
        // Update challenge progress
        updateChallengeProgress(ChallengeType.CLICK_COUNT, BigInteger.ONE);
        
        playClickSound();
        updateUI();
        checkAchievements();
    }
    
    private void showCriticalClickEffect() {
        // Create a visual effect for critical clicks
        TextView critText = new TextView(this);
        critText.setText("CRITICAL!");
        critText.setTextColor(getResources().getColor(R.color.accent_gold));
        critText.setTextSize(24);
        critText.setTypeface(null, android.graphics.Typeface.BOLD);
        
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        
        rootLayout.addView(critText, params);
        
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(critText, "alpha", 1f, 0f);
        ObjectAnimator moveUp = ObjectAnimator.ofFloat(critText, "translationY", 0f, -200f);
        
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(fadeOut, moveUp);
        animSet.setDuration(1000);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rootLayout.removeView(critText);
            }
        });
        animSet.start();
    }
    
    private void updateChallengeProgress(ChallengeType type, BigInteger amount) {
        if (activeChallenge != null && activeChallenge.getType() == type) {
            activeChallenge.addProgress(amount);
            if (activeChallenge.checkCompletion()) {
                completeChallenge(activeChallenge);
            }
        }
    }
    
    private void completeChallenge(Challenge challenge) {
        singularityCount += challenge.getRewardSingularities();
        constellationPoints += challenge.getRewardConstellationPoints();
        
        Toast.makeText(this, "Challenge Complete: " + challenge.getName() + 
            "\nRewards: " + challenge.getRewardSingularities() + " Singularities, " + 
            challenge.getRewardConstellationPoints() + " Constellation Points", 
            Toast.LENGTH_LONG).show();
        
        playAchievementSound();
        activeChallenge = null;
        updateUI();
    }

    // --- Save and Load ---
    private void saveGame() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_STARDUST, stardustCount.toString());
        editor.putInt(KEY_SINGULARITY, singularityCount);
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
        editor.putLong(KEY_LAST_DAILY_REWARD, prefs.getLong(KEY_LAST_DAILY_REWARD, 0));
        editor.putInt(KEY_CONSTELLATION_POINTS, constellationPoints);
        editor.putInt(KEY_ACTIVE_CONSTELLATION, activeConstellationId);
        for (Constellation constellation : constellations) {
            editor.putBoolean(KEY_CONSTELLATION_UNLOCKED_PREFIX + constellation.getId(), constellation.isUnlocked());
        }
        for (Challenge challenge : challenges) {
            editor.putBoolean(KEY_CHALLENGE_COMPLETED_PREFIX + challenge.getId(), challenge.isCompleted());
            editor.putString(KEY_CHALLENGE_PROGRESS_PREFIX + challenge.getId(), challenge.getCurrentProgress().toString());
        }
        editor.putBoolean(KEY_AUTO_CLICKER_UNLOCKED, autoClickerUnlocked);
        editor.putInt(KEY_AUTO_CLICKER_LEVEL, autoClickerLevel);
        editor.putLong(KEY_TOTAL_PLAY_TIME, totalPlayTime);
        editor.putInt(KEY_TOTAL_SUPERNOVAS, totalSupernovas);
        editor.putInt(KEY_TOTAL_BIG_CRUNCHES, totalBigCrunches);
        editor.putString(KEY_HIGHEST_STARDUST, highestStardust.toString());
        editor.putLong(KEY_TOTAL_RIFTS, totalRiftsCollected);
        editor.apply();
    }

    private void loadGame() {
        stardustCount = new BigInteger(prefs.getString(KEY_STARDUST, "0"));
        singularityCount = prefs.getInt(KEY_SINGULARITY, 0);
        cosmicEssence = prefs.getInt(KEY_COSMIC_ESSENCE, 0); // NEW
        for (int i = 0; i < upgrades.size(); i++) upgrades.get(i).count = prefs.getInt(KEY_UPGRADE_COUNT_PREFIX + i, 0);
        for (ResearchUpgrade research : researchUpgrades) research.setPurchased(prefs.getBoolean(KEY_RESEARCH_PREFIX + research.getId(), false));
        for (Achievement achievement : achievements) achievement.setUnlocked(prefs.getBoolean(KEY_ACHIEVEMENT_PREFIX + achievement.getId(), false));
        for (Skill skill : skills) {
            skill.setCurrentLevel(prefs.getInt(KEY_SKILL_LEVEL_PREFIX + skill.getId(), 0));
        }
        totalClicks = prefs.getLong(KEY_TOTAL_CLICKS, 0);
        totalStardustEverEarned = new BigInteger(prefs.getString(KEY_TOTAL_STARDUST_EARNED, "0"));
        
        // --- NEW: Load new features ---
        constellationPoints = prefs.getInt(KEY_CONSTELLATION_POINTS, 0);
        activeConstellationId = prefs.getInt(KEY_ACTIVE_CONSTELLATION, -1);
        for (Constellation constellation : constellations) {
            constellation.setUnlocked(prefs.getBoolean(KEY_CONSTELLATION_UNLOCKED_PREFIX + constellation.getId(), false));
        }
        for (Challenge challenge : challenges) {
            challenge.setCompleted(prefs.getBoolean(KEY_CHALLENGE_COMPLETED_PREFIX + challenge.getId(), false));
            challenge.setCurrentProgress(new BigInteger(prefs.getString(KEY_CHALLENGE_PROGRESS_PREFIX + challenge.getId(), "0")));
        }
        autoClickerUnlocked = prefs.getBoolean(KEY_AUTO_CLICKER_UNLOCKED, false);
        autoClickerLevel = prefs.getInt(KEY_AUTO_CLICKER_LEVEL, 0);
        totalPlayTime = prefs.getLong(KEY_TOTAL_PLAY_TIME, 0);
        totalSupernovas = prefs.getInt(KEY_TOTAL_SUPERNOVAS, 0);
        totalBigCrunches = prefs.getInt(KEY_TOTAL_BIG_CRUNCHES, 0);
        highestStardust = new BigInteger(prefs.getString(KEY_HIGHEST_STARDUST, "0"));
        totalRiftsCollected = prefs.getLong(KEY_TOTAL_RIFTS, 0);
        
        recalculateSps();
        updateUI();
        if (tabLayout.getSelectedTabPosition() == 0) refreshCelestialObjectsUI(); else refreshResearchUI();
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
        
        // --- NEW: Update combo display ---
        if (tvComboMultiplier != null) {
            if (currentCombo > 1) {
                tvComboMultiplier.setVisibility(View.VISIBLE);
                tvComboMultiplier.setText(String.format(Locale.getDefault(), "Combo x%d", currentCombo));
            } else {
                tvComboMultiplier.setVisibility(View.GONE);
            }
        }
        
        // --- NEW: Update event status ---
        if (tvEventStatus != null) {
            if (isEventActive && currentEvent != null) {
                tvEventStatus.setVisibility(View.VISIBLE);
                long remainingTime = (eventEndTime - System.currentTimeMillis()) / 1000;
                tvEventStatus.setText(String.format(Locale.getDefault(), "EVENT: %s (%ds)", 
                    currentEvent.getName(), remainingTime));
            } else {
                tvEventStatus.setVisibility(View.GONE);
            }
        }
        
        // --- NEW: Update constellation points ---
        if (tvConstellationPoints != null) {
            tvConstellationPoints.setText(String.format(Locale.getDefault(), 
                "Constellation Points: %d", constellationPoints));
        }

        btnSupernova.setVisibility(stardustCount.compareTo(supernovaCost) >= 0 ? View.VISIBLE : View.GONE);
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
                // Regular SPS earning
                BigInteger spsToAdd = BigInteger.valueOf(totalSps);
                if (currentEvent == EventType.DOUBLE_STARDUST) {
                    spsToAdd = spsToAdd.multiply(BigInteger.valueOf(2));
                }
                stardustCount = stardustCount.add(spsToAdd);
                totalStardustEverEarned = totalStardustEverEarned.add(spsToAdd);
                
                // Auto-clicker logic
                if (autoClickerUnlocked && autoClickerLevel > 0) {
                    long currentTime = System.currentTimeMillis();
                    long autoClickInterval = AUTO_CLICK_INTERVAL;
                    
                    // Apply constellation bonus
                    for (Constellation c : constellations) {
                        if (c.isUnlocked() && c.getType() == ConstellationType.AUTO_CLICK_SPEED) {
                            autoClickInterval = (long)(autoClickInterval / c.getBonusValue());
                        }
                    }
                    
                    if (currentTime - lastAutoClick >= autoClickInterval) {
                        BigInteger autoClickValue = BigInteger.valueOf(autoClickerLevel);
                        stardustCount = stardustCount.add(autoClickValue);
                        totalStardustEverEarned = totalStardustEverEarned.add(autoClickValue);
                        lastAutoClick = currentTime;
                        
                        // Update no-click challenge progress
                        updateChallengeProgress(ChallengeType.NO_CLICK_CHALLENGE, autoClickValue);
                    }
                }
                
                // Update highest stardust record
                if (stardustCount.compareTo(highestStardust) > 0) {
                    highestStardust = stardustCount;
                }
                
                // Update challenge progress
                updateChallengeProgress(ChallengeType.EARN_STARDUST, spsToAdd);
                updateChallengeProgress(ChallengeType.REACH_SPS, BigInteger.valueOf(totalSps));
                
                // Check for random events
                checkForRandomEvent();
                
                updateUI();
                checkAchievements();
                gameLoopHandler.postDelayed(this, 1000);
            }
        };
        gameLoopHandler.post(gameRunnable);
    }
    
    private void checkForRandomEvent() {
        if (!isEventActive && random.nextInt(300) == 0) { // 1/300 chance per second
            startRandomEvent();
        }
        
        if (isEventActive && System.currentTimeMillis() > eventEndTime) {
            endCurrentEvent();
        }
    }
    
    private void startRandomEvent() {
        EventType[] events = EventType.values();
        currentEvent = events[random.nextInt(events.length)];
        isEventActive = true;
        eventEndTime = System.currentTimeMillis() + 60000; // 1 minute duration
        
        Toast.makeText(this, "EVENT: " + currentEvent.getName() + "\n" + 
            currentEvent.getDescription(), Toast.LENGTH_LONG).show();
        
        // Recalculate if needed
        if (currentEvent == EventType.DOUBLE_STARDUST) {
            recalculateSps();
        }
        
        updateUI();
    }
    
    private void endCurrentEvent() {
        isEventActive = false;
        Toast.makeText(this, "Event ended: " + currentEvent.getName(), Toast.LENGTH_SHORT).show();
        currentEvent = null;
        
        recalculateSps();
        updateUI();
    }

    private void startRiftSpawner() {
        riftSpawnerRunnable = new Runnable() {
            @Override
            public void run() {
                spawnRift();
                int baseDelay = 20000 + random.nextInt(25000);
                
                // Apply constellation bonus
                for (Constellation c : constellations) {
                    if (c.isUnlocked() && c.getType() == ConstellationType.RIFT_FREQUENCY) {
                        baseDelay = (int)(baseDelay / c.getBonusValue());
                    }
                }
                
                // Apply event bonus
                if (currentEvent == EventType.RIFT_RUSH) {
                    baseDelay = baseDelay / 5;
                }
                
                riftHandler.postDelayed(this, baseDelay);
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
            
            // Apply research bonus
            if (getResearch("research_rift_doubler").isPurchased()) {
                reward = reward.multiply(BigInteger.valueOf(2));
            }
            
            stardustCount = stardustCount.add(reward);
            totalStardustEverEarned = totalStardustEverEarned.add(reward);
            totalRiftsCollected++;
            
            Toast.makeText(this, "Rift Tapped! +" + formatBigNumber(reward) + " Stardust!", Toast.LENGTH_SHORT).show();
            playRiftSound();
            explodeParticles(rift.getX() + riftSize / 2f, rift.getY() + riftSize / 2f);
            rootLayout.removeView(rift);
            
            // Update challenge progress
            updateChallengeProgress(ChallengeType.RIFT_COLLECTOR, BigInteger.ONE);
            
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
        SwitchMaterial switchSound = dialogView.findViewById(R.id.switchSound);
        switchSound.setChecked(soundEnabled);
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> soundEnabled = isChecked);
        builder.setView(dialogView)
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
            // Now, actually increment the counts
            for (int i = 0; i < amount; i++) {
                if (upgrade.count % Upgrade.MILESTONE_INTERVAL == 0 && i > 0) { // Check for milestones within the bulk purchase
                    playMilestoneSound();
                }
                upgrade.count++;
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
        
        // Apply constellation discount
        for (Constellation c : constellations) {
            if (c.isUnlocked() && c.getType() == ConstellationType.RESEARCH_SPEED) {
                cost = cost.multiply(BigInteger.valueOf((long)(c.getBonusValue() * 100))).divide(BigInteger.valueOf(100));
            }
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

        for (Upgrade upgrade : upgrades) {
            // Pass the skill level as the second argument
            totalSpsDecimal = totalSpsDecimal.add(upgrade.getTotalCpsForThisUpgrade(harmonicResonance, celestialArchitectureLevel));
        }

        if (getResearch("research_sps_boost").isPurchased()) {
            totalSpsDecimal = totalSpsDecimal.multiply(new BigDecimal("1.1"));
        }

        int spsSkillLevel = getSkillLevel("sps_1");
        BigDecimal spsSkillBonus = BigDecimal.valueOf(spsSkillLevel * 0.02);
        totalSpsDecimal = totalSpsDecimal.multiply(BigDecimal.ONE.add(spsSkillBonus));
        
        // Apply constellation SPS multiplier
        for (Constellation c : constellations) {
            if (c.isUnlocked() && c.getType() == ConstellationType.SPS_MULTIPLIER) {
                totalSpsDecimal = totalSpsDecimal.multiply(BigDecimal.valueOf(c.getBonusValue()));
            }
        }
        
        // Apply event bonus
        if (currentEvent == EventType.DOUBLE_STARDUST) {
            totalSpsDecimal = totalSpsDecimal.multiply(BigDecimal.valueOf(2));
        }

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
            
            // Apply constellation bonus
            for (Constellation c : constellations) {
                if (c.isUnlocked() && c.getType() == ConstellationType.SINGULARITY_BONUS) {
                    earned = earned * c.getBonusValue();
                }
            }
            
            // Apply event bonus
            if (currentEvent == EventType.SINGULARITY_SURGE) {
                earned = earned * 1.5;
            }
            
            int earnedSingularities = (int) earned;

            singularityCount += earnedSingularities;
            totalSupernovas++;
            Toast.makeText(this, "SUPERNOVA! You earned " + earnedSingularities + " Singularities!", Toast.LENGTH_LONG).show();

            // Reset everything EXCEPT singularities, essence, skills, and constellation points
            stardustCount = BigInteger.ZERO;
            totalClicks = 0;
            currentCombo = 0;
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
                        singularityCount = 0; // Reset singularities
                        totalBigCrunches++;

                        Toast.makeText(this, "BIG CRUNCH! You have earned 1 Cosmic Essence!", Toast.LENGTH_LONG).show();
                        playMilestoneSound();

                        // Perform a full reset, even harder than a supernova
                        fullReset();
                    })
                    .show();
        } else {
            Toast.makeText(this, "You need " + BIG_CRUNCH_COST + " Singularities to Big Crunch!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fullReset() {
        stardustCount = BigInteger.ZERO;
        totalClicks = 0;
        totalStardustEverEarned = BigInteger.ZERO;
        currentCombo = 0;

        for (Upgrade upgrade : upgrades) upgrade.count = 0;
        for (ResearchUpgrade research : researchUpgrades) research.setPurchased(false);
        for (Skill skill : skills) skill.setCurrentLevel(0);
        // Note: We DO NOT reset achievements, cosmicEssence, or constellation points

        saveGame(); // Save the new essence/singularity count immediately
        loadGame(); // Reload to apply all new states and update UI
    }

    // --- NEW: Constellation Dialog ---
    private void showConstellationsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_constellations, null);
        LinearLayout container = dialogView.findViewById(R.id.llConstellationsContainer);
        TextView tvPoints = dialogView.findViewById(R.id.tvConstellationPoints);
        
        tvPoints.setText(String.format(Locale.getDefault(), "Constellation Points: %d", constellationPoints));
        container.removeAllViews();
        
        for (Constellation constellation : constellations) {
            View itemView = getLayoutInflater().inflate(R.layout.constellation_item, container, false);
            TextView name = itemView.findViewById(R.id.tvConstellationName);
            TextView desc = itemView.findViewById(R.id.tvConstellationDesc);
            Button activateBtn = itemView.findViewById(R.id.btnActivateConstellation);
            
            name.setText(constellation.getName());
            desc.setText(constellation.getDescription() + "\nCost: " + constellation.getRequiredPoints() + " points");
            
            if (constellation.isUnlocked()) {
                activateBtn.setText(activeConstellationId == constellation.getId() ? "ACTIVE" : "ACTIVATE");
                activateBtn.setEnabled(activeConstellationId != constellation.getId());
                activateBtn.setOnClickListener(v -> {
                    activeConstellationId = constellation.getId();
                    recalculateSps();
                    updateUI();
                    showConstellationsDialog(); // Refresh
                });
            } else if (constellationPoints >= constellation.getRequiredPoints()) {
                activateBtn.setText("UNLOCK (" + constellation.getRequiredPoints() + ")");
                activateBtn.setOnClickListener(v -> {
                    constellationPoints -= constellation.getRequiredPoints();
                    constellation.setUnlocked(true);
                    Toast.makeText(this, "Unlocked: " + constellation.getName(), Toast.LENGTH_SHORT).show();
                    showConstellationsDialog(); // Refresh
                });
            } else {
                activateBtn.setText("LOCKED");
                activateBtn.setEnabled(false);
                itemView.setAlpha(0.5f);
            }
            
            container.addView(itemView);
        }
        
        builder.setView(dialogView)
            .setPositiveButton("Close", (d, w) -> d.dismiss())
            .show();
    }
    
    // --- NEW: Challenges Dialog ---
    private void showChallengesDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_challenges, null);
        LinearLayout container = dialogView.findViewById(R.id.llChallengesContainer);
        
        container.removeAllViews();
        
        for (Challenge challenge : challenges) {
            View itemView = getLayoutInflater().inflate(R.layout.challenge_item, container, false);
            TextView name = itemView.findViewById(R.id.tvChallengeName);
            TextView desc = itemView.findViewById(R.id.tvChallengeDesc);
            TextView progress = itemView.findViewById(R.id.tvChallengeProgress);
            Button actionBtn = itemView.findViewById(R.id.btnChallengeAction);
            
            name.setText(challenge.getName());
            desc.setText(challenge.getDescription());
            progress.setText("Progress: " + formatBigNumber(challenge.getCurrentProgress()) + 
                " / " + formatBigNumber(challenge.getTargetValue()));
            
            if (challenge.isCompleted()) {
                actionBtn.setText("COMPLETED");
                actionBtn.setEnabled(false);
                itemView.setAlpha(0.7f);
            } else if (challenge.isActive()) {
                actionBtn.setText("ACTIVE");
                actionBtn.setEnabled(false);
            } else if (activeChallenge == null) {
                actionBtn.setText("START");
                actionBtn.setOnClickListener(v -> {
                    activeChallenge = challenge;
                    challenge.setActive(true);
                    challenge.setCurrentProgress(BigInteger.ZERO);
                    Toast.makeText(this, "Challenge Started: " + challenge.getName(), Toast.LENGTH_SHORT).show();
                    showChallengesDialog(); // Refresh
                });
            } else {
                actionBtn.setText("BUSY");
                actionBtn.setEnabled(false);
            }
            
            container.addView(itemView);
        }
        
        builder.setView(dialogView)
            .setPositiveButton("Close", (d, w) -> d.dismiss())
            .show();
    }
    
    // --- NEW: Statistics Dialog ---
    private void showStatisticsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_statistics, null);
        TextView tvStats = dialogView.findViewById(R.id.tvStatistics);
        
        long currentPlayTime = totalPlayTime + (System.currentTimeMillis() - sessionStartTime);
        long hours = currentPlayTime / 3600000;
        long minutes = (currentPlayTime % 3600000) / 60000;
        
        String stats = String.format(Locale.getDefault(),
            "=== Lifetime Statistics ===\n\n" +
            "Total Play Time: %dh %dm\n" +
            "Total Clicks: %,d\n" +
            "Total Stardust Earned: %s\n" +
            "Highest Stardust: %s\n" +
            "Current Combo Record: %d\n\n" +
            "=== Prestige Stats ===\n" +
            "Total Supernovas: %d\n" +
            "Total Big Crunches: %d\n" +
            "Cosmic Essence: %d\n" +
            "Constellation Points: %d\n\n" +
            "=== Collection Stats ===\n" +
            "Rifts Collected: %,d\n" +
            "Achievements Unlocked: %d/%d\n" +
            "Challenges Completed: %d/%d\n" +
            "Skills Unlocked: %d",
            hours, minutes,
            totalClicks,
            formatBigNumber(totalStardustEverEarned),
            formatBigNumber(highestStardust),
            currentCombo,
            totalSupernovas,
            totalBigCrunches,
            cosmicEssence,
            constellationPoints,
            totalRiftsCollected,
            getUnlockedAchievementsCount(), achievements.size(),
            getCompletedChallengesCount(), challenges.size(),
            getUnlockedSkillsCount()
        );
        
        tvStats.setText(stats);
        
        builder.setView(dialogView)
            .setPositiveButton("Close", (d, w) -> d.dismiss())
            .show();
    }
    
    // --- NEW: Auto-Clicker Dialog ---
    private void showAutoClickerDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        
        if (!autoClickerUnlocked) {
            builder.setTitle("Auto-Clicker Locked")
                .setMessage("Unlock the Auto-Clicker for 100 Singularities?\n\n" +
                    "The Auto-Clicker will automatically generate stardust even when not clicking!")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Unlock (100)", (d, w) -> {
                    if (singularityCount >= 100) {
                        singularityCount -= 100;
                        autoClickerUnlocked = true;
                        autoClickerLevel = 1;
                        Toast.makeText(this, "Auto-Clicker Unlocked!", Toast.LENGTH_LONG).show();
                        updateUI();
                        showAutoClickerDialog(); // Show upgrade dialog
                    } else {
                        Toast.makeText(this, "Not enough Singularities!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
        } else {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_auto_clicker, null);
            TextView tvLevel = dialogView.findViewById(R.id.tvAutoClickerLevel);
            TextView tvRate = dialogView.findViewById(R.id.tvAutoClickerRate);
            Button btnUpgrade = dialogView.findViewById(R.id.btnUpgradeAutoClicker);
            
            tvLevel.setText("Level: " + autoClickerLevel);
            tvRate.setText("Rate: " + autoClickerLevel + " stardust per " + AUTO_CLICK_INTERVAL + "ms");
            
            int upgradeCost = autoClickerLevel * 50;
            btnUpgrade.setText("Upgrade (" + upgradeCost + " Singularities)");
            btnUpgrade.setEnabled(singularityCount >= upgradeCost);
            btnUpgrade.setOnClickListener(v -> {
                if (singularityCount >= upgradeCost) {
                    singularityCount -= upgradeCost;
                    autoClickerLevel++;
                    Toast.makeText(this, "Auto-Clicker upgraded to level " + autoClickerLevel + "!", Toast.LENGTH_SHORT).show();
                    updateUI();
                    showAutoClickerDialog(); // Refresh
                }
            });
            
            builder.setView(dialogView)
                .setPositiveButton("Close", (d, w) -> d.dismiss())
                .show();
        }
    }
    
    // Helper methods for statistics
    private int getUnlockedAchievementsCount() {
        int count = 0;
        for (Achievement a : achievements) {
            if (a.isUnlocked()) count++;
        }
        return count;
    }
    
    private int getCompletedChallengesCount() {
        int count = 0;
        for (Challenge c : challenges) {
            if (c.isCompleted()) count++;
        }
        return count;
    }
    
    private int getUnlockedSkillsCount() {
        int count = 0;
        for (Skill s : skills) {
            if (s.getCurrentLevel() > 0) count++;
        }
        return count;
    }
}