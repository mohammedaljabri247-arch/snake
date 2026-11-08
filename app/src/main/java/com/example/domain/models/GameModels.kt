package com.example.domain.models

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

data class GridPos(val x: Int, val y: Int) {
    fun move(dir: Direction): GridPos = GridPos(x + dir.dx, y + dir.dy)
}

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun isOpposite(other: Direction): Boolean {
        return (this == UP && other == DOWN) ||
                (this == DOWN && other == UP) ||
                (this == LEFT && other == RIGHT) ||
                (this == RIGHT && other == LEFT)
    }
}

enum class ControlMode(val titleEn: String, val titleAr: String) {
    BOTH("Swipe + D-Pad", "سحب + أزرار الاتجاهات"),
    SWIPE("Swipe Gestures", "إيماءات السحب"),
    DPAD("Virtual D-Pad", "أزرار الاتجاهات فقط")
}

enum class AppLanguage(val code: String, val label: String) {
    EN("en", "English"),
    AR("ar", "العربية")
}

enum class EnergyType(
    val titleEn: String,
    val titleAr: String,
    val basePoints: Int,
    val coinsReward: Int,
    val evolutionXp: Int,
    val color: Color,
    val secondaryColor: Color,
    val isRisk: Boolean = false
) {
    NORMAL("Normal Energy", "طاقة عادية", 15, 1, 10, NeonGreen, ElectricCyan, false),
    GOLDEN("Golden Energy", "طاقة ذهبية", 60, 8, 25, GoldenEnergy, CyberOrange, true),
    PLASMA("Plasma Energy", "طاقة بلازما", 40, 3, 45, PlasmaPurple, ElectricCyan, false),
    TOXIC("Toxic Risk Orb", "كرة سامة خطرة", 90, 12, 30, ToxicGreen, DangerRed, true),
    BONUS("Mystery Bonus", "طاقة مكافأة", 50, 5, 20, NeonMagenta, GoldenEnergy, false)
}

data class EnergyOrb(
    val id: Long,
    val pos: GridPos,
    val type: EnergyType,
    val spawnTick: Int,
    val maxLifetimeTicks: Int? = null // Golden, Toxic, Bonus expire after some ticks
)

enum class EvolutionStage(
    val stageNumber: Int,
    val nameEn: String,
    val nameAr: String,
    val perkEn: String,
    val perkAr: String,
    val minXp: Int,
    val headColor: Color,
    val bodyColor: Color,
    val glowRadiusMultiplier: Float,
    val scoreBonusMultiplier: Float,
    val coinBonusAdd: Int
) {
    MINI_SNAKE(
        stageNumber = 1,
        nameEn = "Stage 1: Mini Snake",
        nameAr = "المرحلة 1: الحنش الصغير",
        perkEn = "Agile starter chassis",
        perkAr = "هيكل ابتدائي رشيق",
        minXp = 0,
        headColor = NeonGreen,
        bodyColor = Color(0xFF00C96B),
        glowRadiusMultiplier = 1.0f,
        scoreBonusMultiplier = 1.0f,
        coinBonusAdd = 0
    ),
    NEON_SNAKE(
        stageNumber = 2,
        nameEn = "Stage 2: Neon Snake",
        nameAr = "المرحلة 2: حنش النيون",
        perkEn = "+15% Score & Dual Pulse Aura",
        perkAr = "+15% نقاط وهالة نيون مزدوجة",
        minXp = 80,
        headColor = ElectricCyan,
        bodyColor = NeonGreen,
        glowRadiusMultiplier = 1.25f,
        scoreBonusMultiplier = 1.15f,
        coinBonusAdd = 1
    ),
    PLASMA_SNAKE(
        stageNumber = 3,
        nameEn = "Stage 3: Plasma Snake",
        nameAr = "المرحلة 3: حنش البلازما",
        perkEn = "+35% Score & +1 Passive Magnet Range",
        perkAr = "+35% نقاط ومدى جذب إضافي",
        minXp = 210,
        headColor = PlasmaPurple,
        bodyColor = ElectricCyan,
        glowRadiusMultiplier = 1.5f,
        scoreBonusMultiplier = 1.35f,
        coinBonusAdd = 2
    ),
    CYBER_SNAKE(
        stageNumber = 4,
        nameEn = "Stage 4: Cyber Snake",
        nameAr = "المرحلة 4: الحنش السيبراني",
        perkEn = "+60% Score & +2 Coins per Orb",
        perkAr = "+60% نقاط وعملتان إضافيتان",
        minXp = 400,
        headColor = NeonMagenta,
        bodyColor = PlasmaPurple,
        glowRadiusMultiplier = 1.8f,
        scoreBonusMultiplier = 1.6f,
        coinBonusAdd = 3
    ),
    ULTIMATE_SNAKE(
        stageNumber = 5,
        nameEn = "Stage 5: Ultimate Snake",
        nameAr = "المرحلة 5: الحنش المطلق",
        perkEn = "2x Score, Overdrive Halo & Auto-Attract",
        perkAr = "مضاعفة النقاط وهالة الطاقة القصوى",
        minXp = 700,
        headColor = GoldenEnergy,
        bodyColor = NeonMagenta,
        glowRadiusMultiplier = 2.2f,
        scoreBonusMultiplier = 2.0f,
        coinBonusAdd = 5
    );

    companion object {
        fun fromXp(xp: Int): EvolutionStage {
            return entries.lastOrNull { xp >= it.minXp } ?: MINI_SNAKE
        }
    }
}

