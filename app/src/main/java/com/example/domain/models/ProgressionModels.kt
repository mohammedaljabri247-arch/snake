package com.example.domain.models

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class SnakeSkin(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val descEn: String,
    val descAr: String,
    val cost: Int,
    val headColor: Color,
    val bodyPrimary: Color,
    val bodySecondary: Color,
    val trailColor: Color
) {
    NEON_GREEN(
        "neon_green",
        "Neon Viper",
        "أفعى النيون الخضراء",
        "Classic high-visibility cyber green.",
        "التصميم الكلاسيكي المتوهج باللون الأخضر السيبراني.",
        0,
        NeonGreen, Color(0xFF00D670), ElectricCyan, NeonGreen
    ),
    ELECTRIC_BLUE(
        "electric_blue",
        "Electric Pulse",
        "النبض الكهربائي الأزرق",
        "Charged with high-voltage cyan currents.",
        "مشحون بتيارات السيان الكهربائية فائقة التوهج.",
        0,
        ElectricCyan, Color(0xFF0088FF), NeonGreen, ElectricCyan
    ),
    PLASMA_PURPLE(
        "plasma_purple",
        "Plasma Phantom",
        "شبح البلازما البنفسجي",
        "Synthesized in deep-space plasma reactors.",
        "مصمم من طاقة مفاعلات البلازما الفضائية.",
        150,
        PlasmaPurple, NeonMagenta, ElectricCyan, PlasmaPurple
    ),
    FIRE_SNAKE(
        "fire_snake",
        "Solar Flare",
        "الحنش الناري المتوهج",
        "Blazing thermal core with molten ember trail.",
        "نواة حرارية ملتهبة تترك أثرًا ناريًا متوهجًا.",
        250,
        CyberOrange, DangerRed, GoldenEnergy, CyberOrange
    ),
    GOLDEN_SNAKE(
        "golden_snake",
        "Sovereign Gold",
        "الحنش الذهبي الملكي",
        "Pure auric circuitry for high-roller champions.",
        "دوائر ذهبية فاخرة مخصصة لأبطال النخبة.",
        400,
        GoldenEnergy, Color(0xFFFFAA00), Color(0xFFFFF5CC), GoldenEnergy
    ),
    CYBER_SNAKE(
        "cyber_snake",
        "Cyberpunk 2099",
        "سايبر بانك 2099",
        "Dual-tone magenta and cyan synthwave armor.",
        "درع مزدوج يجمع بين الماجنتا والسيان بأسلوب مستقبلي.",
        500,
        NeonMagenta, ElectricCyan, PlasmaPurple, NeonMagenta
    ),
    SHADOW_SNAKE(
        "shadow_snake",
        "Stealth Wraith",
        "طيف الظل الخفي",
        "Dark-matter chassis with crimson edge optics.",
        "هيكل من المادة المظلمة مع حواف ليزر قرمزية.",
        650,
        DangerRed, Color(0xFF2B2D42), DangerRed, DangerRed
    ),
    CRYSTAL_SNAKE(
        "crystal_snake",
        "Prism Quartz",
        "حنش الكريستال الماسي",
        "Refracts ambient grid light into diamond beams.",
        "يعكس أضواء الشبكة إلى إشعاعات ماسية نقية.",
        800,
        Color(0xFFE0FFFF), ElectricCyan, Color(0xFFB388FF), Color(0xFFE0FFFF)
    ),
    TOXIC_SNAKE(
        "toxic_snake",
        "Bio-Hazard",
        "الحنش الإشعاعي السام",
        "Radioactive venom core with acid-green pulse.",
        "نواة مشعة تنبض بالطاقة الحمضية الخضراء.",
        950,
        ToxicGreen, Color(0xFF38B000), GoldenEnergy, ToxicGreen
    ),
    LEGENDARY_SNAKE(
        "legendary_snake",
        "Omega Celestial",
        "الحنش الأسطوري المطلق",
        "Transcendent polychromatic apex cyber-serpent.",
        "القمة المطلقة للتطور مع طيف لوني متكامل.",
        1500,
        GoldenEnergy, NeonMagenta, ElectricCyan, NeonGreen
    );

    companion object {
        fun fromId(id: String): SnakeSkin = entries.find { it.id == id } ?: NEON_GREEN
    }
}

