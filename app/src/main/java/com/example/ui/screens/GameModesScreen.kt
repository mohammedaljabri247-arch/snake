package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

@Composable
fun GameModesScreen(
    playerData: PersistedPlayerData,
    selectedMode: GameMode,
    selectedBoss: BossType,
    onSelectMode: (GameMode) -> Unit,
    onSelectArena: (ArenaLevel) -> Unit,
    onSelectBoss: (BossType) -> Unit,
    onStartMode: (GameMode) -> Unit,
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
            title = tr(lang, "GAME MODES & ARENAS", "أنماط اللعب والساحات"),
            coins = playerData.coins,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Section 1: 7 Cyberpunk Arenas
            item {
                Text(
                    text = tr(lang, "SELECT ARENA ENVIRONMENT", "اختر بيئة ساحة اللعب (7 ساحات)"),
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ArenaLevel.entries) { arena ->
                        val isUnlocked = arena.id in playerData.unlockedArenas || arena.unlockCost == 0
                        val isSelected = arena.id == playerData.selectedArenaId

                        Surface(
                            modifier = Modifier
                                .width(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectArena(arena) }
                                .testTag("arena_card_${arena.id}"),
                            shape = RoundedCornerShape(16.dp),
                            color = GlassSurface,
                            border = BorderStroke(
                                width = if (isSelected) 2.5.dp else 1.2.dp,
                                color = if (isSelected) NeonGreen else arena.gridColor.copy(alpha = 0.6f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(arena.bgTop, arena.bgBottom)
                                        )
                                    )
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tr(lang, arena.titleEn, arena.titleAr),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = arena.gridColor
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = NeonGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else if (!isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = GoldenEnergy,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tr(lang, arena.descEn, arena.descAr),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    minLines = 2,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                val statusText = when {
                                    isSelected -> tr(lang, "ACTIVE ARENA", "الساحة المختارة")
                                    isUnlocked -> tr(lang, "TAP TO SELECT", "اضغط للاختيار")
                                    else -> "🪙 ${arena.unlockCost} " + tr(lang, "TO UNLOCK", "لفتح الساحة")
                                }
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isUnlocked) NeonGreen else GoldenEnergy
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: 5 Game Modes
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tr(lang, "CHOOSE GAMEPLAY MODE", "اختر نمط اللعب"),
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonGreen
                )
            }

            items(GameMode.entries) { mode ->
                val isSelected = mode == selectedMode
                NeonGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectMode(mode) }
                        .testTag("mode_card_${mode.id}"),
                    borderColor = if (isSelected) mode.accentColor else GlassBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(lang, mode.titleEn, mode.titleAr),
                                style = MaterialTheme.typography.headlineMedium,
                                color = mode.accentColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr(lang, mode.descEn, mode.descAr),
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onStartMode(mode) }
                                .testTag("launch_mode_${mode.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = mode.accentColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Launch",
                                    tint = PureBlack
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr(lang, "PLAY", "العب"),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PureBlack
                                )
                            }
                        }
                    }

                    // If Boss Challenge mode, show the 4 selectable Bosses
                    if (mode == GameMode.BOSS_CHALLENGE) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = tr(lang, "SELECT TARGET BOSS CORE:", "اختر الزعيم للمواجهة:"),
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldenEnergy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(BossType.entries) { boss ->
                                val bossSelected = boss == selectedBoss
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelectBoss(boss) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (bossSelected) boss.primaryColor.copy(alpha = 0.25f) else DarkNavy,
                                    border = BorderStroke(
                                        width = if (bossSelected) 2.dp else 1.dp,
                                        color = if (bossSelected) boss.primaryColor else GlassBorder
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = tr(lang, boss.nameEn, boss.nameAr),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = boss.primaryColor
                                        )
                                        Text(
                                            text = "HP: ${boss.maxHp} • +${boss.coinReward} 🪙",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = GoldenEnergy
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