enum class PowerUpType(
    val titleEn: String,
    val titleAr: String,
    val symbol: String,
    val baseDurationTicks: Int,
    val color: Color
) {
    SPEED_BOOST("Speed Boost", "تعزيز السرعة", "⚡", 45, CyberOrange),
    SHIELD("Holo Shield", "درع الحماية", "🛡️", 90, ElectricCyan),
    ENERGY_MAGNET("Energy Magnet", "مغناطيس الطاقة", "🧲", 65, PlasmaPurple),
    SLOW_MOTION("Chrono Slow", "إبطاء الزمن", "⏳", 50, NeonGreen),
    BONUS_ORB("Instant Jackpot", "مكافأة فورية", "💎", 1, GoldenEnergy),
    GHOST_MODE("Ghost Phase", "طور الشبح", "👻", 55, Color(0xFFA0E9FF)),
    DOUBLE_SCORE("2X Overdrive", "مضاعف النقاط", "✖️2", 65, NeonMagenta)
}

data class PowerUpItem(
    val id: Long,
    val pos: GridPos,
    val type: PowerUpType,
    val spawnTick: Int,
    val maxLifetimeTicks: Int = 75
)

data class ActivePowerUp(
    val type: PowerUpType,
    val remainingTicks: Int,
    val totalTicks: Int
)

enum class GameMode(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val descEn: String,
    val descAr: String,
    val accentColor: Color
) {
    CLASSIC(
        "classic",
        "Classic Arcade",
        "النمط الكلاسيكي",
        "Pure neon survival. Grow, evolve, and chase the ultimate high score.",
        "اجمع كرات الطاقة وتطور وحقق أعلى نتيجة ممكنة.",
        NeonGreen
    ),
    SURVIVAL(
        "survival",
        "Survival Overdrive",
        "نمط البقاء",
        "Escalating speed and dynamic hazards appearing over time. Endure the storm.",
        "سرعة متزايدة وعقبات ديناميكية تظهر بمرور الوقت.",
        ElectricCyan
    ),
    MISSION(
        "mission",
        "Tactical Missions",
        "نمط المهام",
        "Complete structured objectives with custom arenas and big coin payouts.",
        "أكمل مهام محددة وأهدافًا تكتيكية للحصول على مكافآت كبيرة.",
        PlasmaPurple
    ),
    TIME_ATTACK(
        "time_attack",
        "Time Attack (90s)",
        "سباق الوقت (90 ثانية)",
        "Hyper-spawn energy frenzy! Score as much as possible before the clock hits zero.",
        "اجمع أكبر عدد ممكن من النقاط قبل انتهاء الوقت المحدد.",
        GoldenEnergy
    ),
    BOSS_CHALLENGE(
        "boss_challenge",
        "Boss Challenge",
        "تحدي الزعماء",
        "Enter the reactor arena and collect strike orbs to overload the Boss Core!",
        "واجه زعماء النيون ودمر نواة الطاقة مع تفادي الهجمات.",
        DangerRed
    )
}

