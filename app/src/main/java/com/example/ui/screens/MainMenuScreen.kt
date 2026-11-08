package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.preferences.PersistedPlayerData
import com.example.domain.models.*
import com.example.ui.AppScreen
import com.example.ui.components.NeonActionButton
import com.example.ui.components.NeonGlassCard
import com.example.ui.components.tr
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MainMenuScreen(
    playerData: PersistedPlayerData,
    selectedMode: GameMode,
    onPlayClick: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onToggleLanguage: () -> Unit
) {
    val lang = playerData.settings.language
    val selectedSkin = SnakeSkin.fromId(playerData.selectedSkinId)
    val selectedArena = ArenaLevel.entries.find { it.id == playerData.selectedArenaId } ?: ArenaLevel.NEON_CITY

    val infiniteTransition = rememberInfiniteTransition(label = "menu_anim")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snake_phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Animated Cyberpunk Neon Background with Autonomous Cyber-Snake
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF060919), Color(0xFF140826), PureBlack)
                ),
                size = size
            )

            // Subtle Grid
            val step = size.width / 12f
            val gridColor = ElectricCyan.copy(alpha = 0.07f)
            for (x in 0..12) {
                drawLine(
                    color = gridColor,
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, size.height),
                    strokeWidth = 1f
                )
            }
            val vSteps = (size.height / step).toInt()
            for (y in 0..vSteps) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y * step),
                    end = Offset(size.width, y * step),
                    strokeWidth = 1f
                )
            }

            // Animated Demo Cyber-Snake gliding smoothly across background
            val segments = 16
            for (i in (segments - 1) downTo 0) {
                val t = (animPhase - i * 0.022f + 1f) % 1f
                val angle = t * 2f * PI.toFloat()
                val sx = size.width * 0.5f + cos(angle) * (size.width * 0.34f)
                val sy = size.height * 0.27f + sin(angle * 2f) * (size.height * 0.09f)
                val radius = if (i == 0) 18f else (15f - i * 0.6f).coerceAtLeast(6f)
                val segColor = if (i == 0) selectedSkin.headColor else selectedSkin.bodyPrimary

                if (!playerData.settings.reducedVisualEffects && i % 2 == 0) {
                    drawCircle(
                        color = segColor.copy(alpha = 0.22f),
                        radius = radius * 2.2f,
                        center = Offset(sx, sy)
                    )
                }
                drawCircle(
                    color = segColor.copy(alpha = 0.68f),
                    radius = radius,
                    center = Offset(sx, sy)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Best Score, Energy Coins & Quick Language Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Best Score Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = GlassSurface.copy(alpha = 0.9f),
                    border = BorderStroke(1.2.dp, ElectricCyan.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🏆", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = tr(lang, "BEST SCORE", "أفضل نتيجة"),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = playerData.stats.bestScore.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Energy Coins Badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = GlassSurface.copy(alpha = 0.9f),
                        border = BorderStroke(1.2.dp, GoldenEnergy.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🪙", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = tr(lang, "ENERGY COINS", "العملات"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = playerData.coins.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GoldenEnergy
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Quick AR / EN Switcher
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onToggleLanguage)
                            .testTag("language_toggle_button"),
                        shape = CircleShape,
                        color = GlassSurface,
                        border = BorderStroke(1.2.dp, NeonMagenta)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (lang == AppLanguage.AR) "EN" else "ع",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeonMagenta
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Game Logo & Hero Header
            NeonGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonGreen.copy(alpha = 0.75f),
                backgroundColor = Color(0xFF0B1026).copy(alpha = 0.85f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SNAKE EVOLUTION",
                        style = MaterialTheme.typography.displayMedium,
                        color = NeonGreen,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "NEON SURVIVAL • الحنش المتطور",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Current Loadout Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LoadoutBadge(
                            label = tr(lang, "MODE", "النمط"),
                            value = tr(lang, selectedMode.titleEn, selectedMode.titleAr),
                            color = selectedMode.accentColor
                        )
                        LoadoutBadge(
                            label = tr(lang, "ARENA", "الساحة"),
                            value = tr(lang, selectedArena.titleEn, selectedArena.titleAr),
                            color = ElectricCyan
                        )
                        LoadoutBadge(
                            label = tr(lang, "SKIN", "الشكل"),
                            value = tr(lang, selectedSkin.nameEn, selectedSkin.nameAr),
                            color = selectedSkin.headColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary PLAY Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onPlayClick)
                    .testTag("play_button"),
                shape = RoundedCornerShape(20.dp),
                color = NeonGreen,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = PureBlack,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr(lang, "PLAY NOW • ابدأ اللعب", "ابدأ اللعب الآن • PLAY"),
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2-Column Navigation Grid for all required systems
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuGridButton(
                    modifier = Modifier.weight(1f),
                    title = tr(lang, "Game Modes", "أنماط اللعب"),
                    subtitle = tr(lang, "5 Modes & Arenas", "5 أنماط وساحات"),
                    icon = Icons.Default.SportsEsports,
                    accentColor = ElectricCyan,
                    testTag = "menu_game_modes",
                    onClick = { onNavigate(AppScreen.GAME_MODES) }
                )
                MenuGridButton(
                    modifier = Modifier.weight(1f),
                    title = tr(lang, "Missions", "المهام والزعماء"),
                    subtitle = "${playerData.completedMissions.size}/${ALL_MISSIONS.size} " + tr(lang, "Done", "مكتملة"),
                    icon = Icons.Default.Flag,
                    accentColor = PlasmaPurple,
                    testTag = "menu_missions",
                    onClick = { onNavigate(AppScreen.MISSIONS) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuGridButton(
                    modifier = Modifier.weight(1f),
                    title = tr(lang, "Snake Skins", "أشكال الحنش"),
                    subtitle = "${playerData.unlockedSkins.size}/${SnakeSkin.entries.size} " + tr(lang, "Unlocked", "مفتوحة"),
                    icon = Icons.Default.Palette,
                    accentColor = NeonGreen,
                    testTag = "menu_skins",
                    onClick = { onNavigate(AppScreen.SKINS) }
                )
                MenuGridButton(
                    modifier = Modifier.weight(1f),
                    title = tr(lang, "Upgrades", "شجرة المهارات"),
                    subtitle = tr(lang, "6 Cyber Skills", "تطوير القدرات"),
                    icon = Icons.Default.Bolt,
                    accentColor = GoldenEnergy,
                    testTag = "menu_upgrades",
                    onClick = { onNavigate(AppScreen.SKILL_TREE) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuGridButton(
                    modifier = Modifier.weight(1f),
                    title = tr(lang, "Daily Challenges", "التحديات اليومية"),
                    subtitle = "${playerData.dailyChallenges.count { it.isCompleted }}/3 " + tr(lang, "Today", "اليوم"),
                    icon = Icons.Default.Today,
                    accentColor = CyberOrange,
                    testTag = "menu_daily",
                    onClick = { onNavigate(AppScreen.DAILY_CHALLENGES) }
                )
                MenuGridButton(
                    modifier = Modifier.weight(1f),
                    title = tr(lang, "Achievements", "الإنجازات"),
                    subtitle = "${playerData.unlockedAchievements.size}/${ALL_ACHIEVEMENTS.size} " + tr(lang, "Badges", "وسام"),
                    icon = Icons.Default.EmojiEvents,
                    accentColor = NeonMagenta,
                    testTag = "menu_achievements",
                    onClick = { onNavigate(AppScreen.ACHIEVEMENTS) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuGridButton(
                    modifier = Modifier.weight(1f),
                    title = tr(lang, "Statistics", "الإحصائيات"),
                    subtitle = tr(lang, "Career Records", "سجلات اللاعب"),
                    icon = Icons.Default.QueryStats,
                    accentColor = ElectricCyan,
                    testTag = "menu_statistics",
                    onClick = { onNavigate(AppScreen.STATISTICS) }
                )
                MenuGridButton(
                    modifier = Modifier.weight(1f),
                    title = tr(lang, "Settings", "الإعدادات"),
                    subtitle = tr(lang, "Controls & Audio", "التحكم والصوت"),
                    icon = Icons.Default.Settings,
                    accentColor = TextSecondary,
                    testTag = "menu_settings",
                    onClick = { onNavigate(AppScreen.SETTINGS) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun LoadoutBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun MenuGridButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        color = GlassSurface.copy(alpha = 0.88f),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentColor,
                    maxLines = 1
                )
            }
        }
    }
}
