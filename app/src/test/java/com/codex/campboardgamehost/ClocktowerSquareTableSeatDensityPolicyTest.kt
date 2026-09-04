package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSquareTableSeatDensityPolicyTest {
    @Test
    fun `identity and detailed seats share the same large fifteen-player geometry`() {
        val identity = clocktowerSquareTableSeatDensity(
            playerCount = 15,
            detailedSeatCards = false,
        )
        val detailed = clocktowerSquareTableSeatDensity(
            playerCount = 15,
            detailedSeatCards = true,
        )

        assertEquals(64f, identity.cardWidth)
        assertEquals(70f, identity.cardHeight)
        assertEquals(detailed.cardWidth, identity.cardWidth)
        assertEquals(detailed.cardHeight, identity.cardHeight)
        assertTrue(identity.primaryMaxLines >= 2)
        assertTrue(identity.primaryFontSizeSp >= 12f)
        assertTrue(detailed.detailFontSizeSp >= 11f)
    }

    @Test
    fun `shared square table density expands when capacity allows and compacts at fifteen`() {
        val roomy = clocktowerSquareTableSeatDensity(playerCount = 10, detailedSeatCards = true)
        val medium = clocktowerSquareTableSeatDensity(playerCount = 13, detailedSeatCards = true)
        val compact = clocktowerSquareTableSeatDensity(playerCount = 15, detailedSeatCards = true)

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
        for (detailedSeatCards in listOf(false, true)) {
            for (playerCount in 5..15) {
                val constraints = hostTableSurfaceLayoutConstraints(
                    availableWidth = 360f,
                    availableHeight = 600f,
                    playerCount = playerCount,
                    detailedSeatCards = detailedSeatCards,
                )
                val layout = hostTableLayout(
                    playerCount = playerCount,
                    constraints = constraints,
                )
                val density = clocktowerSquareTableSeatDensity(
                    playerCount = playerCount,
                    detailedSeatCards = detailedSeatCards,
                )

                assertEquals(playerCount, layout.slots.size)
                assertEquals(density.cardWidth, constraints.seatCardWidth)
                assertEquals(density.cardHeight, constraints.seatCardHeight)
            }
        }
    }
}
