package com.example.game.systems

import com.example.domain.models.*

object BossSystem {

    fun createBoss(bossType: BossType, gridWidth: Int): BossState {
        val cx = gridWidth / 2
        val core = listOf(
            GridPos(cx - 1, 3),
            GridPos(cx, 3),
            GridPos(cx - 1, 4),
            GridPos(cx, 4)
        )
        return BossState(
            boss = bossType,
            currentHp = bossType.maxHp,
            maxHp = bossType.maxHp,
            corePositions = core,
            warningCells = emptyList(),
            activeHazardCells = emptyList(),
            attackPhaseTick = 0,
            hitFlashTicks = 0
        )
    }

    /**
     * Advances the Boss state by 1 game tick.
     * Bosses always telegraph their attacks with warningCells for 10 ticks before activating activeHazardCells for 6 ticks,
     * ensuring 100% fair, readable, skill-based gameplay.
     */
    fun updateBossTick(
        bossState: BossState,
        gridWidth: Int,
        gridHeight: Int,
        snakeHead: GridPos
    ): BossState {
        val nextTick = bossState.attackPhaseTick + 1
        val cycle = nextTick % 32
        val flash = (bossState.hitFlashTicks - 1).coerceAtLeast(0)

        return when {
            // Phase 1: Calm (ticks 0..13)
            cycle in 0..13 -> {
                bossState.copy(
                    warningCells = emptyList(),
                    activeHazardCells = emptyList(),
                    attackPhaseTick = nextTick,
                    hitFlashTicks = flash
                )
            }
            // Phase 2: Telegraph Warning (ticks 14..23) -> compute pattern at tick 14
            cycle == 14 -> {
                val pattern = generateBossAttackPattern(
                    boss = bossState.boss,
                    cycleCount = nextTick / 32,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    snakeHead = snakeHead,
                    corePositions = bossState.corePositions
                )
                bossState.copy(
                    warningCells = pattern,
                    activeHazardCells = emptyList(),
                    attackPhaseTick = nextTick,
                    hitFlashTicks = flash
                )
            }
            cycle in 15..23 -> {
                bossState.copy(
                    attackPhaseTick = nextTick,
                    hitFlashTicks = flash
                )
            }
            // Phase 3: Active Strike (ticks 24..30) -> warningCells become activeHazardCells
            cycle in 24..30 -> {
                bossState.copy(
                    activeHazardCells = bossState.warningCells,
                    warningCells = emptyList(),
                    attackPhaseTick = nextTick,
                    hitFlashTicks = flash
                )
            }
            else -> {
                bossState.copy(
                    warningCells = emptyList(),
                    activeHazardCells = emptyList(),
                    attackPhaseTick = nextTick,
                    hitFlashTicks = flash
                )
            }
        }
    }

    private fun generateBossAttackPattern(
        boss: BossType,
        cycleCount: Int,
        gridWidth: Int,
        gridHeight: Int,
        snakeHead: GridPos,
        corePositions: List<GridPos>
    ): List<GridPos> {
        val cells = mutableSetOf<GridPos>()
        val cx = gridWidth / 2
        val cy = gridHeight / 2

        when (boss) {
            BossType.PLASMA_CORE -> {
                // Alternating Cross-Beams
                if (cycleCount % 2 == 0) {
                    for (x in 1 until gridWidth - 1) cells.add(GridPos(x, cy))
                    for (y in 6 until gridHeight - 1) cells.add(GridPos(cx, y))
                } else {
                    for (x in 1 until gridWidth - 1) {
                        cells.add(GridPos(x, 9))
                        cells.add(GridPos(x, gridHeight - 7))
                    }
                }
            }
            BossType.CYBER_GUARDIAN -> {
                // Vertical Laser Columns with safe gaps
                val offset = cycleCount % 3
                for (x in (2 + offset) until (gridWidth - 2) step 4) {
                    for (y in 6 until gridHeight - 2) {
                        cells.add(GridPos(x, y))
                    }
                }
            }
            BossType.TOXIC_REACTOR -> {
                // Bio-Hazard Zones (3x3 pools)
                val centers = listOf(
                    GridPos(4, 9),
                    GridPos(gridWidth - 5, 9),
                    GridPos(cx, cy + 2),
                    GridPos(4, gridHeight - 6),
                    GridPos(gridWidth - 5, gridHeight - 6)
                )
                for (c in centers) {
                    for (dx in -1..1) {
                        for (dy in -1..1) {
                            cells.add(GridPos(c.x + dx, c.y + dy))
                        }
                    }
                }
            }
            BossType.NEON_OVERLORD -> {
                // Omni-directional ring + targeted row
                val targetY = snakeHead.y.coerceIn(7, gridHeight - 3)
                for (x in 1 until gridWidth - 1) {
                    cells.add(GridPos(x, targetY))
                }
                for (y in 6 until gridHeight - 1) {
                    cells.add(GridPos(3, y))
                    cells.add(GridPos(gridWidth - 4, y))
                }
            }
        }

        // Remove core positions and keep at least 1 cell distance from snake's current head so it's always dodgeable
        cells.removeAll(corePositions.toSet())
        cells.remove(snakeHead)
        return cells.toList()
    }
}