enum class SkillType(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val descEn: String,
    val descAr: String,
    val iconSymbol: String,
    val baseCost: Int,
    val maxLevel: Int,
    val accentColor: Color
) {
    ENERGY_BOOST(
        "energy_boost",
        "Energy Boost",
        "تعزيز نقاط الطاقة",
        "+10% base points from all Energy Orbs per level.",
        "زيادة النقاط الأساسية من جميع كرات الطاقة بنسبة 10% لكل مستوى.",
        "⚡", 80, 5, NeonGreen
    ),
    MAGNET_RANGE(
        "magnet_range",
        "Magnet Range",
        "مدى المغناطيس",
        "Increases Energy Magnet pull radius and duration.",
        "زيادة مدى جذب كرات الطاقة ومدة قدرة المغناطيس.",
        "🧲", 100, 5, PlasmaPurple
    ),
    SHIELD_DURATION(
        "shield_duration",
        "Shield Duration",
        "مدة درع الحماية",
        "+25% longer Holo-Shield & Ghost Phase duration per level.",
        "زيادة مدة درع الحماية وطور الشبح بنسبة 25% لكل مستوى.",
        "🛡️", 110, 5, ElectricCyan
    ),
    COIN_BONUS(
        "coin_bonus",
        "Coin Extractor",
        "مضاعف العملات",
        "+15% extra Energy Coins earned every match per level.",
        "زيادة العملات المكتسبة في كل جولة بنسبة 15% لكل مستوى.",
        "🪙", 120, 5, GoldenEnergy
    ),
    EVOLUTION_BONUS(
        "evolution_bonus",
        "Evolution Catalyst",
        "مسرع التطور",
        "+15% faster Evolution XP gain from every orb collected.",
        "تسريع نقاط خبرة التطور بنسبة 15% عند جمع أي طاقة.",
        "🧬", 130, 5, NeonMagenta
    ),
    SCORE_MULTIPLIER(
        "score_multiplier",
        "Combo Overclock",
        "مضاعف النتيجة القصوى",
        "Extends Combo window & boosts total score by +10% per level.",
        "إطالة وقت الكومبو وزيادة النتيجة النهائية بنسبة 10% لكل مستوى.",
        "🔥", 150, 5, CyberOrange
    );

    fun costForNextLevel(currentLevel: Int): Int {
        return baseCost * (currentLevel + 1)
    }
}

enum class MissionGoalType {
    COLLECT_ORBS,
    SCORE_POINTS,
    COLLECT_POWERUPS,
    REACH_EVOLUTION,
    SURVIVE_SECONDS,
    REACH_COMBO
}

data class Mission(
    val id: Int,
    val titleEn: String,
    val titleAr: String,
    val descEn: String,
    val descAr: String,
    val goalType: MissionGoalType,
    val targetValue: Int,
    val rewardCoins: Int,
    val arenaLevel: ArenaLevel,
    val noCrashAllowed: Boolean = false
)

val ALL_MISSIONS: List<Mission> = listOf(
    Mission(
        1,
        "Mission 1: First Ignition",
        "المهمة 1: الشرارة الأولى",
        "Collect 12 Energy Orbs in Neon City.",
        "اجمع 12 كرة طاقة في مدينة النيون.",
        MissionGoalType.COLLECT_ORBS, 12, 80, ArenaLevel.NEON_CITY
    ),
    Mission(
        2,
        "Mission 2: Cyber Surge",
        "المهمة 2: التدفق السيبراني",
        "Score 450 points in Cyber Grid.",
        "حقق 450 نقطة في الشبكة السيبرانية.",
        MissionGoalType.SCORE_POINTS, 450, 100, ArenaLevel.CYBER_GRID
    ),
    Mission(
        3,
        "Mission 3: Power Collector",
        "المهمة 3: صائد القدرات",
        "Activate 3 Special Power-ups in a single run.",
        "اجمع وفعل 3 قدرات خاصة في جولة واحدة.",
        MissionGoalType.COLLECT_POWERUPS, 3, 120, ArenaLevel.NEON_CITY
    ),
    Mission(
        4,
        "Mission 4: Chain Reaction",
        "المهمة 4: التفاعل المتسلسل",
        "Reach a 6x Combo chain in Plasma Arena.",
        "وصل إلى سلسلة كومبو 6 متتالية في ساحة البلازما.",
        MissionGoalType.REACH_COMBO, 6, 150, ArenaLevel.PLASMA_ARENA
    ),
    Mission(
        5,
        "Mission 5: Plasma Awakening",
        "المهمة 5: صحوة البلازما",
        "Evolve to Stage 3 (Plasma Snake).",
        "تطور إلى المرحلة 3 (حنش البلازما).",
        MissionGoalType.REACH_EVOLUTION, 3, 180, ArenaLevel.DARK_SPACE
    ),
    Mission(
        6,
        "Mission 6: Toxic Endurance",
        "المهمة 6: الصمود في المنطقة السامة",
        "Survive for 45 seconds in Toxic Zone.",
        "ابق على قيد الحياة لمدة 45 ثانية في المنطقة السامة.",
        MissionGoalType.SURVIVE_SECONDS, 45, 220, ArenaLevel.TOXIC_ZONE
    ),
    Mission(
        7,
        "Mission 7: Crystal Overdrive",
        "المهمة 7: ذروة الكريستال",
        "Score 1,200 points in Crystal Lab.",
        "حقق 1,200 نقطة في مختبر الكريستال.",
        MissionGoalType.SCORE_POINTS, 1200, 280, ArenaLevel.CRYSTAL_LAB
    ),
    Mission(
        8,
        "Mission 8: Void Ascendant",
        "المهمة 8: سيد الفراغ",
        "Collect 25 Orbs in Void Arena without using Revive.",
        "اجمع 25 كرة طاقة في ساحة الفراغ دون خسارة.",
        MissionGoalType.COLLECT_ORBS, 25, 400, ArenaLevel.VOID_ARENA, noCrashAllowed = true
    )
)

