package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffect
import com.example.audio.SoundManager
import com.example.data.preferences.GamePreferencesRepository
import com.example.data.preferences.PersistedPlayerData
import com.example.domain.models.*
import com.example.game.engine.GameEngine
import com.example.game.engine.GameEngineState
import com.example.game.engine.PlayStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN_MENU,
    GAMEPLAY,
    GAME_MODES,
    MISSIONS,
    SKINS,
    SKILL_TREE,
    ACHIEVEMENTS,
    DAILY_CHALLENGES,
    STATISTICS,
    SETTINGS
}

class SnakeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamePreferencesRepository(application.applicationContext)
    val soundManager = SoundManager(application.applicationContext)

    private val _currentScreen = MutableStateFlow(AppScreen.MAIN_MENU)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val playerData: StateFlow<PersistedPlayerData> = repository.playerDataFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PersistedPlayerData()
    )

    private val _selectedMode = MutableStateFlow(GameMode.CLASSIC)
    val selectedMode: StateFlow<GameMode> = _selectedMode.asStateFlow()

    private val _selectedBoss = MutableStateFlow(BossType.PLASMA_CORE)
    val selectedBoss: StateFlow<BossType> = _selectedBoss.asStateFlow()

    private val _recentUnlockedAchievement = MutableStateFlow<AchievementDef?>(null)
    val recentUnlockedAchievement: StateFlow<AchievementDef?> = _recentUnlockedAchievement.asStateFlow()

    val gameEngine = GameEngine(
        onSoundEffect = { effect -> soundManager.playSfx(effect) },
        onVibrate = { ms, heavy -> soundManager.vibrate(ms, heavy) },
        onMatchFinished = { finalState -> handleMatchFinished(finalState) }
    )

    val engineState: StateFlow<GameEngineState> = gameEngine.state

    private var gameLoopJob: Job? = null
    private var secondTimerJob: Job? = null

    init {
        viewModelScope.launch {
            playerData.collect { data ->
                soundManager.musicVolume = data.settings.musicVolume
                soundManager.sfxVolume = data.settings.sfxVolume
                soundManager.vibrationEnabled = data.settings.vibrationEnabled
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        soundManager.playSfx(SoundEffect.BUTTON_CLICK)
        _currentScreen.value = screen
    }

    fun navigateBackToMenu() {
        soundManager.playSfx(SoundEffect.BUTTON_CLICK)
        stopLoops()
        _currentScreen.value = AppScreen.MAIN_MENU
    }

    fun selectGameMode(mode: GameMode) {
        soundManager.playSfx(SoundEffect.BUTTON_CLICK)
        _selectedMode.value = mode
    }

    fun selectBossType(boss: BossType) {
        soundManager.playSfx(SoundEffect.BUTTON_CLICK)
        _selectedBoss.value = boss
    }

    fun startGame(
        mode: GameMode = _selectedMode.value,
        mission: Mission? = null,
        boss: BossType = _selectedBoss.value
    ) {
        soundManager.playSfx(SoundEffect.BUTTON_CLICK)
        _selectedMode.value = mode
        val data = playerData.value
        val arena = ArenaLevel.entries.find { it.id == data.selectedArenaId } ?: ArenaLevel.NEON_CITY

        gameEngine.startNewGame(
            mode = mode,
            arena = arena,
            mission = mission,
            selectedBoss = boss,
            bestScore = data.stats.bestScore,
            reviveAllowedBySettings = data.settings.reviveEnabled,
            skillLevels = data.skillLevels
        )

        _currentScreen.value = AppScreen.GAMEPLAY
        startLoops()
    }

    fun restartCurrentMatch() {
        val current = engineState.value
        startGame(
            mode = current.gameMode,
            mission = current.activeMission,
            boss = _selectedBoss.value
        )
    }

    fun onDirectionInput(direction: Direction) {
        gameEngine.queueDirection(direction)
    }

    fun pauseGame() {
        gameEngine.pauseGame()
    }

    fun resumeGame() {
        soundManager.playSfx(SoundEffect.BUTTON_CLICK)
        gameEngine.resumeGame()
    }

    fun revivePlayer() {
        val revived = gameEngine.revivePlayer(playerData.value.skillLevels)
        if (revived) {
            startLoops()
        }
    }

    fun dismissTutorial() {
        viewModelScope.launch {
            repository.updateSettings { it.copy(tutorialSeen = true) }
        }
    }

    fun clearAchievementToast() {
        _recentUnlockedAchievement.value = null
    }

    // Shop / Skins / Arena / Skills actions
    fun onSkinAction(skin: SnakeSkin) {
        viewModelScope.launch {
            val data = playerData.value
            if (skin.id in data.unlockedSkins || skin.cost == 0) {
                soundManager.playSfx(SoundEffect.BUTTON_CLICK)
                repository.selectSkin(skin)
            } else {
                val unlocked = repository.unlockAndSelectSkin(skin)
                if (unlocked) {
                    soundManager.playSfx(SoundEffect.EVOLUTION_UP)
                    soundManager.vibrate(40L, false)
                } else {
                    soundManager.playSfx(SoundEffect.TOXIC_HIT)
                }
            }
        }
    }

    fun onArenaAction(arena: ArenaLevel) {
        viewModelScope.launch {
            val data = playerData.value
            if (arena.id in data.unlockedArenas || arena.unlockCost == 0) {
                soundManager.playSfx(SoundEffect.BUTTON_CLICK)
                repository.selectArena(arena)
            } else {
                val unlocked = repository.unlockAndSelectArena(arena)
                if (unlocked) {
                    soundManager.playSfx(SoundEffect.EVOLUTION_UP)
                    soundManager.vibrate(40L, false)
                } else {
                    soundManager.playSfx(SoundEffect.TOXIC_HIT)
                }
            }
        }
    }

    fun onUpgradeSkill(skill: SkillType) {
        viewModelScope.launch {
            val ok = repository.upgradeSkill(skill)
            if (ok) {
                soundManager.playSfx(SoundEffect.POWER_UP)
                soundManager.vibrate(35L, false)
            } else {
                soundManager.playSfx(SoundEffect.TOXIC_HIT)
            }
        }
    }

    fun updateSettings(transform: (GameSettings) -> GameSettings) {
        viewModelScope.launch {
            repository.updateSettings(transform)
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            soundManager.playSfx(SoundEffect.TOXIC_HIT)
            repository.resetAllProgress()
        }
    }

    fun onAppBackgrounded() {
        if (_currentScreen.value == AppScreen.GAMEPLAY && engineState.value.status == PlayStatus.RUNNING) {
            gameEngine.pauseGame()
        }
        soundManager.stopMusic()
    }

    fun onAppForegrounded() {
        if (playerData.value.settings.musicVolume > 0.02f) {
            soundManager.startMusic()
        }
    }

    private fun startLoops() {
        stopLoops()
        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                val status = engineState.value.status
                if (status == PlayStatus.RUNNING) {
                    val data = playerData.value
                    gameEngine.stepTick(
                        skillLevels = data.skillLevels,
                        reducedVisualEffects = data.settings.reducedVisualEffects
                    )
                    val delayMs = gameEngine.computeStepIntervalMs(data.settings.gameSpeedMultiplier)
                    delay(delayMs)
                } else {
                    delay(80L)
                }
            }
        }

        secondTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                if (engineState.value.status == PlayStatus.RUNNING) {
                    gameEngine.onSecondTick()
                }
            }
        }
    }

    private fun stopLoops() {
        gameLoopJob?.cancel()
        gameLoopJob = null
        secondTimerJob?.cancel()
        secondTimerJob = null
    }

    private fun handleMatchFinished(finalState: GameEngineState) {
        viewModelScope.launch {
            val missionCompletedId = if (finalState.status == PlayStatus.VICTORY && finalState.activeMission != null) {
                finalState.activeMission.id
            } else null

            val newlyUnlocked = repository.recordMatchResult(
                score = finalState.score,
                coinsEarnedInRun = finalState.coinsEarnedThisRun,
                orbsCollected = finalState.orbsCollectedThisRun,
                goldenOrbsCollected = finalState.goldenOrbsThisRun,
                maxLength = finalState.snake.size,
                survivalSeconds = finalState.elapsedSeconds,
                maxCombo = finalState.maxComboThisRun,
                evolutionStageReached = finalState.evolutionStage,
                bossDefeatedCount = finalState.bossesDefeatedThisRun,
                missionCompletedId = missionCompletedId
            )

            if (newlyUnlocked.isNotEmpty()) {
                soundManager.playSfx(SoundEffect.ACHIEVEMENT_UNLOCK)
                _recentUnlockedAchievement.value = newlyUnlocked.first()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLoops()
        soundManager.release()
    }
}
