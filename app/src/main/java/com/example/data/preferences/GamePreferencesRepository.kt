package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "snake_evolution_prefs")

data class PersistedPlayerData(
    val coins: Int = 250,
    val unlockedSkins: Set<String> = setOf(SnakeSkin.NEON_GREEN.id, SnakeSkin.ELECTRIC_BLUE.id),
    val selectedSkinId: String = SnakeSkin.NEON_GREEN.id,
    val unlockedArenas: Set<String> = setOf(ArenaLevel.NEON_CITY.id, ArenaLevel.CYBER_GRID.id),
    val selectedArenaId: String = ArenaLevel.NEON_CITY.id,
    val completedMissions: Set<Int> = emptySet(),
    val unlockedAchievements: Set<String> = emptySet(),
    val skillLevels: Map<SkillType, Int> = SkillType.entries.associateWith { 0 },
    val stats: PlayerStats = PlayerStats(),
    val settings: GameSettings = GameSettings(),
    val dailyChallenges: List<DailyChallenge> = emptyList(),
    val dailyDateKey: String = ""
)

class GamePreferencesRepository(private val context: Context) {

    private object Keys {
        val COINS = intPreferencesKey("coins")
        val UNLOCKED_SKINS = stringSetPreferencesKey("unlocked_skins")
        val SELECTED_SKIN = stringPreferencesKey("selected_skin")
        val UNLOCKED_ARENAS = stringSetPreferencesKey("unlocked_arenas")
        val SELECTED_ARENA = stringPreferencesKey("selected_arena")
        val COMPLETED_MISSIONS = stringSetPreferencesKey("completed_missions")
        val UNLOCKED_ACHIEVEMENTS = stringSetPreferencesKey("unlocked_achievements")

        // Stats
        val TOTAL_GAMES = intPreferencesKey("total_games")
        val BEST_SCORE = intPreferencesKey("best_score")
        val TOTAL_SCORE = longPreferencesKey("total_score")
        val TOTAL_COINS_EARNED = intPreferencesKey("total_coins_earned")
        val LONGEST_SNAKE = intPreferencesKey("longest_snake")
        val LONGEST_SURVIVAL = intPreferencesKey("longest_survival")
        val TOTAL_ORBS = intPreferencesKey("total_orbs")
        val BOSSES_DEFEATED = intPreferencesKey("bosses_defeated")

        // Settings
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val VIBRATION = booleanPreferencesKey("vibration")
        val CONTROL_MODE = stringPreferencesKey("control_mode")
        val SWIPE_SENSITIVITY = floatPreferencesKey("swipe_sensitivity")
        val GAME_SPEED = floatPreferencesKey("game_speed")
        val REDUCED_FX = booleanPreferencesKey("reduced_fx")
        val REVIVE_ENABLED = booleanPreferencesKey("revive_enabled")
        val LANGUAGE = stringPreferencesKey("language")
        val TUTORIAL_SEEN = booleanPreferencesKey("tutorial_seen")

        // Daily Challenges
        val DAILY_DATE_KEY = stringPreferencesKey("daily_date_key")
        val DAILY_CHALLENGE_1_PROG = intPreferencesKey("daily_c1_prog")
        val DAILY_CHALLENGE_2_PROG = intPreferencesKey("daily_c2_prog")
        val DAILY_CHALLENGE_3_PROG = intPreferencesKey("daily_c3_prog")
        val DAILY_CHALLENGE_1_DONE = booleanPreferencesKey("daily_c1_done")
        val DAILY_CHALLENGE_2_DONE = booleanPreferencesKey("daily_c2_done")
        val DAILY_CHALLENGE_3_DONE = booleanPreferencesKey("daily_c3_done")

        fun skillKey(skill: SkillType) = intPreferencesKey("skill_${skill.id}")
    }