data class DailyChallenge(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val target: Int,
    val progress: Int,
    val rewardCoins: Int,
    val isCompleted: Boolean
)

data class AchievementDef(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val descEn: String,
    val descAr: String,
    val icon: String,
    val rewardCoins: Int
)

val ALL_ACHIEVEMENTS: List<AchievementDef> = listOf(
    AchievementDef(
        "first_energy",
        "First Energy",
        "الطاقة الأولى",
        "Collect your very first Energy Orb.",
        "اجمع أول كرة طاقة لك في اللعبة.",
        "⚡", 30
    ),
    AchievementDef(
        "long_snake",
        "Long Snake",
        "الحنش العملاق",
        "Grow your snake to a length of 20 segments.",
        "وصل بطول الحنش إلى 20 قطعة.",
        "🐍", 80
    ),
    AchievementDef(
        "survival_master",
        "Survival Master",
        "سيد البقاء",
        "Survive for 60 seconds in a single match.",
        "ابق على قيد الحياة لمدة 60 ثانية في جولة واحدة.",
        "⏱️", 100
    ),
    AchievementDef(
        "high_score",
        "High Score Legend",
        "أسطورة النقاط",
        "Score at least 1,000 points in a single run.",
        "حقق 1,000 نقطة أو أكثر في جولة واحدة.",
        "🏆", 120
    ),
    AchievementDef(
        "evolution_master",
        "Apex Evolution",
        "قمة التطور",
        "Reach Stage 4 (Cyber Snake) or Stage 5 (Ultimate Snake).",
        "وصل إلى المرحلة الرابعة أو الخامسة من التطور.",
        "🧬", 150
    ),
    AchievementDef(
        "combo_master",
        "Combo Master",
        "محترف الكومبو",
        "Trigger a 10+ MEGA COMBO streak.",
        "حقق سلسلة كومبو من 10 كرات متتالية.",
        "🔥", 110
    ),
    AchievementDef(
        "boss_slayer",
        "Boss Slayer",
        "قاهر الزعماء",
        "Defeat a Boss Core in Boss Challenge or Arcade.",
        "اهزم أحد الزعماء في تحدي الزعماء.",
        "👾", 200
    ),
    AchievementDef(
        "mission_complete",
        "Tactical Operative",
        "منفذ المهام",
        "Complete at least 3 Missions.",
        "أكمل 3 مهام تكتيكية بنجاح.",
        "🎯", 150
    )
)

data class PlayerStats(
    val totalGames: Int = 0,
    val bestScore: Int = 0,
    val totalScore: Long = 0L,
    val totalCoinsEarned: Int = 0,
    val longestSnake: Int = 3,
    val longestSurvivalSeconds: Int = 0,
    val totalOrbsCollected: Int = 0,
    val bossesDefeated: Int = 0,
    val completedMissionsCount: Int = 0
)

data class GameSettings(
    val musicVolume: Float = 0.7f,
    val sfxVolume: Float = 0.85f,
    val vibrationEnabled: Boolean = true,
    val controlMode: ControlMode = ControlMode.BOTH,
    val swipeSensitivity: Float = 1.0f, // 0.5f to 1.5f
    val gameSpeedMultiplier: Float = 1.0f, // 0.8f (Relaxed), 1.0f (Arcade), 1.25f (Hyper)
    val reducedVisualEffects: Boolean = false,
    val reviveEnabled: Boolean = true,
    val language: AppLanguage = AppLanguage.AR, // Default Arabic + English toggle
    val tutorialSeen: Boolean = false
)
