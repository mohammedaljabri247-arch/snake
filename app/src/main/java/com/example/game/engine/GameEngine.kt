package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.audio.SoundEffect
import com.example.domain.models.*
import com.example.game.systems.BossSystem
import com.example.game.systems.CollisionSystem
import com.example.game.systems.SpawnManager
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.ArrayDeque
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
    private val onSoundEffect: (SoundEffect) -> Unit = {},
    private val onVibrate: (Long, Boolean) -> Unit = { _, _ -> },
    private val onMatchFinished: (GameEngineState) -> Unit = {}
) {
    private val _state = MutableStateFlow(GameEngineState())
    val state: StateFlow<GameEngineState> = _state.asStateFlow()

    private val directionQueue = ArrayDeque<Direction>()
    private var floatingTextIdCounter = 1L

    fun startNewGame(
        mode: GameMode,
        arena: ArenaLevel,
        mission: Mission? = null,
        selectedBoss: BossType = BossType.PLASMA_CORE,
        bestScore: Int = 0,
        reviveAllowedBySettings: Boolean = true,
        skillLevels: Map<SkillType, Int> = emptyMap()
    ) {
        directionQueue.clear()
        val gw = 20
        val gh = 28
        val effectiveArena = mission?.arenaLevel ?: arena
        val startSnake = listOf(
            GridPos(gw / 2, gh - 8),
            GridPos(gw / 2, gh - 7),
            GridPos(gw / 2, gh - 6),
            GridPos(gw / 2, gh - 5)
        )

        val initialObstacles = if (mode == GameMode.BOSS_CHALLENGE) {
            listOf(
                Obstacle(GridPos(3, 12)),
                Obstacle(GridPos(gw - 4, 12)),
                Obstacle(GridPos(3, gh - 6)),
                Obstacle(GridPos(gw - 4, gh - 6))
            )
        } else {
            SpawnManager.createArenaObstacles(effectiveArena, gw, gh)
        }

        val initialBoss = if (mode == GameMode.BOSS_CHALLENGE) {
            BossSystem.createBoss(selectedBoss, gw)
        } else {
            null
        }

        val orbs = mutableListOf<EnergyOrb>()
        // Spawn 2 initial orbs (or 3 in Time Attack)
        val initialOrbCount = if (mode == GameMode.TIME_ATTACK) 3 else 2
        repeat(initialOrbCount) { idx ->
            val orb = SpawnManager.spawnEnergyOrb(
                gridWidth = gw,
                gridHeight = gh,
                snake = startSnake,
                obstacles = initialObstacles,
                existingOrbs = orbs,
                existingPowerUps = emptyList(),
                bossState = initialBoss,
                currentTick = 0,
                forceType = if (idx == 0) EnergyType.NORMAL else null
            )
            orbs.add(orb)
        }

        val comboBonusLevel = skillLevels[SkillType.SCORE_MULTIPLIER] ?: 0
        val maxComboWindow = 28 + comboBonusLevel * 4

        val canReviveInMatch = reviveAllowedBySettings && (mission?.noCrashAllowed != true)

        _state.value = GameEngineState(
            gridWidth = gw,
            gridHeight = gh,
            status = PlayStatus.RUNNING,
            gameMode = mode,
            arena = effectiveArena,
            activeMission = mission,
            snake = startSnake,
            direction = Direction.UP,
            score = 0,
            bestScore = bestScore,
            energyOrbs = orbs,
            obstacles = initialObstacles,
            bossState = initialBoss,
            timeRemainingSeconds = if (mode == GameMode.TIME_ATTACK) 90 else null,
            reviveUsed = false,
            canRevive = canReviveInMatch,
            maxComboWindowTicks = maxComboWindow,
            notificationBanner = when (mode) {
                GameMode.BOSS_CHALLENGE -> "⚠️ ${selectedBoss.nameEn} ENGAGED!"
                GameMode.MISSION -> "🎯 ${mission?.titleEn ?: "MISSION START"}"
                GameMode.TIME_ATTACK -> "⏱️ 90s HYPER TIME ATTACK!"
                GameMode.SURVIVAL -> "⚡ SURVIVAL OVERDRIVE!"
                GameMode.CLASSIC -> "🐍 NEON SURVIVAL ONLINE"
            },
            notificationColor = ElectricCyan
        )
    }

    fun queueDirection(newDir: Direction) {
        val currentState = _state.value
        if (currentState.status != PlayStatus.RUNNING) return

        val lastPlannedDir = directionQueue.peekLast() ?: currentState.direction
        if (newDir != lastPlannedDir && !newDir.isOpposite(lastPlannedDir)) {
            if (directionQueue.size < 2) {
                directionQueue.addLast(newDir)
            }
        }
    }

    fun pauseGame() {
        val cur = _state.value
        if (cur.status == PlayStatus.RUNNING) {
            _state.value = cur.copy(status = PlayStatus.PAUSED)
        }
    }

    fun resumeGame() {
        val cur = _state.value
        if (cur.status == PlayStatus.PAUSED) {
            _state.value = cur.copy(status = PlayStatus.RUNNING)
        }
    }

    fun revivePlayer(skillLevels: Map<SkillType, Int> = emptyMap()): Boolean {
        val cur = _state.value
        if (cur.status != PlayStatus.GAME_OVER || cur.reviveUsed || !cur.canRevive) return false

        val gw = cur.gridWidth
        val gh = cur.gridHeight
        val safeCenter = GridPos(gw / 2, gh / 2 + 3)
        val keepLength = cur.snake.size.coerceIn(4, 14)
        val rebuiltSnake = (0 until keepLength).map { idx ->
            GridPos(
                safeCenter.x,
                (safeCenter.y + idx).coerceAtMost(gh - 2)
            )
        }

        val shieldLevel = skillLevels[SkillType.SHIELD_DURATION] ?: 0
        val shieldTicks = (45 * (1f + shieldLevel * 0.25f)).roundToInt()

        directionQueue.clear()
        onSoundEffect(SoundEffect.POWER_UP)
        onVibrate(45L, true)

        _state.value = cur.copy(
            status = PlayStatus.RUNNING,
            snake = rebuiltSnake,
            direction = Direction.UP,
            reviveUsed = true,
            canRevive = false,
            invulnerableTicks = 35,
            activePowerUps = cur.activePowerUps + ActivePowerUp(
                type = PowerUpType.SHIELD,
                remainingTicks = shieldTicks,
                totalTicks = shieldTicks
            ),
            notificationBanner = "🛡️ SYSTEM REVIVED + HOLO SHIELD!",
            notificationColor = ElectricCyan
        )
        return true
    }

    /**
     * Computes dynamic step interval in milliseconds based on current evolution, mode, speed settings, and active power-ups.
     */
    fun computeStepIntervalMs(gameSpeedMultiplier: Float): Long {
        val s = _state.value
        // Base speed starts comfortable (145ms per grid cell) and scales gently with evolution & survival time
        var interval = 145f / gameSpeedMultiplier.coerceIn(0.6f, 1.6f)

        // Evolution speed bonus (subtle 4% per stage)
        interval *= (1f - (s.evolutionStage.stageNumber - 1) * 0.04f)

        // Survival mode gradually accelerates
        if (s.gameMode == GameMode.SURVIVAL) {
            val survivalFactor = (1f - (s.elapsedSeconds / 180f) * 0.30f).coerceAtLeast(0.65f)
            interval *= survivalFactor
        }

        // Active power-ups or toxic speed surge
        if (s.hasPowerUp(PowerUpType.SPEED_BOOST) || s.speedPoisonTicks > 0) {
            interval *= 0.72f
        }
        if (s.hasPowerUp(PowerUpType.SLOW_MOTION)) {
            interval *= 1.38f
        }

        return interval.roundToInt().toLong().coerceIn(65L, 240L)
    }

    /**
     * Advances 1 second timer when game is RUNNING (called by ViewModel ticker).
     */
    fun onSecondTick() {
        val cur = _state.value
        if (cur.status != PlayStatus.RUNNING) return

        val newElapsed = cur.elapsedSeconds + 1
        var newTimeRem = cur.timeRemainingSeconds
        if (newTimeRem != null) {
            newTimeRem = (newTimeRem - 1).coerceAtLeast(0)
            if (newTimeRem == 0) {
                // Time Attack finished!
                onSoundEffect(SoundEffect.BOSS_DEFEATED)
                val finishedState = cur.copy(
                    elapsedSeconds = newElapsed,
                    timeRemainingSeconds = 0,
                    status = PlayStatus.VICTORY,
                    notificationBanner = "⏱️ TIME UP! FINAL SCORE: ${cur.score}",
                    notificationColor = GoldenEnergy
                )
                _state.value = finishedState
                onMatchFinished(finishedState)
                return
            }
        }

        // In Survival mode, spawn an extra dynamic hazard every 20 seconds (up to +8 hazards)
        var updatedObstacles = cur.obstacles
        var banner = cur.notificationBanner
        var bannerColor = cur.notificationColor
        if (cur.gameMode == GameMode.SURVIVAL && newElapsed % 20 == 0 && cur.obstacles.size < 22) {
            val newObsPos = SpawnManager.findSafePosition(
                gridWidth = cur.gridWidth,
                gridHeight = cur.gridHeight,
                snake = cur.snake,
                obstacles = cur.obstacles,
                existingOrbs = cur.energyOrbs,
                existingPowerUps = cur.powerUpItems,
                bossState = cur.bossState
            )
            updatedObstacles = cur.obstacles + Obstacle(newObsPos)
            banner = "⚠️ NEW HAZARD DEPLOYED!"
            bannerColor = CyberOrange
        }

        // In Classic mode, spawn a surprise Boss encounter at 500 points if no boss active yet
        var updatedBoss = cur.bossState
        if (cur.gameMode == GameMode.CLASSIC && updatedBoss == null && cur.score >= 500 && cur.bossesDefeatedThisRun == 0) {
            updatedBoss = BossSystem.createBoss(BossType.PLASMA_CORE, cur.gridWidth)
            banner = "🚨 BOSS INCURSION: PLASMA CORE!"
            bannerColor = DangerRed
            onSoundEffect(SoundEffect.EVOLUTION_UP)
        }

        var nextState = cur.copy(
            elapsedSeconds = newElapsed,
            timeRemainingSeconds = newTimeRem,
            obstacles = updatedObstacles,
            bossState = updatedBoss,
            notificationBanner = banner,
            notificationColor = bannerColor
        )

        // Check survival mission completion
        nextState = checkMissionCompletion(nextState)
        _state.value = nextState
    }

    /**
     * Advances 1 discrete grid step of the game engine.
     */
    fun stepTick(
        skillLevels: Map<SkillType, Int> = emptyMap(),
        reducedVisualEffects: Boolean = false
    ) {
        val cur = _state.value
        if (cur.status != PlayStatus.RUNNING) return

        val nextTick = cur.tickCount + 1
        val nextDir = if (directionQueue.isNotEmpty()) {
            val polled = directionQueue.removeFirst()
            if (!polled.isOpposite(cur.direction)) polled else cur.direction
        } else {
            cur.direction
        }

        val head = cur.snake.first()
        val rawNextPos = head.move(nextDir)

        val ghostActive = cur.hasPowerUp(PowerUpType.GHOST_MODE)
        val allowWrap = cur.arena.allowWrapWalls || ghostActive

        val (nextHeadPos, hitWall) = CollisionSystem.wrapOrCheckWall(
            nextPos = rawNextPos,
            gridWidth = cur.gridWidth,
            gridHeight = cur.gridHeight,
            allowWrap = allowWrap
        )

        // Move dynamic obstacles first
        val movedObstacles = cur.obstacles.map { obs ->
            if (obs.velocity != null && nextTick % obs.moveEveryTicks == 0) {
                val candidate = obs.pos.move(obs.velocity)
                if (candidate.x in 2 until (cur.gridWidth - 2) &&
                    candidate.y in 4 until (cur.gridHeight - 4) &&
                    candidate !in cur.snake
                ) {
                    obs.copy(pos = candidate)
                } else {
                    val reversed = when (obs.velocity) {
                        Direction.LEFT -> Direction.RIGHT
                        Direction.RIGHT -> Direction.LEFT
                        Direction.UP -> Direction.DOWN
                        Direction.DOWN -> Direction.UP
                    }
                    obs.copy(velocity = reversed)
                }
            } else {
                obs
            }
        }

        // Update Boss state
        val updatedBoss = cur.bossState?.let {
            BossSystem.updateBossTick(it, cur.gridWidth, cur.gridHeight, nextHeadPos)
        }

        // Check collisions (Wall, Self, Obstacle, Boss Hazard)
        val hitSelf = !ghostActive &&
                cur.invulnerableTicks <= 0 &&
                CollisionSystem.checkSelfCollision(nextHeadPos, cur.snake.dropLast(1))

        val hitObstacleObj = if (!ghostActive && cur.invulnerableTicks <= 0) {
            CollisionSystem.checkObstacleCollision(nextHeadPos, movedObstacles)
        } else null

        val hitBossHazard = !ghostActive &&
                cur.invulnerableTicks <= 0 &&
                CollisionSystem.checkBossHazardCollision(nextHeadPos, updatedBoss)

        if (hitWall || hitSelf || hitObstacleObj != null || hitBossHazard) {
            // Check if Shield is active!
            if (cur.hasPowerUp(PowerUpType.SHIELD)) {
                onSoundEffect(SoundEffect.POWER_UP)
                onVibrate(40L, false)
                val remainingPowerUps = cur.activePowerUps.filterNot { it.type == PowerUpType.SHIELD }
                val destroyedObstacles = if (hitObstacleObj != null) {
                    movedObstacles - hitObstacleObj
                } else movedObstacles

                // If wall hit without wrap, clamp inside arena and bounce direction safely
                val safeHead = if (hitWall) {
                    GridPos(
                        nextHeadPos.x.coerceIn(0, cur.gridWidth - 1),
                        nextHeadPos.y.coerceIn(0, cur.gridHeight - 1)
                    )
                } else nextHeadPos

                val shieldParticles = if (!reducedVisualEffects) {
                    createBurstParticles(safeHead, ElectricCyan, 14)
                } else emptyList()

                _state.value = cur.copy(
                    direction = nextDir,
                    activePowerUps = remainingPowerUps,
                    obstacles = destroyedObstacles,
                    invulnerableTicks = 12,
                    notificationBanner = "🛡️ SHIELD ABSORBED IMPACT!",
                    notificationColor = ElectricCyan,
                    particles = (cur.particles + shieldParticles).takeLast(60),
                    tickCount = nextTick
                )
                return
            } else {
                // Trigger Game Over
                onSoundEffect(SoundEffect.GAME_OVER)
                onVibrate(90L, true)
                val crashParticles = if (!reducedVisualEffects) {
                    createBurstParticles(head, DangerRed, 22)
                } else emptyList()
                val overState = cur.copy(
                    status = PlayStatus.GAME_OVER,
                    particles = (cur.particles + crashParticles).takeLast(60),
                    notificationBanner = "💥 CRITICAL COLLISION!",
                    notificationColor = DangerRed,
                    tickCount = nextTick
                )
                _state.value = overState
                onMatchFinished(overState)
                return
            }
        }

        // Magnet Attraction Logic
        val magnetSkillLevel = skillLevels[SkillType.MAGNET_RANGE] ?: 0
        val magnetActive = cur.hasPowerUp(PowerUpType.ENERGY_MAGNET)
        val passiveMagnetRadius = when {
            magnetActive -> 4 + (magnetSkillLevel / 2)
            cur.evolutionStage.stageNumber >= 5 -> 2
            cur.evolutionStage.stageNumber >= 3 -> 1
            else -> 0
        }

        val pulledOrbs = cur.energyOrbs.map { orb ->
            val dist = abs(orb.pos.x - nextHeadPos.x) + abs(orb.pos.y - nextHeadPos.y)
            if (passiveMagnetRadius > 0 && dist in 1..passiveMagnetRadius) {
                val stepX = (nextHeadPos.x - orb.pos.x).coerceIn(-1, 1)
                val stepY = (nextHeadPos.y - orb.pos.y).coerceIn(-1, 1)
                orb.copy(pos = GridPos(orb.pos.x + stepX, orb.pos.y + stepY))
            } else {
                orb
            }
        }

        // Check Energy Orb Pickup
        val collectedOrb = pulledOrbs.find { it.pos == nextHeadPos }
        var remainingOrbs = pulledOrbs.filterNot { it.id == collectedOrb?.id }

        // Expire temporary orbs & maintain minimum 2 orbs on board
        remainingOrbs = remainingOrbs.filter { orb ->
            val maxLife = orb.maxLifetimeTicks
            maxLife == null || (nextTick - orb.spawnTick) < maxLife
        }.toMutableList()

        var newScore = cur.score
        var newCoins = cur.coinsEarnedThisRun
        var newOrbsCount = cur.orbsCollectedThisRun
        var newGoldenCount = cur.goldenOrbsThisRun
        var newEvoXp = cur.evolutionXp
        var newCombo = cur.comboCount
        var newComboRem = (cur.comboRemainingTicks - 1).coerceAtLeast(0)
        if (newComboRem == 0 && collectedOrb == null) {
            newCombo = 0
        }
        var newComboBanner: String? = if (newComboRem > 0) cur.comboBannerText else null
        var newBanner = cur.notificationBanner
        var newBannerColor = cur.notificationColor
        var newSpeedPoisonTicks = (cur.speedPoisonTicks - 1).coerceAtLeast(0)
        var newParticles = updateParticles(cur.particles)
        var newFloatingTexts = updateFloatingTexts(cur.floatingTexts)
        var currentBoss = updatedBoss
        var bossesDefeated = cur.bossesDefeatedThisRun

        if (collectedOrb != null) {
            newOrbsCount += 1
            if (collectedOrb.type == EnergyType.GOLDEN) {
                newGoldenCount += 1
            }

            // Combo calculation
            newCombo = if (cur.comboRemainingTicks > 0 || cur.comboCount == 0) cur.comboCount + 1 else 1
            newComboRem = cur.maxComboWindowTicks

            val comboMult = when {
                newCombo >= 15 -> 5.0f
                newCombo >= 10 -> 3.0f
                newCombo >= 5 -> 2.0f
                newCombo >= 3 -> 1.5f
                else -> 1.0f
            }

            when {
                newCombo == 15 -> {
                    newComboBanner = "🔥 ULTRA COMBO x5!"
                    onSoundEffect(SoundEffect.COMBO_MILESTONE)
                }
                newCombo == 10 -> {
                    newComboBanner = "⚡ MEGA COMBO x3!"
                    onSoundEffect(SoundEffect.COMBO_MILESTONE)
                }
                newCombo == 5 -> {
                    newComboBanner = "✨ SUPER COMBO x2!"
                    onSoundEffect(SoundEffect.COMBO_MILESTONE)
                }
                newCombo == 3 -> {
                    newComboBanner = "COMBO x1.5!"
                }
            }

            // Skill & Evolution multipliers
            val energySkillLvl = skillLevels[SkillType.ENERGY_BOOST] ?: 0
            val scoreSkillLvl = skillLevels[SkillType.SCORE_MULTIPLIER] ?: 0
            val coinSkillLvl = skillLevels[SkillType.COIN_BONUS] ?: 0
            val evoSkillLvl = skillLevels[SkillType.EVOLUTION_BONUS] ?: 0

            val doubleScoreFactor = if (cur.hasPowerUp(PowerUpType.DOUBLE_SCORE)) 2.0f else 1.0f
            val totalScoreMult = comboMult *
                    cur.evolutionStage.scoreBonusMultiplier *
                    (1f + energySkillLvl * 0.10f) *
                    (1f + scoreSkillLvl * 0.10f) *
                    doubleScoreFactor

            val pointsAwarded = (collectedOrb.type.basePoints * totalScoreMult).roundToInt()
            newScore += pointsAwarded

            val rawCoins = collectedOrb.type.coinsReward + cur.evolutionStage.coinBonusAdd
            val coinsAwarded = (rawCoins * (1f + coinSkillLvl * 0.15f)).roundToInt()
            newCoins += coinsAwarded

            val xpAwarded = (collectedOrb.type.evolutionXp * (1f + evoSkillLvl * 0.15f)).roundToInt()
            newEvoXp += xpAwarded

            // Special Energy Orb behavior
            when (collectedOrb.type) {
                EnergyType.NORMAL, EnergyType.PLASMA -> {
                    onSoundEffect(SoundEffect.ORB_PICKUP)
                    onVibrate(18L, false)
                }
                EnergyType.GOLDEN -> {
                    onSoundEffect(SoundEffect.GOLDEN_ORB)
                    onVibrate(28L, false)
                    newBanner = "🪙 GOLDEN ENERGY! +$pointsAwarded PTS"
                    newBannerColor = GoldenEnergy
                }
                EnergyType.TOXIC -> {
                    // High Risk & High Reward: Big points + triggers brief adrenaline speed surge!
                    onSoundEffect(SoundEffect.TOXIC_HIT)
                    onVibrate(35L, true)
                    newSpeedPoisonTicks = 22
                    newBanner = "☣️ TOXIC RISK PAYOUT! +$pointsAwarded (ADRENALINE SURGE)"
                    newBannerColor = ToxicGreen
                }
                EnergyType.BONUS -> {
                    onSoundEffect(SoundEffect.GOLDEN_ORB)
                    onVibrate(28L, false)
                    val bonusCoins = 12
                    newCoins += bonusCoins
                    newBanner = "🎁 MYSTERY JACKPOT! +$bonusCoins COINS"
                    newBannerColor = NeonMagenta
                }
            }

            if (!reducedVisualEffects) {
                newParticles = (newParticles + createBurstParticles(nextHeadPos, collectedOrb.type.color, 12)).takeLast(60)
            }
            newFloatingTexts = (newFloatingTexts + FloatingText(
                id = floatingTextIdCounter++,
                text = "+$pointsAwarded",
                gridPos = nextHeadPos,
                color = collectedOrb.type.color
            )).takeLast(10)

            // If Boss is active, collecting energy fires a Plasma Strike at the Boss Core!
            if (currentBoss != null) {
                val damage = when (collectedOrb.type) {
                    EnergyType.GOLDEN, EnergyType.PLASMA -> 28
                    EnergyType.TOXIC, EnergyType.BONUS -> 32
                    EnergyType.NORMAL -> 18
                }
                val nextHp = (currentBoss.currentHp - damage).coerceAtLeast(0)
                if (nextHp == 0) {
                    // Boss Defeated!
                    bossesDefeated += 1
                    val bossReward = currentBoss.boss.coinReward
                    newCoins += bossReward
                    newScore += 350
                    onSoundEffect(SoundEffect.BOSS_DEFEATED)
                    onVibrate(80L, true)
                    if (!reducedVisualEffects) {
                        currentBoss.corePositions.forEach { cp ->
                            newParticles = (newParticles + createBurstParticles(cp, GoldenEnergy, 15)).takeLast(70)
                        }
                    }
                    newBanner = "🏆 ${currentBoss.boss.nameEn} DEFEATED! +$bossReward COINS"
                    newBannerColor = GoldenEnergy
                    currentBoss = null
                } else {
                    onSoundEffect(SoundEffect.BOSS_HIT)
                    currentBoss = currentBoss.copy(
                        currentHp = nextHp,
                        hitFlashTicks = 5
                    )
                }
            }
        }

        // Check Evolution Stage Upgrade
        val newEvoStage = EvolutionStage.fromXp(newEvoXp)
        if (newEvoStage.stageNumber > cur.evolutionStage.stageNumber) {
            onSoundEffect(SoundEffect.EVOLUTION_UP)
            onVibrate(55L, true)
            newBanner = "🧬 EVOLVED TO ${newEvoStage.nameEn.uppercase()}!"
            newBannerColor = newEvoStage.headColor
            newCoins += 25 * newEvoStage.stageNumber
            if (!reducedVisualEffects) {
                newParticles = (newParticles + createBurstParticles(nextHeadPos, newEvoStage.headColor, 20)).takeLast(70)
            }
        }

        // Update snake body segments
        val updatedSnake = buildList {
            add(nextHeadPos)
            if (collectedOrb != null) {
                // Grow by 1 segment
                addAll(cur.snake)
            } else {
                addAll(cur.snake.dropLast(1))
            }
        }

        // Check Power-up Item Pickup
        val pickedPowerUp = cur.powerUpItems.find { it.pos == nextHeadPos }
        var remainingPowerUpItems = cur.powerUpItems
            .filterNot { it.id == pickedPowerUp?.id }
            .filter { (nextTick - it.spawnTick) < it.maxLifetimeTicks }

        var activePowerUpsList = cur.activePowerUps.mapNotNull { active ->
            val rem = active.remainingTicks - 1
            if (rem > 0) active.copy(remainingTicks = rem) else null
        }.toMutableList()

        var newPowerUpsCount = cur.powerUpsCollectedThisRun

        if (pickedPowerUp != null) {
            newPowerUpsCount += 1
            onSoundEffect(SoundEffect.POWER_UP)
            onVibrate(30L, false)

            val shieldSkillLvl = skillLevels[SkillType.SHIELD_DURATION] ?: 0
            val magnetSkillLvl = skillLevels[SkillType.MAGNET_RANGE] ?: 0

            val durationMult = when (pickedPowerUp.type) {
                PowerUpType.SHIELD, PowerUpType.GHOST_MODE -> 1f + shieldSkillLvl * 0.25f
                PowerUpType.ENERGY_MAGNET -> 1f + magnetSkillLvl * 0.20f
                else -> 1f
            }
            val totalDuration = (pickedPowerUp.type.baseDurationTicks * durationMult).roundToInt()

            if (pickedPowerUp.type == PowerUpType.BONUS_ORB) {
                newScore += 180
                newCoins += 20
                newBanner = "💎 INSTANT JACKPOT! +180 PTS & +20 COINS"
                newBannerColor = GoldenEnergy
            } else {
                activePowerUpsList.removeAll { it.type == pickedPowerUp.type }
                activePowerUpsList.add(
                    ActivePowerUp(
                        type = pickedPowerUp.type,
                        remainingTicks = totalDuration,
                        totalTicks = totalDuration
                    )
                )
                newBanner = "${pickedPowerUp.type.symbol} ${pickedPowerUp.type.titleEn.uppercase()} ACTIVE!"
                newBannerColor = pickedPowerUp.type.color
            }

            if (!reducedVisualEffects) {
                newParticles = (newParticles + createBurstParticles(nextHeadPos, pickedPowerUp.type.color, 14)).takeLast(65)
            }
        }

        // Ensure minimum Energy Orbs on the arena
        val targetOrbCount = if (cur.gameMode == GameMode.TIME_ATTACK) 3 else 2
        val mutableOrbs = remainingOrbs.toMutableList()
        while (mutableOrbs.size < targetOrbCount) {
            val forceNormal = mutableOrbs.none { it.type == EnergyType.NORMAL }
            mutableOrbs.add(
                SpawnManager.spawnEnergyOrb(
                    gridWidth = cur.gridWidth,
                    gridHeight = cur.gridHeight,
                    snake = updatedSnake,
                    obstacles = movedObstacles,
                    existingOrbs = mutableOrbs,
                    existingPowerUps = remainingPowerUpItems,
                    bossState = currentBoss,
                    currentTick = nextTick,
                    forceType = if (forceNormal) EnergyType.NORMAL else null
                )
            )
        }

        // Periodically spawn a special risk/bonus orb or PowerUp item
        if (nextTick % 42 == 0 && mutableOrbs.size < 4) {
            mutableOrbs.add(
                SpawnManager.spawnEnergyOrb(
                    gridWidth = cur.gridWidth,
                    gridHeight = cur.gridHeight,
                    snake = updatedSnake,
                    obstacles = movedObstacles,
                    existingOrbs = mutableOrbs,
                    existingPowerUps = remainingPowerUpItems,
                    bossState = currentBoss,
                    currentTick = nextTick
                )
            )
        }

        if (nextTick % 55 == 0 && remainingPowerUpItems.isEmpty()) {
            remainingPowerUpItems = remainingPowerUpItems + SpawnManager.spawnPowerUp(
                gridWidth = cur.gridWidth,
                gridHeight = cur.gridHeight,
                snake = updatedSnake,
                obstacles = movedObstacles,
                existingOrbs = mutableOrbs,
                existingPowerUps = remainingPowerUpItems,
                bossState = currentBoss,
                currentTick = nextTick
            )
        }

        val currentMult = when {
            newCombo >= 15 -> 5.0f
            newCombo >= 10 -> 3.0f
            newCombo >= 5 -> 2.0f
            newCombo >= 3 -> 1.5f
            else -> 1.0f
        }

        // Check if Boss Challenge mode was won
        var nextStatus = cur.status
        if (cur.gameMode == GameMode.BOSS_CHALLENGE && currentBoss == null && bossesDefeated > 0) {
            nextStatus = PlayStatus.VICTORY
        }

        var nextState = cur.copy(
            status = nextStatus,
            snake = updatedSnake,
            direction = nextDir,
            score = newScore,
            bestScore = maxOf(cur.bestScore, newScore),
            coinsEarnedThisRun = newCoins,
            orbsCollectedThisRun = newOrbsCount,
            goldenOrbsThisRun = newGoldenCount,
            powerUpsCollectedThisRun = newPowerUpsCount,
            evolutionXp = newEvoXp,
            evolutionStage = newEvoStage,
            comboCount = newCombo,
            maxComboThisRun = maxOf(cur.maxComboThisRun, newCombo),
            comboMultiplier = currentMult,
            comboBannerText = newComboBanner,
            comboRemainingTicks = newComboRem,
            energyOrbs = mutableOrbs,
            powerUpItems = remainingPowerUpItems,
            activePowerUps = activePowerUpsList,
            obstacles = movedObstacles,
            bossState = currentBoss,
            bossesDefeatedThisRun = bossesDefeated,
            invulnerableTicks = (cur.invulnerableTicks - 1).coerceAtLeast(0),
            speedPoisonTicks = newSpeedPoisonTicks,
            notificationBanner = newBanner,
            notificationColor = newBannerColor,
            particles = newParticles,
            floatingTexts = newFloatingTexts,
            tickCount = nextTick
        )

        nextState = checkMissionCompletion(nextState)
        _state.value = nextState

        if (nextState.status == PlayStatus.VICTORY) {
            onMatchFinished(nextState)
        }
    }

    private fun checkMissionCompletion(state: GameEngineState): GameEngineState {
        val mission = state.activeMission ?: return state
        if (state.status != PlayStatus.RUNNING) return state

        if (state.missionProgressCurrent >= mission.targetValue) {
            onSoundEffect(SoundEffect.BOSS_DEFEATED)
            onVibrate(70L, true)
            val completedState = state.copy(
                status = PlayStatus.VICTORY,
                coinsEarnedThisRun = state.coinsEarnedThisRun + mission.rewardCoins,
                notificationBanner = "🎯 MISSION COMPLETE! +${mission.rewardCoins} COINS",
                notificationColor = GoldenEnergy
            )
            onMatchFinished(completedState)
            return completedState
        }
        return state
    }

    private fun createBurstParticles(pos: GridPos, color: Color, count: Int): List<Particle> {
        return List(count) { idx ->
            val angle = (idx.toFloat() / count) * (Math.PI * 2.0).toFloat() + Random.nextFloat() * 0.4f
            val speed = 0.08f + Random.nextFloat() * 0.22f
            Particle(
                x = pos.x + 0.5f,
                y = pos.y + 0.5f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                color = color,
                alpha = 1.0f,
                size = 3.5f + Random.nextFloat() * 3.5f
            )
        }
    }

    private fun updateParticles(particles: List<Particle>): List<Particle> {
        return particles.mapNotNull { p ->
            val nextAlpha = p.alpha - 0.14f
            if (nextAlpha <= 0.05f) null
            else p.copy(
                x = p.x + p.vx,
                y = p.y + p.vy,
                alpha = nextAlpha
            )
        }
    }

    private fun updateFloatingTexts(texts: List<FloatingText>): List<FloatingText> {
        return texts.mapNotNull { ft ->
            val nextProg = ft.progress + 0.14f
            if (nextProg >= 1.0f) null
            else ft.copy(progress = nextProg)
        }
    }
}
