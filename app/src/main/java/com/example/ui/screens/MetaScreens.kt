package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.preferences.PersistedPlayerData
import com.example.domain.models.*
import com.example.ui.components.NeonActionButton
import com.example.ui.components.NeonGlassCard
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.tr
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DailyChallengesScreen(
    playerData: PersistedPlayerData,
    onPlayNow: () -> Unit,
    onBack: () -> Unit
) {
    val lang = playerData.settings.language
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        ScreenTopBar(
            title = tr(lang, "DAILY CHALLENGES", "التحديات اليومية"),
            coins = playerData.coins,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                NeonGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = CyberOrange
                ) {
                    Text(
                        text = "📅 ${tr(lang, "TODAY'S ACTIVE BOUNTIES", "تحديات اليوم النشطة")} (${playerData.dailyDateKey})",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberOrange
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr(
                            lang,
                            "Offline daily challenges refresh automatically every day based on your device clock.",
                            "تتجدد التحديات اليومية تلقائيًا كل يوم محليًا دون الحاجة إلى إنترنت."
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            items(playerData.dailyChallenges) { challenge ->
                val fraction = (challenge.progress.toFloat() / challenge.target.coerceAtLeast(1)).coerceIn(0f, 1f)
                NeonGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_card_${challenge.id}"),
                    borderColor = if (challenge.isCompleted) NeonGreen else GlassBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(lang, challenge.titleEn, challenge.titleAr),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (challenge.isCompleted) NeonGreen else TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (challenge.isCompleted) NeonGreen else ElectricCyan,
                                trackColor = DarkNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${challenge.progress} / ${challenge.target} • 🪙 +${challenge.rewardCoins}",
                                style = MaterialTheme.typography.labelLarge,
                                color = GoldenEnergy
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        if (challenge.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = NeonGreen,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                NeonActionButton(
                    text = tr(lang, "PLAY MATCH TO PROGRESS", "العب جولة لإنجاز التحديات"),
                    icon = Icons.Default.PlayArrow,
                    accentColor = NeonGreen,
                    onClick = onPlayNow
                )
            }
        }
    }
}

@Composable
fun AchievementsScreen(
    playerData: PersistedPlayerData,
    onBack: () -> Unit
) {
    val lang = playerData.settings.language
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        ScreenTopBar(
            title = tr(lang, "ACHIEVEMENTS • الإنجازات", "الإنجازات • ACHIEVEMENTS"),
            coins = playerData.coins,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(ALL_ACHIEVEMENTS) { ach ->
                val isUnlocked = ach.id in playerData.unlockedAchievements
                NeonGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("achievement_${ach.id}"),
                    borderColor = if (isUnlocked) NeonMagenta else GlassBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isUnlocked) NeonMagenta.copy(alpha = 0.22f) else DarkNavy
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = ach.icon, style = MaterialTheme.typography.headlineMedium)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(lang, ach.titleEn, ach.titleAr),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isUnlocked) NeonGreen else TextPrimary
                            )
                            Text(
                                text = tr(lang, ach.descEn, ach.descAr),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🪙 +${ach.rewardCoins} " +
                                        if (isUnlocked) tr(lang, "(CLAIMED)", "(مكتمل)") else tr(lang, "(LOCKED)", "(مغلق)"),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isUnlocked) NeonGreen else GoldenEnergy
                            )
                        }
                        if (isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Unlocked",
                                tint = NeonGreen,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsScreen(
    playerData: PersistedPlayerData,
    onBack: () -> Unit
) {
    val lang = playerData.settings.language
    val stats = playerData.stats
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        ScreenTopBar(
            title = tr(lang, "PLAYER STATISTICS", "إحصائيات اللاعب"),
            coins = playerData.coins,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            val rows = listOf(
                Triple(tr(lang, "Total Matches Played", "إجمالي عدد الألعاب"), stats.totalGames.toString(), ElectricCyan),
                Triple(tr(lang, "All-Time Best Score", "أعلى نتيجة"), stats.bestScore.toString(), NeonGreen),
                Triple(tr(lang, "Total Cumulative Score", "إجمالي النقاط"), stats.totalScore.toString(), PlasmaPurple),
                Triple(tr(lang, "Total Energy Coins Earned", "إجمالي العملات المكتسبة"), "${stats.totalCoinsEarned} 🪙", GoldenEnergy),
                Triple(tr(lang, "Longest Cyber-Snake", "أطول حنش تم الوصول إليه"), "${stats.longestSnake} segments", NeonGreen),
                Triple(tr(lang, "Longest Survival Time", "أطول مدة بقاء"), "${stats.longestSurvivalSeconds}s", ElectricCyan),
                Triple(tr(lang, "Total Energy Orbs Collected", "عدد كرات الطاقة المجمعة"), stats.totalOrbsCollected.toString(), CyberOrange),
                Triple(tr(lang, "Boss Cores Defeated", "عدد مرات هزيمة الزعماء"), stats.bossesDefeated.toString(), DangerRed),
                Triple(tr(lang, "Missions Completed", "المهام المكتملة"), "${playerData.completedMissions.size} / ${ALL_MISSIONS.size}", PlasmaPurple),
                Triple(tr(lang, "Achievements Unlocked", "الإنجازات المفتوحة"), "${playerData.unlockedAchievements.size} / ${ALL_ACHIEVEMENTS.size}", NeonMagenta)
            )

            items(rows) { (label, value, color) ->
                NeonGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = color.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineMedium,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    playerData: PersistedPlayerData,
    onUpdateSettings: ((GameSettings) -> GameSettings) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit
) {
    val settings = playerData.settings
    val lang = settings.language
    var showResetConfirm by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        ScreenTopBar(
            title = tr(lang, "SETTINGS • الإعدادات", "الإعدادات • SETTINGS"),
            coins = playerData.coins,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // 1. Language (AR / EN with RTL support)
            item {
                NeonGlassCard(borderColor = NeonMagenta) {
                    Text(
                        text = tr(lang, "LANGUAGE • اللغة", "اللغة • LANGUAGE"),
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonMagenta
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppLanguage.entries.forEach { l ->
                            val selected = l == lang
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onUpdateSettings { it.copy(language = l) } }
                                    .testTag("lang_option_${l.code}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) NeonMagenta.copy(alpha = 0.25f) else DarkNavy,
                                border = BorderStroke(1.5.dp, if (selected) NeonMagenta else GlassBorder)
                            ) {
                                Text(
                                    text = l.label,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (selected) NeonMagenta else TextPrimary,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // 2. Audio Sliders (Music & SFX Volume)
            item {
                NeonGlassCard(borderColor = ElectricCyan) {
                    Text(
                        text = tr(lang, "AUDIO & HAPTICS", "الصوت والاهتزاز"),
                        style = MaterialTheme.typography.titleLarge,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${tr(lang, "Music Volume", "مستوى صوت الموسيقى")}: ${(settings.musicVolume * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Slider(
                        value = settings.musicVolume,
                        onValueChange = { v -> onUpdateSettings { it.copy(musicVolume = v) } },
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan
                        )
                    )

                    Text(
                        text = "${tr(lang, "Sound Effects Volume", "مستوى المؤثرات الصوتية")}: ${(settings.sfxVolume * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Slider(
                        value = settings.sfxVolume,
                        onValueChange = { v -> onUpdateSettings { it.copy(sfxVolume = v) } },
                        colors = SliderDefaults.colors(
                            thumbColor = NeonGreen,
                            activeTrackColor = NeonGreen
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr(lang, "Haptic Vibration", "الاهتزاز التفاعلي"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        Switch(
                            checked = settings.vibrationEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(vibrationEnabled = v) } }
                        )
                    }
                }
            }

            // 3. Controls & Speed
            item {
                NeonGlassCard(borderColor = NeonGreen) {
                    Text(
                        text = tr(lang, "CONTROLS & GAME SPEED", "التحكم وسرعة اللعب"),
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = tr(lang, "Control Scheme", "طريقة التحكم"),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ControlMode.entries.forEach { mode ->
                            val selected = mode == settings.controlMode
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onUpdateSettings { it.copy(controlMode = mode) } }
                                    .testTag("control_mode_${mode.name}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) NeonGreen.copy(alpha = 0.2f) else DarkNavy,
                                border = BorderStroke(1.2.dp, if (selected) NeonGreen else GlassBorder)
                            ) {
                                Text(
                                    text = tr(lang, mode.titleEn, mode.titleAr),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (selected) NeonGreen else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${tr(lang, "Swipe Sensitivity", "حساسية السحب")}: ${(settings.swipeSensitivity * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Slider(
                        value = settings.swipeSensitivity,
                        onValueChange = { v -> onUpdateSettings { it.copy(swipeSensitivity = v) } },
                        valueRange = 0.6f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonGreen,
                            activeTrackColor = NeonGreen
                        )
                    )

                    Text(
                        text = "${tr(lang, "Base Snake Speed", "سرعة الحنش الأساسية")}: ${(settings.gameSpeedMultiplier * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Slider(
                        value = settings.gameSpeedMultiplier,
                        onValueChange = { v -> onUpdateSettings { it.copy(gameSpeedMultiplier = v) } },
                        valueRange = 0.75f..1.35f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldenEnergy,
                            activeTrackColor = GoldenEnergy
                        )
                    )
                }
            }

            // 4. Visuals & Gameplay Toggles (Reduced FX & Revive)
            item {
                NeonGlassCard(borderColor = PlasmaPurple) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(lang, "Reduced Visual Effects", "تقليل المؤثرات البصرية"),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = tr(lang, "Optimizes battery & performance on older phones.", "لتوفير البطارية وتحسين الأداء."),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = settings.reducedVisualEffects,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(reducedVisualEffects = v) } }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(lang, "Allow 1x Free Revive per Match", "تفعيل ميزة الإحياء مرة في الجولة"),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = tr(lang, "Disable for hardcore single-life challenge.", "أوقفها لتحدي المحترفين بدون إحياء."),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = settings.reviveEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(reviveEnabled = v) } }
                        )
                    }
                }
            }

            // 5. Reset All Progress
            item {
                if (!showResetConfirm) {
                    NeonActionButton(
                        text = tr(lang, "RESET ALL PROGRESS • مسح التقدم", "مسح التقدم وإعادة الضبط • RESET"),
                        icon = Icons.Default.DeleteForever,
                        accentColor = DangerRed,
                        testTag = "reset_progress_button",
                        onClick = { showResetConfirm = true }
                    )
                } else {
                    NeonGlassCard(borderColor = DangerRed) {
                        Text(
                            text = tr(
                                lang,
                                "Are you sure you want to reset coins, skins, skills, and records?",
                                "هل أنت متأكد من مسح جميع العملات والأشكال والمهارات والنتائج؟"
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = DangerRed
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            NeonActionButton(
                                text = tr(lang, "CONFIRM RESET", "تأكيد المسح"),
                                accentColor = DangerRed,
                                modifier = Modifier.weight(1f),
                                testTag = "confirm_reset_button",
                                onClick = {
                                    onResetProgress()
                                    showResetConfirm = false
                                }
                            )
                            NeonActionButton(
                                text = tr(lang, "CANCEL", "إلغاء"),
                                accentColor = ElectricCyan,
                                modifier = Modifier.weight(1f),
                                onClick = { showResetConfirm = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
