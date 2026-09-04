package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSquareTableSeatDensityPolicyTest {
    @Test
    fun `identity seats preserve compact geometry while allowing wrapped player names`() {
        val density = clocktowerSquareTableSeatDensity(
            playerCount = 15,
            detailedSeatCards = false,
        )

        assertEquals(64f, density.cardWidth)
        assertEquals(50f, density.cardHeight)
        assertTrue(density.primaryMaxLines >= 2)
    }

    @Test
    fun `storyteller detail density expands when table capacity allows and compacts at fifteen`() {
        val roomy = clocktowerSquareTableSeatDensity(playerCount = 10, detailedSeatCards = true)
        val medium = clocktowerSquareTableSeatDensity(playerCount = 13, detailedSeatCards = true)
        val compact = clocktowerSquareTableSeatDensity(playerCount = 15, detailedSeatCards = true)

        assertEquals(80f, roomy.cardWidth)
        assertEquals(90f, roomy.cardHeight)
        assertEquals(72f, medium.cardWidth)
        assertEquals(84f, medium.cardHeight)
        assertEquals(64f, compact.cardWidth)
        assertEquals(70f, compact.cardHeight)
        assertTrue(roomy.detailMaxLines >= 2)
        assertTrue(medium.detailMaxLines >= 2)
        assertTrue(compact.detailMaxLines >= 2)
    }

    @Test
    fun `adaptive detailed density preserves 360 by 600 table capacity for every supported player count`() {
        for (playerCount in 5..15) {
            val constraints = hostTableSurfaceLayoutConstraints(
                availableWidth = 360f,
                availableHeight = 600f,
                playerCount = playerCount,
                detailedSeatCards = true,
            )
            val layout = hostTableLayout(
                playerCount = playerCount,
                constraints = constraints,
            )

            assertEquals(playerCount, layout.slots.size)
            assertEquals(
                clocktowerSquareTableSeatDensity(playerCount, detailedSeatCards = true).cardWidth,
                constraints.seatCardWidth,
            )
            assertEquals(
                clocktowerSquareTableSeatDensity(playerCount, detailedSeatCards = true).cardHeight,
                constraints.seatCardHeight,
            )
        }
    }
}
