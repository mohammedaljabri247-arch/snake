package com.example.game.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.models.*
import com.example.game.engine.GameEngineState
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

object GameRenderer {

    fun DrawScope.renderArena(
        engineState: GameEngineState,
        selectedSkin: SnakeSkin,
        pulsePhase: Float,
        reducedVisualEffects: Boolean
    ) {
        val gw = engineState.gridWidth
        val gh = engineState.gridHeight
        val cellW = size.width / gw
        val cellH = size.height / gh
        val cellMin = minOf(cellW, cellH)

        // 1. Arena Deep Neon Background Gradient
        val arena = engineState.arena
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(arena.bgTop, arena.bgBottom, PureBlack)
            ),
            size = size
        )

        // 2. Cyberpunk Electronic Grid Lines
        val gridAlpha = if (reducedVisualEffects) 0.08f else 0.14f
        val gridLineColor = arena.gridColor.copy(alpha = gridAlpha)

        for (x in 0..gw) {
            val px = x * cellW
            drawLine(
                color = gridLineColor,
                start = Offset(px, 0f),
                end = Offset(px, size.height),
                strokeWidth = if (x % 5 == 0) 2f else 1f
            )
        }
        for (y in 0..gh) {
            val py = y * cellH
            drawLine(
                color = gridLineColor,
                start = Offset(0f, py),
                end = Offset(size.width, py),
                strokeWidth = if (y % 5 == 0) 2f else 1f
            )
        }

        // 3. Perimeter Border (Dashed/Warp if allowWrapWalls, Solid Neon Laser if standard)
        val borderColor = if (arena.allowWrapWalls || engineState.hasPowerUp(PowerUpType.GHOST_MODE)) {
            ElectricCyan.copy(alpha = 0.45f + 0.25f * sin(pulsePhase * 2f * PI.toFloat()))
        } else {
            arena.obstacleColor.copy(alpha = 0.85f)
        }
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(2f, 2f),
            size = Size(size.width - 4f, size.height - 4f),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 4f)
        )

        // 4. Boss Telegraph Warnings & Active Strikes
        engineState.bossState?.let { boss ->
            val warnAlpha = 0.25f + 0.22f * abs(sin(pulsePhase * 4f * PI.toFloat()))
            for (wc in boss.warningCells) {
                val topLeft = Offset(wc.x * cellW + 2f, wc.y * cellH + 2f)
                drawRoundRect(
                    color = GoldenEnergy.copy(alpha = warnAlpha),
                    topLeft = topLeft,
                    size = Size(cellW - 4f, cellH - 4f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = DangerRed.copy(alpha = 0.7f),
                    topLeft = topLeft,
                    size = Size(cellW - 4f, cellH - 4f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = 2f)
                )
            }

            // Active Boss Strike Laser Beams
            for (hc in boss.activeHazardCells) {
                val topLeft = Offset(hc.x * cellW, hc.y * cellH)
                if (!reducedVisualEffects) {
                    drawRoundRect(
                        color = DangerRed.copy(alpha = 0.45f),
                        topLeft = Offset(topLeft.x - 4f, topLeft.y - 4f),
                        size = Size(cellW + 8f, cellH + 8f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
                drawRoundRect(
                    color = DangerRed,
                    topLeft = Offset(topLeft.x + 2f, topLeft.y + 2f),
                    size = Size(cellW - 4f, cellH - 4f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // Boss Core Reactor (2x2)
            if (boss.corePositions.isNotEmpty()) {
                val minX = boss.corePositions.minOf { it.x } * cellW
                val minY = boss.corePositions.minOf { it.y } * cellH
                val coreW = cellW * 2f
                val coreH = cellH * 2f
                val center = Offset(minX + coreW / 2f, minY + coreH / 2f)
                val coreColor = if (boss.hitFlashTicks > 0) Color.White else boss.boss.primaryColor

                if (!reducedVisualEffects) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                coreColor.copy(alpha = 0.65f),
                                boss.boss.secondaryColor.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = coreW * 1.15f
                        ),
                        radius = coreW * 1.15f,
                        center = center
                    )
                }
                drawRoundRect(
                    color = coreColor,
                    topLeft = Offset(minX + 3f, minY + 3f),
                    size = Size(coreW - 6f, coreH - 6f),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                drawRoundRect(
                    color = boss.boss.secondaryColor,
                    topLeft = Offset(minX + 8f, minY + 8f),
                    size = Size(coreW - 16f, coreH - 16f),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 3f)
                )
            }
        }

        // 5. Static & Moving Obstacles
        for (obs in engineState.obstacles) {
            val ox = obs.pos.x * cellW
            val oy = obs.pos.y * cellH
            val obsColor = if (obs.velocity != null) CyberOrange else arena.obstacleColor

            if (!reducedVisualEffects) {
                drawRoundRect(
                    color = obsColor.copy(alpha = 0.28f),
                    topLeft = Offset(ox - 3f, oy - 3f),
                    size = Size(cellW + 6f, cellH + 6f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
            drawRoundRect(
                color = GlassSurface,
                topLeft = Offset(ox + 2f, oy + 2f),
                size = Size(cellW - 4f, cellH - 4f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = obsColor,
                topLeft = Offset(ox + 2f, oy + 2f),
                size = Size(cellW - 4f, cellH - 4f),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 2.5f)
            )
        }

        // 6. Energy Orbs with Multi-Ring Glow
        val pulseScale = 0.88f + 0.18f * sin(pulsePhase * 2f * PI.toFloat())
        for (orb in engineState.energyOrbs) {
            val center = Offset((orb.pos.x + 0.5f) * cellW, (orb.pos.y + 0.5f) * cellH)
            val baseRadius = (cellMin * 0.40f) * pulseScale

            if (!reducedVisualEffects) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orb.type.color.copy(alpha = 0.65f),
                            orb.type.secondaryColor.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 2.4f
                    ),
                    radius = baseRadius * 2.4f,
                    center = center
                )
            }
            drawCircle(
                color = orb.type.color,
                radius = baseRadius,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = baseRadius * 0.42f,
                center = center
            )
        }

        // 7. Power-Up Items on Grid
        for (pu in engineState.powerUpItems) {
            val center = Offset((pu.pos.x + 0.5f) * cellW, (pu.pos.y + 0.5f) * cellH)
            val r = cellMin * 0.46f
            if (!reducedVisualEffects) {
                drawCircle(
                    color = pu.type.color.copy(alpha = 0.35f),
                    radius = r * 1.9f,
                    center = center
                )
            }
            // Diamond power-up shape
            val path = Path().apply {
                moveTo(center.x, center.y - r)
                lineTo(center.x + r, center.y)
                lineTo(center.x, center.y + r)
                lineTo(center.x - r, center.y)
                close()
            }
            drawPath(path = path, color = pu.type.color)
            drawPath(path = path, color = Color.White, style = Stroke(width = 2f))
        }

        // 8. Neon Evolved Snake (Tail-to-Head rendering so Head sits on top)
        val snake = engineState.snake
        val evo = engineState.evolutionStage
        val ghostMode = engineState.hasPowerUp(PowerUpType.GHOST_MODE)
        val shieldMode = engineState.hasPowerUp(PowerUpType.SHIELD)
        val alphaMod = if (ghostMode) 0.55f else 1.0f

        for (i in snake.indices.reversed()) {
            val seg = snake[i]
            val cx = (seg.x + 0.5f) * cellW
            val cy = (seg.y + 0.5f) * cellH
            val isHead = i == 0

            val progressAlongBody = i.toFloat() / snake.size.coerceAtLeast(1)
            val taperFactor = if (isHead) 1.0f else (1.0f - progressAlongBody * 0.38f).coerceAtLeast(0.52f)

            val segColor = when {
                isHead -> selectedSkin.headColor
                i % 2 == 0 -> selectedSkin.bodyPrimary
                else -> selectedSkin.bodySecondary
            }.copy(alpha = alphaMod)

            val segW = cellW * 0.88f * taperFactor
            val segH = cellH * 0.88f * taperFactor

            // Evolution & Skin Outer Glow
            if (!reducedVisualEffects && (isHead || i % 2 == 0)) {
                val glowRadius = cellMin * 0.65f * evo.glowRadiusMultiplier * taperFactor
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            segColor.copy(alpha = 0.42f * alphaMod),
                            evo.headColor.copy(alpha = 0.15f * alphaMod),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = Offset(cx, cy)
                )
            }

            // Segment core body
            drawRoundRect(
                color = segColor,
                topLeft = Offset(cx - segW / 2f, cy - segH / 2f),
                size = Size(segW, segH),
                cornerRadius = CornerRadius(segW * 0.38f, segH * 0.38f)
            )

            // Evolution Stage 3+ inner energy spine node
            if (!isHead && evo.stageNumber >= 3) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f * alphaMod),
                    radius = minOf(segW, segH) * 0.22f,
                    center = Offset(cx, cy)
                )
            }

            // Head Cyber Visor & Eyes + Shield Ring
            if (isHead) {
                val dir = engineState.direction
                val eyeOffsetForward = cellMin * 0.16f
                val eyeOffsetSide = cellMin * 0.18f

                val (eye1, eye2) = when (dir) {
                    Direction.UP -> Offset(cx - eyeOffsetSide, cy - eyeOffsetForward) to Offset(cx + eyeOffsetSide, cy - eyeOffsetForward)
                    Direction.DOWN -> Offset(cx - eyeOffsetSide, cy + eyeOffsetForward) to Offset(cx + eyeOffsetSide, cy + eyeOffsetForward)
                    Direction.LEFT -> Offset(cx - eyeOffsetForward, cy - eyeOffsetSide) to Offset(cx - eyeOffsetForward, cy + eyeOffsetSide)
                    Direction.RIGHT -> Offset(cx + eyeOffsetForward, cy - eyeOffsetSide) to Offset(cx + eyeOffsetForward, cy + eyeOffsetSide)
                }

                drawCircle(color = PureBlack, radius = cellMin * 0.11f, center = eye1)
                drawCircle(color = PureBlack, radius = cellMin * 0.11f, center = eye2)
                drawCircle(color = Color.White, radius = cellMin * 0.06f, center = eye1)
                drawCircle(color = Color.White, radius = cellMin * 0.06f, center = eye2)

                // Active Holo-Shield bubble around head
                if (shieldMode) {
                    drawCircle(
                        color = ElectricCyan.copy(alpha = 0.75f),
                        radius = cellMin * 0.72f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3f)
                    )
                }
            }
        }

        // 9. Burst Particles
        if (!reducedVisualEffects) {
            for (p in engineState.particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = p.size,
                    center = Offset(p.x * cellW, p.y * cellH)
                )
            }
        }
    }
}
