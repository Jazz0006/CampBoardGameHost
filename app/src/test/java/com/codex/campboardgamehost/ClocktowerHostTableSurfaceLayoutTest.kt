package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerHostTableSurfaceLayoutTest {
    @Test
    fun `setup sized portrait resolves twelve to thirteen without capacity crash`() {
        val twelvePlayers = resolveHostTableSurfaceLayout(
            availableWidth = 360f,
            availableHeight = 520f,
            playerCount = 12,
            detailedSeatCards = false,
        )
        val thirteenPlayers = resolveHostTableSurfaceLayout(
            availableWidth = 360f,
            availableHeight = 520f,
            playerCount = 13,
            detailedSeatCards = false,
        )

        assertEquals(12, twelvePlayers.layout.slots.size)
        assertEquals(13, thirteenPlayers.layout.slots.size)
        assertEquals(72f, twelvePlayers.layout.constraints.seatCardWidth)
        assertEquals(84f, twelvePlayers.layout.constraints.seatCardHeight)
        assertEquals(64f, thirteenPlayers.layout.constraints.seatCardWidth)
        assertEquals(70f, thirteenPlayers.layout.constraints.seatCardHeight)
        assertEquals(
            thirteenPlayers.seatDensity.cardWidth,
            thirteenPlayers.layout.constraints.seatCardWidth,
        )
        assertEquals(
            thirteenPlayers.seatDensity.cardHeight,
            thirteenPlayers.layout.constraints.seatCardHeight,
        )

        // The adaptive result must also satisfy the strict pure-layout contract.
        hostTableLayout(
            playerCount = 13,
            constraints = thirteenPlayers.layout.constraints,
        )
    }

    @Test
    fun `setup sized portrait preserves strict capacity for every supported player count`() {
        for (detailedSeatCards in listOf(false, true)) {
            for (playerCount in 5..15) {
                val resolved = resolveHostTableSurfaceLayout(
                    availableWidth = 360f,
                    availableHeight = 520f,
                    playerCount = playerCount,
                    detailedSeatCards = detailedSeatCards,
                )

                assertEquals(playerCount, resolved.layout.slots.size)
                assertEquals(
                    resolved.seatDensity.cardWidth,
                    resolved.layout.constraints.seatCardWidth,
                )
                assertEquals(
                    resolved.seatDensity.cardHeight,
                    resolved.layout.constraints.seatCardHeight,
                )
                hostTableLayout(
                    playerCount = playerCount,
                    constraints = resolved.layout.constraints,
                )
            }
        }
    }

    @Test
    fun `strict preferred thirteen player geometry remains fail closed`() {
        val preferredConstraints = hostTableSurfaceLayoutConstraints(
            availableWidth = 360f,
            availableHeight = 520f,
            playerCount = 13,
            detailedSeatCards = false,
        )

        val failure = runCatching {
            hostTableLayout(
                playerCount = 13,
                constraints = preferredConstraints,
            )
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
        assertTrue(
            failure?.message?.contains("capacity is insufficient for 13 players") == true,
        )
    }
}
