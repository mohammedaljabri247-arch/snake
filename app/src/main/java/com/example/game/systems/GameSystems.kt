package com.example.game.systems

import com.example.domain.models.*
import kotlin.math.abs
import kotlin.random.Random

object CollisionSystem {

    fun wrapOrCheckWall(
        nextPos: GridPos,
        gridWidth: Int,
        gridHeight: Int,
        allowWrap: Boolean
    ): Pair<GridPos, Boolean> {
        if (allowWrap) {
            val wrappedX = (nextPos.x + gridWidth) % gridWidth
            val wrappedY = (nextPos.y + gridHeight) % gridHeight
            return GridPos(wrappedX, wrappedY) to false
        }
        val hitWall = nextPos.x < 0 || nextPos.x >= gridWidth || nextPos.y < 0 || nextPos.y >= gridHeight
        return nextPos to hitWall
    }

    fun checkSelfCollision(head: GridPos, bodyWithoutTail: List<GridPos>): Boolean {
        return bodyWithoutTail.any { it == head }
    }

    fun checkObstacleCollision(head: GridPos, obstacles: List<Obstacle>): Obstacle? {
        return obstacles.find { it.pos == head }
    }

    fun checkBossHazardCollision(head: GridPos, bossState: BossState?): Boolean {
        if (bossState == null) return false
        return bossState.activeHazardCells.any { it == head } ||
                bossState.corePositions.any { it == head }
    }
}

object SpawnManager {

    private var nextEntityId = 1L

    fun createArenaObstacles(arena: ArenaLevel, gridWidth: Int, gridHeight: Int): List<Obstacle> {
        val list = mutableListOf<Obstacle>()
        when (arena) {
            ArenaLevel.NEON_CITY -> {
                // Clean open perimeter with 4 subtle corner neon pylons
                list.add(Obstacle(GridPos(3, 4)))
                list.add(Obstacle(GridPos(gridWidth - 4, 4)))
                list.add(Obstacle(GridPos(3, gridHeight - 5)))
                list.add(Obstacle(GridPos(gridWidth - 4, gridHeight - 5)))
            }
            ArenaLevel.CYBER_GRID -> {
                // Twin vertical laser pillars with wide middle and side lanes
                for (y in 6..10) {
                    list.add(Obstacle(GridPos(5, y)))
                    list.add(Obstacle(GridPos(gridWidth - 6, y)))
                }
                for (y in (gridHeight - 11)..(gridHeight - 7)) {
                    list.add(Obstacle(GridPos(5, y)))
                    list.add(Obstacle(GridPos(gridWidth - 6, y)))
                }
            }
            ArenaLevel.PLASMA_ARENA -> {
                // L-shaped corner plasma shields
                val coords = listOf(
                    GridPos(3, 4), GridPos(4, 4), GridPos(3, 5),
                    GridPos(gridWidth - 4, 4), GridPos(gridWidth - 5, 4), GridPos(gridWidth - 4, 5),
                    GridPos(3, gridHeight - 5), GridPos(4, gridHeight - 5), GridPos(3, gridHeight - 6),
                    GridPos(gridWidth - 4, gridHeight - 5), GridPos(gridWidth - 5, gridHeight - 5), GridPos(gridWidth - 4, gridHeight - 6),
                    GridPos(gridWidth / 2, 8), GridPos(gridWidth / 2, gridHeight - 9)
                )
                coords.forEach { list.add(Obstacle(it)) }
            }
            ArenaLevel.DARK_SPACE -> {
                // Floating space monoliths (walls wrap around!)
                list.add(Obstacle(GridPos(6, 8)))
                list.add(Obstacle(GridPos(gridWidth - 7, 8)))
                list.add(Obstacle(GridPos(6, gridHeight - 9)))
                list.add(Obstacle(GridPos(gridWidth - 7, gridHeight - 9)))
            }
            ArenaLevel.TOXIC_ZONE -> {
                // Static hazard blocks + 2 horizontal patrol drones
                list.add(Obstacle(GridPos(4, 6)))
                list.add(Obstacle(GridPos(gridWidth - 5, 6)))
                list.add(Obstacle(GridPos(4, gridHeight - 7)))
                list.add(Obstacle(GridPos(gridWidth - 5, gridHeight - 7)))
                list.add(Obstacle(GridPos(5, 11), velocity = Direction.RIGHT, moveEveryTicks = 4))
                list.add(Obstacle(GridPos(gridWidth - 6, gridHeight - 11), velocity = Direction.LEFT, moveEveryTicks = 4))
            }
            ArenaLevel.CRYSTAL_LAB -> {
                // Symmetrical crystal prism gates
                for (x in 4..7) {
                    list.add(Obstacle(GridPos(x, 7)))
                    list.add(Obstacle(GridPos(gridWidth - 1 - x, 7)))
                    list.add(Obstacle(GridPos(x, gridHeight - 8)))
                    list.add(Obstacle(GridPos(gridWidth - 1 - x, gridHeight - 8)))
                }
            }
            ArenaLevel.VOID_ARENA -> {
                // Elite arena with static gates + 3 moving void sentinels
                for (x in 5..7) {
                    list.add(Obstacle(GridPos(x, 6)))
                    list.add(Obstacle(GridPos(gridWidth - 1 - x, 6)))
                }
                list.add(Obstacle(GridPos(4, 12), velocity = Direction.RIGHT, moveEveryTicks = 3))
                list.add(Obstacle(GridPos(gridWidth - 5, 19), velocity = Direction.LEFT, moveEveryTicks = 3))
                list.add(Obstacle(GridPos(3, gridHeight - 6)))
                list.add(Obstacle(GridPos(gridWidth - 4, gridHeight - 6)))
            }
        }
        return list
    }

