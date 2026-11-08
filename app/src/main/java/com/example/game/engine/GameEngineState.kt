package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.domain.models.*

enum class PlayStatus {
    IDLE,
    RUNNING,
    PAUSED,
    VICTORY,
    GAME_OVER
}

data class GameEngineState(
    val gridWidth: Int = 20,
    val gridHeight: Int = 28,
    val status: PlayStatus = PlayStatus.IDLE,
    val gameMode: GameMode = GameMode.CLASSIC,
    val arena: ArenaLevel = ArenaLevel.NEON_CITY,
    val activeMission: Mission? = null,
    val snake: List<GridPos> = listOf(GridPos(10, 16), GridPos(10, 17), GridPos(10, 18)),
    val direction: Direction = Direction.UP,
    val score: Int = 0,
    val bestScore: Int = 0,
    val coinsEarnedThisRun: Int = 0,
    val orbsCollectedThisRun: Int = 0,
    val goldenOrbsThisRun: Int = 0,
    val powerUpsCollectedThisRun: Int = 0,
    val evolutionXp: Int = 0,
    val evolutionStage: EvolutionStage = EvolutionStage.MINI_SNAKE,
    val comboCount: Int = 0,
    val maxComboThisRun: Int = 0,
    val comboMultiplier: Float = 1.0f,
    val comboBannerText: String? = null,
    val comboRemainingTicks: Int = 0,
    val maxComboWindowTicks: Int = 28,
    val energyOrbs: List<EnergyOrb> = emptyList(),
    val powerUpItems: List<PowerUpItem> = emptyList(),
    val activePowerUps: List<ActivePowerUp> = emptyList(),
    val obstacles: List<Obstacle> = emptyList(),
    val bossState: BossState? = null,
    val bossesDefeatedThisRun: Int = 0,
    val elapsedSeconds: Int = 0,
    val timeRemainingSeconds: Int? = null, // Used in Time Attack
    val reviveUsed: Boolean = false,
    val canRevive: Boolean = true,
    val invulnerableTicks: Int = 0,
    val speedPoisonTicks: Int = 0,
    val notificationBanner: String? = null,
    val notificationColor: Color = Color(0xFF00FF87),
    val particles: List<Particle> = emptyList(),
    val floatingTexts: List<FloatingText> = emptyList(),
    val tickCount: Int = 0
) {
    fun hasPowerUp(type: PowerUpType): Boolean {
        return activePowerUps.any { it.type == type && it.remainingTicks > 0 }
    }

    val missionProgressCurrent: Int
        get() {
            val m = activeMission ?: return 0
            return when (m.goalType) {
                MissionGoalType.COLLECT_ORBS -> orbsCollectedThisRun
                MissionGoalType.SCORE_POINTS -> score
                MissionGoalType.COLLECT_POWERUPS -> powerUpsCollectedThisRun
                MissionGoalType.REACH_EVOLUTION -> evolutionStage.stageNumber
                MissionGoalType.SURVIVE_SECONDS -> elapsedSeconds
                MissionGoalType.REACH_COMBO -> maxComboThisRun
            }
        }
}
