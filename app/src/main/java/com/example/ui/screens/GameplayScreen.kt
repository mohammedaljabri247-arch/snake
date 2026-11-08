package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.preferences.PersistedPlayerData
import com.example.domain.models.*
import com.example.game.engine.GameEngineState
import com.example.game.engine.PlayStatus
import com.example.game.renderer.GameRenderer.renderArena
import com.example.ui.components.NeonActionButton
import com.example.ui.components.NeonGlassCard
import com.example.ui.components.tr
import com.example.ui.theme.*
import kotlin.math.abs

@Composable
fun GameplayScreen(
    engineState: GameEngineState,
    playerData: PersistedPlayerData,
    onDirectionInput: (Direction) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onRevive: () -> Unit,
    onMainMenu: () -> Unit,
    onDismissTutorial: () -> Unit
) {
    val lang = playerData.settings.language
    val selectedSkin = SnakeSkin.fromId(playerData.selectedSkinId)
    val controlMode = playerData.settings.controlMode
    val swipeSensitivity = playerData.settings.swipeSensitivity

    BackHandler {
        if (engineState.status == PlayStatus.RUNNING) {
            onPause()
        } else {
            onMainMenu()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "arena_pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    // Minimum drag threshold scaled by user's swipeSensitivity (default 1.0f -> 26f px)
    val minSwipeDist = (28f / swipeSensitivity.coerceIn(0.5f, 1.6f))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // 1. Compact Top Game HUD
            GameHudHeader(
                engineState = engineState,
                lang = lang,
                onPause = onPause
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Boss Health Bar or Active Mission Progress Banner
            if (engineState.bossState != null) {
                BossHealthBanner(bossState = engineState.bossState, lang = lang)
                Spacer(modifier = Modifier.height(6.dp))
            } else if (engineState.activeMission != null) {
                MissionHudBanner(
                    mission = engineState.activeMission,
                    current = engineState.missionProgressCurrent,
                    lang = lang
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 3. Active Power-Ups & Combo Status Strip
            ActivePowerUpsAndComboRow(engineState = engineState, lang = lang)

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Primary 2D Neon Arena Canvas (with Swipe Gesture Detector)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("game_arena_canvas")
                    .pointerInput(controlMode, minSwipeDist) {
                        if (controlMode == ControlMode.SWIPE || controlMode == ControlMode.BOTH) {
                            var accX = 0f
                            var accY = 0f
                            detectDragGestures(
                                onDragStart = {
                                    accX = 0f
                                    accY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    accX += dragAmount.x
                                    accY += dragAmount.y
                                    if (abs(accX) > minSwipeDist || abs(accY) > minSwipeDist) {
                                        if (abs(accX) > abs(accY)) {
                                            onDirectionInput(if (accX > 0) Direction.RIGHT else Direction.LEFT)
                                        } else {
                                            onDirectionInput(if (accY > 0) Direction.DOWN else Direction.UP)
                                        }
                                        accX = 0f
                                        accY = 0f
                                    }
                                }
                            )
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    renderArena(
                        engineState = engineState,
                        selectedSkin = selectedSkin,
                        pulsePhase = pulsePhase,
                        reducedVisualEffects = playerData.settings.reducedVisualEffects
                    )
                }

                // Floating Notification / Evolution / Combo Banner inside top of Arena
                androidx.compose.animation.AnimatedVisibility(
                    visible = engineState.notificationBanner != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PureBlack.copy(alpha = 0.82f),
                        border = BorderStroke(1.2.dp, engineState.notificationColor)
                    ) {
                        Text(
                            text = engineState.notificationBanner ?: "",
                            style = MaterialTheme.typography.labelLarge,
                            color = engineState.notificationColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 5. Optional Ergonomic One-Handed D-Pad Controller
            if (controlMode == ControlMode.DPAD || controlMode == ControlMode.BOTH) {
                Spacer(modifier = Modifier.height(8.dp))
                ArcadeDPadControls(onDirectionInput = onDirectionInput)
            }
        }

        // 6. First-Launch Interactive Tutorial Overlay
        if (!playerData.settings.tutorialSeen && engineState.status == PlayStatus.RUNNING) {
            TutorialOverlayModal(lang = lang, onDismiss = onDismissTutorial)
        }

        // 7. Pause Overlay Modal
        if (engineState.status == PlayStatus.PAUSED) {
            PauseOverlayModal(
                engineState = engineState,
                lang = lang,
                onResume = onResume,
                onRestart = onRestart,
                onMainMenu = onMainMenu
            )
        }

        // 8. Game Over / Victory Summary Modal (with Revive System)
        if (engineState.status == PlayStatus.GAME_OVER || engineState.status == PlayStatus.VICTORY) {
            GameOverOrVictoryModal(
                engineState = engineState,
                lang = lang,
                onPlayAgain = onRestart,
                onRevive = onRevive,
                onMainMenu = onMainMenu
            )
        }
    }
}

@Composable
private fun GameHudHeader(
    engineState: GameEngineState,
    lang: AppLanguage,
    onPause: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GlassSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.2.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Score & Best
            Column {
                Text(
                    text = "${tr(lang, "SCORE", "النتيجة")}: ${engineState.score}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeonGreen
                )
                Text(
                    text = "${tr(lang, "BEST", "الأفضل")}: ${engineState.bestScore}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            // Evolution Stage & XP Progress
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = tr(lang, engineState.evolutionStage.nameEn, engineState.evolutionStage.nameAr),
                    style = MaterialTheme.typography.labelLarge,
                    color = engineState.evolutionStage.headColor
                )
                val timeDisplay = engineState.timeRemainingSeconds?.let { "⏱️ ${it}s" }
                    ?: "⚡ ${engineState.orbsCollectedThisRun} • 🪙 +${engineState.coinsEarnedThisRun}"
                Text(
                    text = timeDisplay,
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldenEnergy
                )
            }

            // Pause Button (48.dp minimum touch target)
            IconButton(
                onClick = onPause,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DarkNavy)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = ElectricCyan
                )
            }
        }
    }
}

@Composable
private fun BossHealthBanner(bossState: BossState, lang: AppLanguage) {
    val hpFraction = (bossState.currentHp.toFloat() / bossState.maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF280618).copy(alpha = 0.92f),
        border = BorderStroke(1.5.dp, DangerRed)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "👾 ${tr(lang, bossState.boss.nameEn, bossState.boss.nameAr)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = DangerRed
                )
                Text(
                    text = "${bossState.currentHp} / ${bossState.maxHp} HP",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldenEnergy
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { hpFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = DangerRed,
                trackColor = PureBlack
            )
        }
    }
}

