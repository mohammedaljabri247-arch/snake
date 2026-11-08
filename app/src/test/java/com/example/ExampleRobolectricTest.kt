package com.example

import com.example.domain.models.ALL_ACHIEVEMENTS
import com.example.domain.models.ALL_MISSIONS
import com.example.domain.models.ArenaLevel
import com.example.domain.models.SnakeSkin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `progression catalogs contain all required skins arenas missions and achievements`() {
        assertEquals(10, SnakeSkin.entries.size)
        assertEquals(7, ArenaLevel.entries.size)
        assertTrue(ALL_MISSIONS.size >= 8)
        assertTrue(ALL_ACHIEVEMENTS.size >= 8)
    }
}