    private fun todayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    val playerDataFlow: Flow<PersistedPlayerData> = context.dataStore.data.map { prefs ->
        val today = todayDateKey()
        val savedDate = prefs[Keys.DAILY_DATE_KEY] ?: ""
        val isSameDay = savedDate == today

        val c1Prog = if (isSameDay) (prefs[Keys.DAILY_CHALLENGE_1_PROG] ?: 0) else 0
        val c2Prog = if (isSameDay) (prefs[Keys.DAILY_CHALLENGE_2_PROG] ?: 0) else 0
        val c3Prog = if (isSameDay) (prefs[Keys.DAILY_CHALLENGE_3_PROG] ?: 0) else 0
        val c1Done = if (isSameDay) (prefs[Keys.DAILY_CHALLENGE_1_DONE] ?: false) else false
        val c2Done = if (isSameDay) (prefs[Keys.DAILY_CHALLENGE_2_DONE] ?: false) else false
        val c3Done = if (isSameDay) (prefs[Keys.DAILY_CHALLENGE_3_DONE] ?: false) else false

        val dailyList = listOf(
            DailyChallenge(
                id = "daily_orbs",
                titleEn = "Collect 25 Energy Orbs today",
                titleAr = "اجمع 25 كرة طاقة اليوم",
                target = 25,
                progress = c1Prog.coerceAtMost(25),
                rewardCoins = 100,
                isCompleted = c1Done
            ),
            DailyChallenge(
                id = "daily_score",
                titleEn = "Score 800+ points in a match",
                titleAr = "حقق 800 نقطة أو أكثر في جولة",
                target = 800,
                progress = c2Prog.coerceAtMost(800),
                rewardCoins = 130,
                isCompleted = c2Done
            ),
            DailyChallenge(
                id = "daily_golden",
                titleEn = "Collect 5 Golden Energy Orbs",
                titleAr = "اجمع 5 كرات طاقة ذهبية",
                target = 5,
                progress = c3Prog.coerceAtMost(5),
                rewardCoins = 150,
                isCompleted = c3Done
            )
        )

        val completedMissionIds = (prefs[Keys.COMPLETED_MISSIONS] ?: emptySet())
            .mapNotNull { it.toIntOrNull() }
            .toSet()

        val skillsMap = SkillType.entries.associateWith { skill ->
            (prefs[Keys.skillKey(skill)] ?: 0).coerceIn(0, skill.maxLevel)
        }

        val controlMode = ControlMode.entries.find {
            it.name == (prefs[Keys.CONTROL_MODE] ?: ControlMode.BOTH.name)
        } ?: ControlMode.BOTH

        val lang = AppLanguage.entries.find {
            it.name == (prefs[Keys.LANGUAGE] ?: AppLanguage.AR.name)
        } ?: AppLanguage.AR

        PersistedPlayerData(
            coins = prefs[Keys.COINS] ?: 250,
            unlockedSkins = (prefs[Keys.UNLOCKED_SKINS] ?: emptySet()) +
                    setOf(SnakeSkin.NEON_GREEN.id, SnakeSkin.ELECTRIC_BLUE.id),
            selectedSkinId = prefs[Keys.SELECTED_SKIN] ?: SnakeSkin.NEON_GREEN.id,
            unlockedArenas = (prefs[Keys.UNLOCKED_ARENAS] ?: emptySet()) +
                    setOf(ArenaLevel.NEON_CITY.id, ArenaLevel.CYBER_GRID.id),
            selectedArenaId = prefs[Keys.SELECTED_ARENA] ?: ArenaLevel.NEON_CITY.id,
            completedMissions = completedMissionIds,
            unlockedAchievements = prefs[Keys.UNLOCKED_ACHIEVEMENTS] ?: emptySet(),
            skillLevels = skillsMap,
            stats = PlayerStats(
                totalGames = prefs[Keys.TOTAL_GAMES] ?: 0,
                bestScore = prefs[Keys.BEST_SCORE] ?: 0,
                totalScore = prefs[Keys.TOTAL_SCORE] ?: 0L,
                totalCoinsEarned = prefs[Keys.TOTAL_COINS_EARNED] ?: 0,
                longestSnake = prefs[Keys.LONGEST_SNAKE] ?: 3,
                longestSurvivalSeconds = prefs[Keys.LONGEST_SURVIVAL] ?: 0,
                totalOrbsCollected = prefs[Keys.TOTAL_ORBS] ?: 0,
                bossesDefeated = prefs[Keys.BOSSES_DEFEATED] ?: 0,
                completedMissionsCount = completedMissionIds.size
            ),
            settings = GameSettings(
                musicVolume = prefs[Keys.MUSIC_VOLUME] ?: 0.7f,
                sfxVolume = prefs[Keys.SFX_VOLUME] ?: 0.85f,
                vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
                controlMode = controlMode,
                swipeSensitivity = prefs[Keys.SWIPE_SENSITIVITY] ?: 1.0f,
                gameSpeedMultiplier = prefs[Keys.GAME_SPEED] ?: 1.0f,
                reducedVisualEffects = prefs[Keys.REDUCED_FX] ?: false,
                reviveEnabled = prefs[Keys.REVIVE_ENABLED] ?: true,
                language = lang,
                tutorialSeen = prefs[Keys.TUTORIAL_SEEN] ?: false
            ),
            dailyChallenges = dailyList,
            dailyDateKey = today
        )
    }