    fun findSafePosition(
        gridWidth: Int,
        gridHeight: Int,
        snake: List<GridPos>,
        obstacles: List<Obstacle>,
        existingOrbs: List<EnergyOrb>,
        existingPowerUps: List<PowerUpItem>,
        bossState: BossState?,
        preferNearObstacle: Boolean = false,
        random: Random = Random.Default
    ): GridPos {
        val head = snake.firstOrNull() ?: GridPos(gridWidth / 2, gridHeight / 2)
        val occupied = HashSet<GridPos>()
        occupied.addAll(snake)
        obstacles.forEach { occupied.add(it.pos) }
        existingOrbs.forEach { occupied.add(it.pos) }
        existingPowerUps.forEach { occupied.add(it.pos) }
        bossState?.corePositions?.forEach { occupied.add(it) }
        bossState?.activeHazardCells?.forEach { occupied.add(it) }
        bossState?.warningCells?.forEach { occupied.add(it) }

        val candidates = mutableListOf<GridPos>()
        val riskCandidates = mutableListOf<GridPos>()

        for (x in 1 until (gridWidth - 1)) {
            for (y in 1 until (gridHeight - 1)) {
                val p = GridPos(x, y)
                if (p in occupied) continue
                // Never spawn immediately in front of snake head (prevent unfair instant hit)
                val distToHead = abs(p.x - head.x) + abs(p.y - head.y)
                if (distToHead <= 2) continue

                candidates.add(p)
                if (preferNearObstacle) {
                    val nearObs = obstacles.any { abs(it.pos.x - p.x) + abs(it.pos.y - p.y) in 1..2 }
                    if (nearObs || x == 1 || x == gridWidth - 2 || y == 1 || y == gridHeight - 2) {
                        riskCandidates.add(p)
                    }
                }
            }
        }

        if (preferNearObstacle && riskCandidates.isNotEmpty()) {
            return riskCandidates[random.nextInt(riskCandidates.size)]
        }
        if (candidates.isNotEmpty()) {
            return candidates[random.nextInt(candidates.size)]
        }
        // Fallback any free cell
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val p = GridPos(x, y)
                if (p !in occupied) return p
            }
        }
        return GridPos(gridWidth / 2, gridHeight / 2)
    }

    fun spawnEnergyOrb(
        gridWidth: Int,
        gridHeight: Int,
        snake: List<GridPos>,
        obstacles: List<Obstacle>,
        existingOrbs: List<EnergyOrb>,
        existingPowerUps: List<PowerUpItem>,
        bossState: BossState?,
        currentTick: Int,
        forceType: EnergyType? = null,
        random: Random = Random.Default
    ): EnergyOrb {
        val type = forceType ?: rollEnergyType(random)
        val pos = findSafePosition(
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            snake = snake,
            obstacles = obstacles,
            existingOrbs = existingOrbs,
            existingPowerUps = existingPowerUps,
            bossState = bossState,
            preferNearObstacle = type.isRisk,
            random = random
        )
        val lifetime = when (type) {
            EnergyType.NORMAL -> null
            EnergyType.PLASMA -> 95
            EnergyType.GOLDEN -> 65
            EnergyType.TOXIC -> 55
            EnergyType.BONUS -> 60
        }
        return EnergyOrb(
            id = nextEntityId++,
            pos = pos,
            type = type,
            spawnTick = currentTick,
            maxLifetimeTicks = lifetime
        )
    }

    private fun rollEnergyType(random: Random): EnergyType {
        val roll = random.nextInt(100)
        return when {
            roll < 52 -> EnergyType.NORMAL
            roll < 72 -> EnergyType.PLASMA
            roll < 86 -> EnergyType.GOLDEN
            roll < 94 -> EnergyType.BONUS
            else -> EnergyType.TOXIC
        }
    }

    fun spawnPowerUp(
        gridWidth: Int,
        gridHeight: Int,
        snake: List<GridPos>,
        obstacles: List<Obstacle>,
        existingOrbs: List<EnergyOrb>,
        existingPowerUps: List<PowerUpItem>,
        bossState: BossState?,
        currentTick: Int,
        random: Random = Random.Default
    ): PowerUpItem {
        val types = PowerUpType.entries
        val chosen = types[random.nextInt(types.size)]
        val pos = findSafePosition(
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            snake = snake,
            obstacles = obstacles,
            existingOrbs = existingOrbs,
            existingPowerUps = existingPowerUps,
            bossState = bossState,
            preferNearObstacle = false,
            random = random
        )
        return PowerUpItem(
            id = nextEntityId++,
            pos = pos,
            type = chosen,
            spawnTick = currentTick,
            maxLifetimeTicks = 75
        )
    }
}
