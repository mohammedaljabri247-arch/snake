package com.example

import com.example.domain.models.*
import com.example.game.engine.GameEngine
import com.example.game.engine.PlayStatus
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `startNewGame initializes snake, arena, and running status`() {
        val engine = GameEngine()
        engine.startNewGame(
            mode = GameMode.CLASSIC,
            arena = ArenaLevel.NEON_CITY,
            bestScore = 500
        )
        val state = engine.state.value
        assertEquals(PlayStatus.RUNNING, state.status)
        assertEquals(4, state.snake.size)
        assertEquals(Direction.UP, state.direction)
        assertEquals(500, state.bestScore)
        assertTrue(state.energyOrbs.isNotEmpty())
    }

    @Test
    fun `queueDirection prevents direct 180-degree reversal`() {
        val engine = GameEngine()
        engine.startNewGame(mode = GameMode.CLASSIC, arena = ArenaLevel.NEON_CITY)
        // Initial direction is UP; queuing DOWN should be ignored
        engine.queueDirection(Direction.DOWN)
        engine.stepTick(reducedVisualEffects = true)
        assertEquals(Direction.UP, engine.state.value.direction)

        // Queuing LEFT is valid
        engine.queueDirection(Direction.LEFT)
        engine.stepTick(reducedVisualEffects = true)
        assertEquals(Direction.LEFT, engine.state.value.direction)
    }

    @Test
    fun `evolution stage progresses accurately with XP`() {
        assertEquals(EvolutionStage.MINI_SNAKE, EvolutionStage.fromXp(0))
        assertEquals(EvolutionStage.NEON_SNAKE, EvolutionStage.fromXp(85))
        assertEquals(EvolutionStage.PLASMA_SNAKE, EvolutionStage.fromXp(220))
        assertEquals(EvolutionStage.CYBER_SNAKE, EvolutionStage.fromXp(420))
        assertEquals(EvolutionStage.ULTIMATE_SNAKE, EvolutionStage.fromXp(750))
    }

    @Test
    fun `pause, resume, and wall collision gameOver + revive work properly`() {
        val engine = GameEngine()
        engine.startNewGame(mode = GameMode.CLASSIC, arena = ArenaLevel.NEON_CITY)

        engine.pauseGame()
        assertEquals(PlayStatus.PAUSED, engine.state.value.status)

        engine.resumeGame()
        assertEquals(PlayStatus.RUNNING, engine.state.value.status)

        // Step UP repeatedly until hitting top wall
        repeat(30) {
            if (engine.state.value.status == PlayStatus.RUNNING) {
                engine.stepTick(reducedVisualEffects = true)
            }
        }
        assertEquals(PlayStatus.GAME_OVER, engine.state.value.status)

        // Revive once
        val revived = engine.revivePlayer()
        assertTrue(revived)
        assertEquals(PlayStatus.RUNNING, engine.state.value.status)
        assertTrue(engine.state.value.reviveUsed)
        assertTrue(engine.state.value.hasPowerUp(PowerUpType.SHIELD))
    }

    @Test
    fun `boss challenge mode initializes boss core and telegraphs attacks`() {
        val engine = GameEngine()
        engine.startNewGame(
            mode = GameMode.BOSS_CHALLENGE,
            arena = ArenaLevel.NEON_CITY,
            selectedBoss = BossType.PLASMA_CORE
        )
        val boss = engine.state.value.bossState
        assertNotNull(boss)
        assertEquals(BossType.PLASMA_CORE.maxHp, boss?.currentHp)
    }
}