    suspend fun updateSettings(transform: (GameSettings) -> GameSettings) {
        context.dataStore.edit { prefs ->
            val currentControl = ControlMode.entries.find {
                it.name == (prefs[Keys.CONTROL_MODE] ?: ControlMode.BOTH.name)
            } ?: ControlMode.BOTH
            val currentLang = AppLanguage.entries.find {
                it.name == (prefs[Keys.LANGUAGE] ?: AppLanguage.AR.name)
            } ?: AppLanguage.AR
            val current = GameSettings(
                musicVolume = prefs[Keys.MUSIC_VOLUME] ?: 0.7f,
                sfxVolume = prefs[Keys.SFX_VOLUME] ?: 0.85f,
                vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
                controlMode = currentControl,
                swipeSensitivity = prefs[Keys.SWIPE_SENSITIVITY] ?: 1.0f,
                gameSpeedMultiplier = prefs[Keys.GAME_SPEED] ?: 1.0f,
                reducedVisualEffects = prefs[Keys.REDUCED_FX] ?: false,
                reviveEnabled = prefs[Keys.REVIVE_ENABLED] ?: true,
                language = currentLang,
                tutorialSeen = prefs[Keys.TUTORIAL_SEEN] ?: false
            )
            val updated = transform(current)
            prefs[Keys.MUSIC_VOLUME] = updated.musicVolume
            prefs[Keys.SFX_VOLUME] = updated.sfxVolume
            prefs[Keys.VIBRATION] = updated.vibrationEnabled
            prefs[Keys.CONTROL_MODE] = updated.controlMode.name
            prefs[Keys.SWIPE_SENSITIVITY] = updated.swipeSensitivity
            prefs[Keys.GAME_SPEED] = updated.gameSpeedMultiplier
            prefs[Keys.REDUCED_FX] = updated.reducedVisualEffects
            prefs[Keys.REVIVE_ENABLED] = updated.reviveEnabled
            prefs[Keys.LANGUAGE] = updated.language.name
            prefs[Keys.TUTORIAL_SEEN] = updated.tutorialSeen
        }
    }