@Composable
private fun MissionHudBanner(mission: Mission, current: Int, lang: AppLanguage) {
    val prog = (current.toFloat() / mission.targetValue.coerceAtLeast(1)).coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = DeepPurple.copy(alpha = 0.85f),
        border = BorderStroke(1.2.dp, PlasmaPurple)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎯 ${tr(lang, mission.descEn, mission.descAr)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$current / ${mission.targetValue}",
                style = MaterialTheme.typography.labelLarge,
                color = NeonGreen
            )
        }
    }
}

@Composable
private fun ActivePowerUpsAndComboRow(engineState: GameEngineState, lang: AppLanguage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Combo Indicator
        if (engineState.comboCount >= 2) {
            val comboLabel = when {
                engineState.comboCount >= 15 -> "MEGA COMBO x5"
                engineState.comboCount >= 10 -> "MEGA COMBO x3"
                engineState.comboCount >= 5 -> "SUPER COMBO x2"
                else -> "COMBO x${engineState.comboMultiplier}"
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = NeonMagenta.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, NeonMagenta)
            ) {
                Text(
                    text = "🔥 $comboLabel (${engineState.comboCount})",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonMagenta,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        } else {
            Text(
                text = "${tr(lang, "Arena", "الساحة")}: ${tr(lang, engineState.arena.titleEn, engineState.arena.titleAr)}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }

        // Active Power-Up Timers
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            engineState.activePowerUps.forEach { ap ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ap.type.color.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, ap.type.color)
                ) {
                    Text(
                        text = "${ap.type.symbol} ${ap.remainingTicks}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ap.type.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArcadeDPadControls(onDirectionInput: (Direction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DPadButton(
            icon = Icons.Default.KeyboardArrowLeft,
            desc = "Left",
            testTag = "dpad_left",
            onClick = { onDirectionInput(Direction.LEFT) }
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DPadButton(
                icon = Icons.Default.KeyboardArrowUp,
                desc = "Up",
                testTag = "dpad_up",
                onClick = { onDirectionInput(Direction.UP) }
            )
            DPadButton(
                icon = Icons.Default.KeyboardArrowDown,
                desc = "Down",
                testTag = "dpad_down",
                onClick = { onDirectionInput(Direction.DOWN) }
            )
        }
        DPadButton(
            icon = Icons.Default.KeyboardArrowRight,
            desc = "Right",
            testTag = "dpad_right",
            onClick = { onDirectionInput(Direction.RIGHT) }
        )
    }
}

@Composable
private fun DPadButton(
    icon: ImageVector,
    desc: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(width = 76.dp, height = 50.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = GlassSurface,
        border = BorderStroke(1.5.dp, ElectricCyan.copy(alpha = 0.7f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = desc,
                tint = ElectricCyan,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun TutorialOverlayModal(lang: AppLanguage, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack.copy(alpha = 0.78f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        NeonGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ElectricCyan
        ) {
            Text(
                text = tr(lang, "HOW TO PLAY • طريقة اللعب", "طريقة اللعب • HOW TO PLAY"),
                style = MaterialTheme.typography.headlineMedium,
                color = NeonGreen
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = tr(
                    lang,
                    "• Swipe anywhere on the arena or use the D-Pad buttons to steer your Cyber-Snake.\n" +
                            "• Collect glowing Energy Orbs to grow, chain Combos (up to x5), and evolve through 5 Stages!\n" +
                            "• Grab Shields, Magnets, and Ghost Phase power-ups.\n" +
                            "• In Boss Challenges, every orb you collect fires a Plasma Strike at the Boss Core!",
                    "• اسحب على الساحة أو استخدم أزرار الاتجاهات للتحكم بالحنش الإلكتروني.\n" +
                            "• اجمع كرات الطاقة للنمو وتحقيق سلاسل الكومبو (حتى ×5) والتطور عبر 5 مراحل!\n" +
                            "• التقط القدرات الخاصة مثل الدرع والمغناطيس وطور الشبح.\n" +
                            "• عند ظهور الزعيم، جمع الطاقة يطلق ضربات مباشرة لتدمير نواة الزعيم!"
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            NeonActionButton(
                text = tr(lang, "GOT IT! LET'S PLAY", "فهمت! ابدأ اللعب"),
                accentColor = NeonGreen,
                testTag = "dismiss_tutorial_button",
                onClick = onDismiss
            )
        }
    }
}

@Composable
private fun PauseOverlayModal(
    engineState: GameEngineState,
    lang: AppLanguage,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack.copy(alpha = 0.82f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        NeonGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ElectricCyan
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tr(lang, "GAME PAUSED", "اللعبة متوقفة مؤقتًا"),
                    style = MaterialTheme.typography.headlineLarge,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${tr(lang, "Current Score", "النتيجة الحالية")}: ${engineState.score}",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonGreen
                )
                Spacer(modifier = Modifier.height(20.dp))

                NeonActionButton(
                    text = tr(lang, "RESUME • استئناف", "استئناف اللعب • RESUME"),
                    icon = Icons.Default.PlayArrow,
                    accentColor = NeonGreen,
                    testTag = "resume_button",
                    onClick = onResume
                )
                Spacer(modifier = Modifier.height(12.dp))
                NeonActionButton(
                    text = tr(lang, "RESTART • إعادة الجولة", "إعادة الجولة • RESTART"),
                    icon = Icons.Default.Refresh,
                    accentColor = ElectricCyan,
                    testTag = "restart_button",
                    onClick = onRestart
                )
                Spacer(modifier = Modifier.height(12.dp))
                NeonActionButton(
                    text = tr(lang, "MAIN MENU • القائمة الرئيسية", "القائمة الرئيسية • MENU"),
                    icon = Icons.Default.Home,
                    accentColor = NeonMagenta,
                    testTag = "pause_main_menu_button",
                    onClick = onMainMenu
                )
            }
        }
    }
}

@Composable
private fun GameOverOrVictoryModal(
    engineState: GameEngineState,
    lang: AppLanguage,
    onPlayAgain: () -> Unit,
    onRevive: () -> Unit,
    onMainMenu: () -> Unit
) {
    val isVictory = engineState.status == PlayStatus.VICTORY
    val headerColor = if (isVictory) GoldenEnergy else DangerRed
    val headerTitle = if (isVictory) {
        tr(lang, "VICTORY ACHIEVED!", "تم إنجاز المهمة بنجاح!")
    } else {
        tr(lang, "GAME OVER", "انتهت الجولة")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack.copy(alpha = 0.86f))
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        NeonGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = headerColor
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.displayMedium,
                    color = headerColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Match Summary Metrics
                StatSummaryRow(
                    label = tr(lang, "Final Score", "النتيجة النهائية"),
                    value = engineState.score.toString(),
                    valueColor = NeonGreen
                )
                StatSummaryRow(
                    label = tr(lang, "Best Score", "أفضل نتيجة"),
                    value = engineState.bestScore.toString(),
                    valueColor = ElectricCyan
                )
                StatSummaryRow(
                    label = tr(lang, "Evolution Stage", "مرحلة التطور"),
                    value = tr(lang, engineState.evolutionStage.nameEn, engineState.evolutionStage.nameAr),
                    valueColor = engineState.evolutionStage.headColor
                )
                StatSummaryRow(
                    label = tr(lang, "Play Duration", "مدة اللعب"),
                    value = "${engineState.elapsedSeconds}s",
                    valueColor = TextPrimary
                )
                StatSummaryRow(
                    label = tr(lang, "Max Combo", "أعلى كومبو"),
                    value = "${engineState.maxComboThisRun}x",
                    valueColor = NeonMagenta
                )
                StatSummaryRow(
                    label = tr(lang, "Energy Coins Earned", "العملات المكتسبة"),
                    value = "+${engineState.coinsEarnedThisRun} 🪙",
                    valueColor = GoldenEnergy
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Revive Button (Available once per round on Game Over if enabled)
                if (!isVictory && !engineState.reviveUsed && engineState.canRevive) {
                    NeonActionButton(
                        text = tr(
                            lang,
                            "REVIVE WITH SHIELD (1x FREE)",
                            "إحياء الحنش مع درع حماية (مجاني لمرة واحدة)"
                        ),
                        icon = Icons.Default.Shield,
                        accentColor = ElectricCyan,
                        testTag = "revive_button",
                        onClick = onRevive
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                NeonActionButton(
                    text = tr(lang, "PLAY AGAIN • العب مجددًا", "العب مجددًا • PLAY AGAIN"),
                    icon = Icons.Default.Replay,
                    accentColor = NeonGreen,
                    testTag = "play_again_button",
                    onClick = onPlayAgain
                )
                Spacer(modifier = Modifier.height(10.dp))
                NeonActionButton(
                    text = tr(lang, "MAIN MENU • القائمة الرئيسية", "القائمة الرئيسية • MENU"),
                    icon = Icons.Default.Home,
                    accentColor = NeonMagenta,
                    testTag = "gameover_main_menu_button",
                    onClick = onMainMenu
                )
            }
        }
    }
}

@Composable
private fun StatSummaryRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}
