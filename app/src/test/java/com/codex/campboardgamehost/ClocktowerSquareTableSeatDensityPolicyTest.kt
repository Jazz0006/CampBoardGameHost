package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSquareTableSeatDensityPolicyTest {
    @Test
    fun `fifteen-player density preserves minimum geometry and readable typography`() {
        val identity = clocktowerSquareTableSeatDensity(
            playerCount = 15,
        )
        assertEquals(64f, identity.cardWidth)
        assertEquals(70f, identity.cardHeight)
        assertTrue(identity.primaryMaxLines >= 2)
        assertTrue(identity.primaryFontSizeSp >= 12f)
        assertTrue(identity.detailFontSizeSp >= 11f)
    }

    @Test
    fun `shared square table density expands when capacity allows and compacts at fifteen`() {
        val roomy = clocktowerSquareTableSeatDensity(playerCount = 10)
        val medium = clocktowerSquareTableSeatDensity(playerCount = 13)
        val compact = clocktowerSquareTableSeatDensity(playerCount = 15)

        assertEquals(80f, roomy.cardWidth)
        assertEquals(90f, roomy.cardHeight)
        assertEquals(72f, medium.cardWidth)
        assertEquals(84f, medium.cardHeight)
        assertEquals(64f, compact.cardWidth)
        assertEquals(70f, compact.cardHeight)
        assertTrue(roomy.primaryFontSizeSp >= 13f)
        assertTrue(medium.primaryFontSizeSp >= 12f)
        assertTrue(compact.primaryFontSizeSp >= 12f)
        assertTrue(roomy.detailFontSizeSp >= 12f)
        assertTrue(medium.detailFontSizeSp >= 11f)
        assertTrue(compact.detailFontSizeSp >= 11f)
    }

    @Test
    fun `adaptive shared density preserves 360 by 600 table capacity for every supported player count`() {
        for (playerCount in 5..15) {
            val constraints = hostTableSurfaceLayoutConstraints(
                availableWidth = 360f,
                availableHeight = 600f,
                playerCount = playerCount,
            )
            val layout = hostTableLayout(
                playerCount = playerCount,
                constraints = constraints,
            )
            val density = clocktowerSquareTableSeatDensity(
                playerCount = playerCount,
            )

            assertEquals(playerCount, layout.slots.size)
            assertEquals(density.cardWidth, constraints.seatCardWidth)
            assertEquals(density.cardHeight, constraints.seatCardHeight)
        }
    }
}