    suspend fun selectSkin(skin: SnakeSkin) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_SKIN] = skin.id
        }
    }

    suspend fun unlockAndSelectSkin(skin: SnakeSkin): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val currentCoins = prefs[Keys.COINS] ?: 250
            val unlocked = (prefs[Keys.UNLOCKED_SKINS] ?: emptySet()).toMutableSet()
            if (skin.id in unlocked || skin.cost == 0) {
                prefs[Keys.SELECTED_SKIN] = skin.id
                success = true
            } else if (currentCoins >= skin.cost) {
                prefs[Keys.COINS] = currentCoins - skin.cost
                unlocked.add(skin.id)
                prefs[Keys.UNLOCKED_SKINS] = unlocked
                prefs[Keys.SELECTED_SKIN] = skin.id
                success = true
            }
        }
        return success
    }

    suspend fun selectArena(arena: ArenaLevel) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_ARENA] = arena.id
        }
    }

    suspend fun unlockAndSelectArena(arena: ArenaLevel): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val currentCoins = prefs[Keys.COINS] ?: 250
            val unlocked = (prefs[Keys.UNLOCKED_ARENAS] ?: emptySet()).toMutableSet()
            if (arena.id in unlocked || arena.unlockCost == 0) {
                prefs[Keys.SELECTED_ARENA] = arena.id
                success = true
            } else if (currentCoins >= arena.unlockCost) {
                prefs[Keys.COINS] = currentCoins - arena.unlockCost
                unlocked.add(arena.id)
                prefs[Keys.UNLOCKED_ARENAS] = unlocked
                prefs[Keys.SELECTED_ARENA] = arena.id
                success = true
            }
        }
        return success
    }

    suspend fun upgradeSkill(skill: SkillType): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val currentCoins = prefs[Keys.COINS] ?: 250
            val currentLevel = (prefs[Keys.skillKey(skill)] ?: 0).coerceIn(0, skill.maxLevel)
            if (currentLevel < skill.maxLevel) {
                val cost = skill.costForNextLevel(currentLevel)
                if (currentCoins >= cost) {
                    prefs[Keys.COINS] = currentCoins - cost
                    prefs[Keys.skillKey(skill)] = currentLevel + 1
                    success = true
                }
            }
        }
        return success
    }

    /**
     * Records match results, updates statistics, checks missions, daily challenges, and achievements,
     * and returns newly unlocked achievement definitions + bonus coins awarded.
     */
    suspend fun recordMatchResult(
        score: Int,
        coinsEarnedInRun: Int,
        orbsCollected: Int,
        goldenOrbsCollected: Int,
        maxLength: Int,
        survivalSeconds: Int,
        maxCombo: Int,
        evolutionStageReached: EvolutionStage,
        bossDefeatedCount: Int,
        missionCompletedId: Int?
    ): List<AchievementDef> {
        val newlyUnlockedAchievements = mutableListOf<AchievementDef>()

        context.dataStore.edit { prefs ->
            val today = todayDateKey()
            val savedDate = prefs[Keys.DAILY_DATE_KEY] ?: ""
            if (savedDate != today) {
                prefs[Keys.DAILY_DATE_KEY] = today
                prefs[Keys.DAILY_CHALLENGE_1_PROG] = 0
                prefs[Keys.DAILY_CHALLENGE_2_PROG] = 0
                prefs[Keys.DAILY_CHALLENGE_3_PROG] = 0
                prefs[Keys.DAILY_CHALLENGE_1_DONE] = false
                prefs[Keys.DAILY_CHALLENGE_2_DONE] = false
                prefs[Keys.DAILY_CHALLENGE_3_DONE] = false
            }

            var bonusCoins = 0

            // Update Daily Challenges
            val c1New = (prefs[Keys.DAILY_CHALLENGE_1_PROG] ?: 0) + orbsCollected
            val c2New = maxOf(prefs[Keys.DAILY_CHALLENGE_2_PROG] ?: 0, score)
            val c3New = (prefs[Keys.DAILY_CHALLENGE_3_PROG] ?: 0) + goldenOrbsCollected

            prefs[Keys.DAILY_CHALLENGE_1_PROG] = c1New
            prefs[Keys.DAILY_CHALLENGE_2_PROG] = c2New
            prefs[Keys.DAILY_CHALLENGE_3_PROG] = c3New

            if (c1New >= 25 && prefs[Keys.DAILY_CHALLENGE_1_DONE] != true) {
                prefs[Keys.DAILY_CHALLENGE_1_DONE] = true
                bonusCoins += 100
            }
            if (c2New >= 800 && prefs[Keys.DAILY_CHALLENGE_2_DONE] != true) {
                prefs[Keys.DAILY_CHALLENGE_2_DONE] = true
                bonusCoins += 130
            }
            if (c3New >= 5 && prefs[Keys.DAILY_CHALLENGE_3_DONE] != true) {
                prefs[Keys.DAILY_CHALLENGE_3_DONE] = true
                bonusCoins += 150
            }

            // Missions
            val completedMissions = (prefs[Keys.COMPLETED_MISSIONS] ?: emptySet()).toMutableSet()
            if (missionCompletedId != null) {
                val missionIdStr = missionCompletedId.toString()
                if (missionIdStr !in completedMissions) {
                    completedMissions.add(missionIdStr)
                    prefs[Keys.COMPLETED_MISSIONS] = completedMissions
                    val missionReward = ALL_MISSIONS.find { it.id == missionCompletedId }?.rewardCoins ?: 100
                    bonusCoins += missionReward
                }
            }

            // Stats
            val totalGames = (prefs[Keys.TOTAL_GAMES] ?: 0) + 1
            val bestScore = maxOf(prefs[Keys.BEST_SCORE] ?: 0, score)
            val totalScore = (prefs[Keys.TOTAL_SCORE] ?: 0L) + score
            val longestSnake = maxOf(prefs[Keys.LONGEST_SNAKE] ?: 3, maxLength)
            val longestSurvival = maxOf(prefs[Keys.LONGEST_SURVIVAL] ?: 0, survivalSeconds)
            val totalOrbs = (prefs[Keys.TOTAL_ORBS] ?: 0) + orbsCollected
            val totalBosses = (prefs[Keys.BOSSES_DEFEATED] ?: 0) + bossDefeatedCount

            // Achievements check
            val unlockedAch = (prefs[Keys.UNLOCKED_ACHIEVEMENTS] ?: emptySet()).toMutableSet()

            fun checkAndUnlock(id: String, condition: Boolean) {
                if (condition && id !in unlockedAch) {
                    unlockedAch.add(id)
                    val def = ALL_ACHIEVEMENTS.find { it.id == id }
                    if (def != null) {
                        newlyUnlockedAchievements.add(def)
                        bonusCoins += def.rewardCoins
                    }
                }
            }

            checkAndUnlock("first_energy", totalOrbs >= 1)
            checkAndUnlock("long_snake", longestSnake >= 20)
            checkAndUnlock("survival_master", longestSurvival >= 60)
            checkAndUnlock("high_score", bestScore >= 1000)
            checkAndUnlock("evolution_master", evolutionStageReached.stageNumber >= 4)
            checkAndUnlock("combo_master", maxCombo >= 10)
            checkAndUnlock("boss_slayer", totalBosses >= 1)
            checkAndUnlock("mission_complete", completedMissions.size >= 3)

            prefs[Keys.UNLOCKED_ACHIEVEMENTS] = unlockedAch

            val totalEarnedThisMatch = coinsEarnedInRun + bonusCoins
            prefs[Keys.COINS] = (prefs[Keys.COINS] ?: 250) + totalEarnedThisMatch
            prefs[Keys.TOTAL_COINS_EARNED] = (prefs[Keys.TOTAL_COINS_EARNED] ?: 0) + totalEarnedThisMatch

            prefs[Keys.TOTAL_GAMES] = totalGames
            prefs[Keys.BEST_SCORE] = bestScore
            prefs[Keys.TOTAL_SCORE] = totalScore
            prefs[Keys.LONGEST_SNAKE] = longestSnake
            prefs[Keys.LONGEST_SURVIVAL] = longestSurvival
            prefs[Keys.TOTAL_ORBS] = totalOrbs
            prefs[Keys.BOSSES_DEFEATED] = totalBosses
        }

        return newlyUnlockedAchievements
    }

    suspend fun resetAllProgress() {
        context.dataStore.edit { prefs ->
            val currentLang = prefs[Keys.LANGUAGE]
            prefs.clear()
            if (currentLang != null) {
                prefs[Keys.LANGUAGE] = currentLang
            }
        }
    }
}
