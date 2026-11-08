package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
fun MissionsScreen(
    playerData: PersistedPlayerData,
    onStartMission: (Mission) -> Unit,
    onStartBoss: (BossType) -> Unit,
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
            title = tr(lang, "TACTICAL MISSIONS", "المهام التكتيكية والزعماء"),
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
                Text(
                    text = tr(
                        lang,
                        "Complete tactical objectives across futuristic arenas to earn massive Energy Coin rewards.",
                        "أكمل المهام التكتيكية في مختلف الساحات للحصول على مكافآت كبيرة من العملات."
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }

            items(ALL_MISSIONS) { mission ->
                val isCompleted = mission.id in playerData.completedMissions
                NeonGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mission_item_${mission.id}"),
                    borderColor = if (isCompleted) NeonGreen else PlasmaPurple
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tr(lang, mission.titleEn, mission.titleAr),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (isCompleted) NeonGreen else TextPrimary
                                )
                                if (isCompleted) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr(lang, mission.descEn, mission.descAr),
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🪙 +${mission.rewardCoins} • ${tr(lang, mission.arenaLevel.titleEn, mission.arenaLevel.titleAr)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = GoldenEnergy
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onStartMission(mission) }
                                .testTag("start_mission_${mission.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCompleted) ElectricCyan else NeonGreen
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start Mission",
                                    tint = PureBlack
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCompleted) tr(lang, "REPLAY", "إعادة") else tr(lang, "START", "ابدأ"),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PureBlack
                                )
                            }
                        }
                    }
                }
            }

            // Boss Bounties Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tr(lang, "BOSS REACTOR BOUNTIES", "مواجهات الزعماء المباشرة"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = DangerRed
                )
            }

            items(BossType.entries) { boss ->
                NeonGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = boss.primaryColor
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👾 ${tr(lang, boss.nameEn, boss.nameAr)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = boss.primaryColor
                            )
                            Text(
                                text = tr(lang, boss.subtitleEn, boss.subtitleAr),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "HP: ${boss.maxHp} • Bounty: +${boss.coinReward} 🪙",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldenEnergy
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onStartBoss(boss) },
                            shape = RoundedCornerShape(14.dp),
                            color = boss.primaryColor
                        ) {
                            Text(
                                text = tr(lang, "FIGHT", "واجه"),
                                style = MaterialTheme.typography.labelLarge,
                                color = PureBlack,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkinsScreen(
    playerData: PersistedPlayerData,
    onSkinAction: (SnakeSkin) -> Unit,
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
            title = tr(lang, "SNAKE SKINS • أشكال الحنش", "أشكال الحنش • SNAKE SKINS"),
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
            items(SnakeSkin.entries) { skin ->
                val isUnlocked = skin.id in playerData.unlockedSkins || skin.cost == 0
                val isSelected = skin.id == playerData.selectedSkinId
                val canAfford = playerData.coins >= skin.cost

                NeonGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSkinAction(skin) }
                        .testTag("skin_card_${skin.id}"),
                    borderColor = when {
                        isSelected -> NeonGreen
                        isUnlocked -> skin.headColor.copy(alpha = 0.7f)
                        else -> GlassBorder
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Mini Canvas Preview of the Snake Skin
                        Canvas(
                            modifier = Modifier
                                .size(width = 88.dp, height = 44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkNavy)
                        ) {
                            val segCount = 4
                            val radius = 10.dp.toPx()
                            for (i in (segCount - 1) downTo 0) {
                                val cx = 20.dp.toPx() + (segCount - 1 - i) * 16.dp.toPx()
                                val cy = size.height / 2f
                                val col = when {
                                    i == 0 -> skin.headColor
                                    i % 2 == 0 -> skin.bodyPrimary
                                    else -> skin.bodySecondary
                                }
                                drawRoundRect(
                                    color = col,
                                    topLeft = Offset(cx - radius, cy - radius),
                                    size = Size(radius * 2, radius * 2),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(lang, skin.nameEn, skin.nameAr),
                                style = MaterialTheme.typography.titleLarge,
                                color = skin.headColor
                            )
                            Text(
                                text = tr(lang, skin.descEn, skin.descAr),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        val btnText = when {
                            isSelected -> tr(lang, "EQUIPPED", "مفعل")
                            isUnlocked -> tr(lang, "SELECT", "اختيار")
                            else -> "🪙 ${skin.cost}"
                        }
                        val btnColor = when {
                            isSelected -> NeonGreen
                            isUnlocked -> ElectricCyan
                            canAfford -> GoldenEnergy
                            else -> TextSecondary
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSkinAction(skin) }
                                .testTag("skin_action_${skin.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = btnColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.5.dp, btnColor)
                        ) {
                            Text(
                                text = btnText,
                                style = MaterialTheme.typography.labelLarge,
                                color = btnColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkillTreeScreen(
    playerData: PersistedPlayerData,
    onUpgradeSkill: (SkillType) -> Unit,
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
            title = tr(lang, "CYBER SKILL TREE", "شجرة المهارات والتطوير"),
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
                Text(
                    text = tr(
                        lang,
                        "Permanently upgrade your cyber-snake core systems using Energy Coins.",
                        "طور قدرات الحنش الإلكتروني بشكل دائم باستخدام عملات الطاقة."
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }

            items(SkillType.entries) { skill ->
                val currentLvl = playerData.skillLevels[skill] ?: 0
                val isMax = currentLvl >= skill.maxLevel
                val nextCost = skill.costForNextLevel(currentLvl)
                val canAfford = !isMax && playerData.coins >= nextCost

                NeonGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("skill_card_${skill.id}"),
                    borderColor = skill.accentColor.copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${skill.iconSymbol} ${tr(lang, skill.titleEn, skill.titleAr)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = skill.accentColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr(lang, skill.descEn, skill.descAr),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Level pips
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (i in 1..skill.maxLevel) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 28.dp, height = 8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (i <= currentLvl) skill.accentColor else DarkNavy
                                            )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = canAfford) { onUpgradeSkill(skill) }
                                .testTag("upgrade_skill_${skill.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isMax -> NeonGreen.copy(alpha = 0.2f)
                                canAfford -> GoldenEnergy
                                else -> DarkNavy
                            },
                            border = BorderStroke(
                                1.5.dp,
                                if (isMax) NeonGreen else if (canAfford) GoldenEnergy else GlassBorder
                            )
                        ) {
                            Text(
                                text = if (isMax) tr(lang, "MAXED", "الحد الأقصى") else "🪙 $nextCost",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (canAfford && !isMax) PureBlack else TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
