package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.models.AppLanguage
import com.example.domain.models.GameMode
import com.example.ui.AppScreen
import com.example.ui.SnakeViewModel
import com.example.ui.components.tr
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnakeEvolutionTheme {
                SnakeEvolutionApp()
            }
        }
    }
}

@Composable
fun SnakeEvolutionApp(viewModel: SnakeViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val playerData by viewModel.playerData.collectAsStateWithLifecycle()
    val engineState by viewModel.engineState.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val selectedBoss by viewModel.selectedBoss.collectAsStateWithLifecycle()
    val unlockedAchievement by viewModel.recentUnlockedAchievement.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.onAppForegrounded()
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> viewModel.onAppBackgrounded()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Auto-dismiss achievement celebration banner after 4 seconds
    LaunchedEffect(unlockedAchievement) {
        if (unlockedAchievement != null) {
            delay(4000L)
            viewModel.clearAchievementToast()
        }
    }

    val layoutDir = if (playerData.settings.language == AppLanguage.AR) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                AppScreen.MAIN_MENU -> MainMenuScreen(
                    playerData = playerData,
                    selectedMode = selectedMode,
                    onPlayClick = { viewModel.startGame(selectedMode) },
                    onNavigate = { viewModel.navigateTo(it) },
                    onToggleLanguage = {
                        viewModel.updateSettings { s ->
                            val nextLang = if (s.language == AppLanguage.AR) AppLanguage.EN else AppLanguage.AR
                            s.copy(language = nextLang)
                        }
                    }
                )
                AppScreen.GAMEPLAY -> GameplayScreen(
                    engineState = engineState,
                    playerData = playerData,
                    onDirectionInput = { viewModel.onDirectionInput(it) },
                    onPause = { viewModel.pauseGame() },
                    onResume = { viewModel.resumeGame() },
                    onRestart = { viewModel.restartCurrentMatch() },
                    onRevive = { viewModel.revivePlayer() },
                    onMainMenu = { viewModel.navigateBackToMenu() },
                    onDismissTutorial = { viewModel.dismissTutorial() }
                )
                AppScreen.GAME_MODES -> GameModesScreen(
                    playerData = playerData,
                    selectedMode = selectedMode,
                    selectedBoss = selectedBoss,
                    onSelectMode = { viewModel.selectGameMode(it) },
                    onSelectArena = { viewModel.onArenaAction(it) },
                    onSelectBoss = { viewModel.selectBossType(it) },
                    onStartMode = { mode ->
                        if (mode == GameMode.MISSION) {
                            viewModel.navigateTo(AppScreen.MISSIONS)
                        } else {
                            viewModel.startGame(mode = mode, boss = selectedBoss)
                        }
                    },
                    onBack = { viewModel.navigateBackToMenu() }
                )
                AppScreen.MISSIONS -> MissionsScreen(
                    playerData = playerData,
                    onStartMission = { mission ->
                        viewModel.startGame(mode = GameMode.MISSION, mission = mission)
                    },
                    onStartBoss = { boss ->
                        viewModel.selectBossType(boss)
                        viewModel.startGame(mode = GameMode.BOSS_CHALLENGE, boss = boss)
                    },
                    onBack = { viewModel.navigateBackToMenu() }
                )
                AppScreen.SKINS -> SkinsScreen(
                    playerData = playerData,
                    onSkinAction = { viewModel.onSkinAction(it) },
                    onBack = { viewModel.navigateBackToMenu() }
                )
                AppScreen.SKILL_TREE -> SkillTreeScreen(
                    playerData = playerData,
                    onUpgradeSkill = { viewModel.onUpgradeSkill(it) },
                    onBack = { viewModel.navigateBackToMenu() }
                )
                AppScreen.DAILY_CHALLENGES -> DailyChallengesScreen(
                    playerData = playerData,
                    onPlayNow = { viewModel.startGame(GameMode.CLASSIC) },
                    onBack = { viewModel.navigateBackToMenu() }
                )
                AppScreen.ACHIEVEMENTS -> AchievementsScreen(
                    playerData = playerData,
                    onBack = { viewModel.navigateBackToMenu() }
                )
                AppScreen.STATISTICS -> StatisticsScreen(
                    playerData = playerData,
                    onBack = { viewModel.navigateBackToMenu() }
                )
                AppScreen.SETTINGS -> SettingsScreen(
                    playerData = playerData,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onResetProgress = { viewModel.resetProgress() },
                    onBack = { viewModel.navigateBackToMenu() }
                )
            }

            // Achievement Unlocked Celebration Banner
            AnimatedVisibility(
                visible = unlockedAchievement != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp)
            ) {
                unlockedAchievement?.let { ach ->
                    val lang = playerData.settings.language
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.clearAchievementToast() },
                        shape = RoundedCornerShape(16.dp),
                        color = GlassSurface,
                        border = BorderStroke(2.dp, GoldenEnergy),
                        shadowElevation = 10.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = ach.icon, style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tr(lang, "ACHIEVEMENT UNLOCKED!", "تم فتح إنجاز جديد!"),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GoldenEnergy
                                )
                                Text(
                                    text = tr(lang, ach.titleEn, ach.titleAr),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = NeonGreen
                                )
                            }
                            Text(
                                text = "+${ach.rewardCoins} 🪙",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = GoldenEnergy
                            )
                        }
                    }
                }
            }
        }
    }
}