enum class ArenaLevel(
    val id: String,
    val index: Int,
    val titleEn: String,
    val titleAr: String,
    val descEn: String,
    val descAr: String,
    val unlockCost: Int,
    val bgTop: Color,
    val bgBottom: Color,
    val gridColor: Color,
    val obstacleColor: Color,
    val allowWrapWalls: Boolean = false
) {
    NEON_CITY(
        "neon_city", 1,
        "Neon City", "مدينة النيون",
        "Futuristic metropolis grid with clean perimeter walls.",
        "مدينة مستقبلية مضيئة ذات ساحة مفتوحة للمبتدئين والمحترفين.",
        0,
        Color(0xFF070B1E), Color(0xFF140A2E), Color(0xFF00F5FF), NeonMagenta
    ),
    CYBER_GRID(
        "cyber_grid", 2,
        "Cyber Grid", "الشبكة السيبرانية",
        "Twin laser pillars guarding the central data highway.",
        "ساحة إلكترونية متوهجة تحتوي على أعمدة ليزر تكتيكية.",
        0,
        Color(0xFF041418), Color(0xFF0A2436), Color(0xFF00FF87), ElectricCyan
    ),
    PLASMA_ARENA(
        "plasma_arena", 3,
        "Plasma Arena", "ساحة البلازما",
        "High-voltage reactor chambers with corner plasma barriers.",
        "ساحة مشحونة بالطاقة تحتوي على حواجز بلازما في الزوايا.",
        200,
        Color(0xFF1B062E), Color(0xFF320A47), Color(0xFFB517FF), GoldenEnergy
    ),
    DARK_SPACE(
        "dark_space", 4,
        "Dark Space", "الفضاء المظلم",
        "Zero-gravity warp borders! Cross screen edges to teleport across space.",
        "بيئة فضائية تسمح بالعبور من حواف الشاشة والانتقال للجهة المقابلة.",
        350,
        Color(0xFF03040A), Color(0xFF0D1126), Color(0xFF5D76CB), PlasmaPurple,
        allowWrapWalls = true
    ),
    TOXIC_ZONE(
        "toxic_zone", 5,
        "Toxic Zone", "المنطقة السامة",
        "Bio-hazard sector with patrol drones and unstable energy.",
        "منطقة خطرة تحتوي على حواجز متحركة وكرات طاقة عالية المخاطر.",
        500,
        Color(0xFF0A1908), Color(0xFF1A2E0B), Color(0xFF7BFF00), ToxicGreen
    ),
    CRYSTAL_LAB(
        "crystal_lab", 6,
        "Crystal Lab", "مختبر الكريستال",
        "Precision laser labyrinth that amplifies combo energy.",
        "مختبر مستقبلي مليء بالممرات الكريستالية الدقيقة.",
        750,
        Color(0xFF081C2E), Color(0xFF1E1240), Color(0xFF66FCF1), NeonMagenta
    ),
    VOID_ARENA(
        "void_arena", 7,
        "Void Arena", "ساحة الفراغ",
        "Elite mastery gauntlet with shifting void monoliths.",
        "ساحة النخبة الخاصة بالتحديات المتقدمة والعقبات المتحركة.",
        1000,
        Color(0xFF14041C), Color(0xFF050208), Color(0xFFFF007F), DangerRed
    )
}

enum class BossType(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val subtitleEn: String,
    val subtitleAr: String,
    val maxHp: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val coinReward: Int
) {
    PLASMA_CORE(
        "plasma_core",
        "PLASMA CORE",
        "نواة البلازما",
        "Emits cross-grid plasma shockwaves",
        "تطلق موجات بلازما متقاطعة عبر الساحة",
        100,
        PlasmaPurple,
        ElectricCyan,
        120
    ),
    CYBER_GUARDIAN(
        "cyber_guardian",
        "CYBER GUARDIAN",
        "الحارس السيبراني",
        "Deploys sweeping laser firewalls",
        "ينشر جدران ليزر متحركة عبر الشبكة",
        140,
        ElectricCyan,
        NeonMagenta,
        180
    ),
    TOXIC_REACTOR(
        "toxic_reactor",
        "TOXIC REACTOR",
        "المفاعل السام",
        "Spawns bio-hazard zones & toxic surges",
        "يولد مناطق خطرة وموجات إشعاعية",
        180,
        ToxicGreen,
        CyberOrange,
        240
    ),
    NEON_OVERLORD(
        "neon_overlord",
        "NEON OVERLORD",
        "سيد النيون المطلق",
        "Master of the Void with omni-directional strikes",
        "الزعيم الأعلى بهجمات متعددة الاتجاهات",
        240,
        DangerRed,
        GoldenEnergy,
        350
    )
}

data class BossState(
    val boss: BossType,
    val currentHp: Int,
    val maxHp: Int,
    val corePositions: List<GridPos>, // 2x2 core in top-center
    val warningCells: List<GridPos>,  // Telegraphed attack cells before strike!
    val activeHazardCells: List<GridPos>, // Active damaging strike cells
    val attackPhaseTick: Int,
    val hitFlashTicks: Int = 0
)

data class Obstacle(
    val pos: GridPos,
    val velocity: Direction? = null,
    val moveEveryTicks: Int = 6
)

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val alpha: Float,
    val size: Float
)

data class FloatingText(
    val id: Long,
    val text: String,
    val gridPos: GridPos,
    val color: Color,
    val progress: Float = 0f // 0f to 1f
)
